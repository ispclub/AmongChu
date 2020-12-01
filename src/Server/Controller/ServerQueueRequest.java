/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Controller;

import Server.Model.Matrix;
import Server.Model.Match;
import Server.Model.Message.ClientMessage;
import Server.Model.Message.ServerMessage;
import Server.Model.ServerDataEvent;
import Server.Model.UserAccount;
import Server.Model.UserTable;
import Utils.Utils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

/**
 *
 * @author hoang
 */
public class ServerQueueRequest implements Runnable {

    private final List queue = new LinkedList();
    private final BidiMap clientName = new DualHashBidiMap();
    private DatabaseManager dbm;
    private Map Match = new HashMap();
    private final Map runningGame = new HashMap();

    private Object byteBufferToObject(ByteBuffer b) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream;
        byteArrayInputStream = new ByteArrayInputStream(b.array());
        ObjectInputStream ois = new ObjectInputStream(byteArrayInputStream);
        return ois.readObject();
    }

    private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    private static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public void processData(ServerReactor aThis, SocketChannel socketChannel, byte[] array, int numRead) {
        if (numRead == -1) {
            synchronized (queue) {
                queue.add(new ServerDataEvent(aThis, socketChannel, null));
                queue.notify();
            }
            return;
        }
        byte[] dataCopy = new byte[numRead];
        System.arraycopy(array, 0, dataCopy, 0, numRead);
        synchronized (queue) {
            queue.add(new ServerDataEvent(aThis, socketChannel, dataCopy));
            queue.notify();
        }
    }

    public ServerQueueRequest() throws SQLException {
        dbm = new DatabaseManager();
    }

    private void addToRequestMap(String a, String b) {
        Set set = (Set) Match.get(a);
        if (set == null) {
            set = new HashSet();
            this.Match.put(a, set);
        }
        set.add(b);
    }

    private void removeFromRequestMap(String user) {
        this.Match.remove(user);
    }

    private boolean needCreateMatch(String currentUser, String otherUser) {
        Set array = (Set) this.Match.get(otherUser);
        if (array == null) {
            return false;
        }
        if (array.contains(currentUser)) {
            //remove all request
            this.Match.remove(currentUser);
            this.Match.remove(otherUser);
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        ServerDataEvent dataEvent;
        while (true) {
            synchronized (queue) {
                while (queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException ex) {
                        System.out.println("Lỗi race condition ServerQueueRequest");
                    }
                }
                dataEvent = (ServerDataEvent) queue.remove(0);
            }
            // Handler message here
            if (dataEvent.getData() == null) {
                // remove the client
                if (clientName.get(dataEvent.getSocket()) == null) {
                    continue;
                }
                String user = (String) (clientName.get(dataEvent.getSocket()));
                try {
                    dbm.setLogout(user);
                } catch (SQLException ex) {
                    System.out.println("Set Logout thất bại");
                }
                if (runningGame.get(user) != null) {
                    String winner = ((Match) runningGame.get(user)).getOther(user);
                    String loser = new String(user);
                    SocketChannel winnerSocket = (SocketChannel) clientName.getKey(winner);
                    ServerMessage sm = new ServerMessage(ServerMessage.STATUS.S_WARN, ServerMessage.ACTION.NONE, loser, ServerMessage.REQUEST.RESULT);
                    try {
                        dataEvent.getServerReactor().send(winnerSocket, serialize(sm));
                    } catch (IOException ex) {
                        System.out.println("Gửi response force close thất bại");
                    }
                    try {
                        dbm.addPoint(winner);
                        dbm.setOnline(winner);
                    } catch (SQLException ex) {
                        System.out.println("Except khi add point khi force close");
                    }
                    runningGame.remove(winner);
                    runningGame.remove(user);
                }
                removeFromRequestMap(user);
                clientName.removeValue(dataEvent.getSocket());
                System.out.println("Client đăng xuất thành công");
                continue;
            }
            Object o = null;
            try {
                o = deserialize(dataEvent.data);
            } catch (IOException ex) {
                System.out.println("Exception deser (IO) " + ex);
            } catch (ClassNotFoundException ex) {
                System.out.println("Exception deser (ClassNotFound) " + ex);
            }
            if (o instanceof ClientMessage) {
                ClientMessage cm = (ClientMessage) o;
                if (null != cm.getRequest()) {
                    switch (cm.getRequest()) {
                        case LOGIN:
                            if (!(cm.getData() instanceof UserAccount)) {
                                continue;
                            }
                            UserAccount ua = (UserAccount) cm.getData();
                            int temp = -1;
                            try {
                                if ((temp = dbm.checkLogin(ua)) == 0) {
                                    System.out.println("Client đăng nhập thành công");
                                    dbm.setLogin(ua);
                                    clientName.put(dataEvent.getSocket(), ua.getUsername());
                                    UserTable ut = dbm.getUserTable();
                                    ServerMessage sm = new ServerMessage(ServerMessage.STATUS.S_OK, ServerMessage.ACTION.NONE, ut, ServerMessage.REQUEST.LOGIN);
                                    dataEvent.getServerReactor().send(dataEvent.getSocket(), serialize(sm));
                                } else if (temp == 1) {
                                    System.out.println("Client đăng nhập thất bại");
                                    ServerMessage sm = new ServerMessage(ServerMessage.STATUS.S_FAIL, ServerMessage.ACTION.NONE, null, ServerMessage.REQUEST.LOGIN);
                                    dataEvent.getServerReactor().send(dataEvent.getSocket(), serialize(sm));
                                } else if (temp == 2) {
                                    System.out.println("Client đã được đăng nhập tại vị trí khác");
                                    ServerMessage sm = new ServerMessage(ServerMessage.STATUS.S_WARN, ServerMessage.ACTION.NONE, null, ServerMessage.REQUEST.LOGIN);
                                    dataEvent.getServerReactor().send(dataEvent.getSocket(), serialize(sm));
                                }
                            } catch (SQLException ex) {
                                System.out.println("Check đăng nhập thất bại");
                            } catch (IOException ex) {
                                System.out.println("Serialize thất bại");
                            }
                            break;
                        case CHALLENGE:
                            if (!(cm.getData() instanceof String)) {
                                continue;
                            }
                            String userToChallenge = (String) (cm.getData());
                            String currentUser = (String) (clientName.get(dataEvent.getSocket()));
                            ServerMessage sm = null;
                            try {
                                if (userToChallenge.equals(currentUser)) {
                                    sm = new ServerMessage(ServerMessage.STATUS.S_FAIL, ServerMessage.ACTION.MESSAGE_BOX, "Bạn không thể thách đấu chính bản thân", ServerMessage.REQUEST.CHALLENGE);
                                    dataEvent.getServerReactor().send(dataEvent.getSocket(), serialize(sm));
                                } else {
                                    if (dbm.checkIsLogin(userToChallenge) == true) {
                                        if (dbm.checkIsPlaying(userToChallenge) == false) {
                                            // check if the userToChallenge is the one who request first
                                            if (needCreateMatch(currentUser, userToChallenge)) {
                                                //Create a match here
                                                Match match = new Match(currentUser, userToChallenge);
                                                runningGame.put(currentUser, match);
                                                runningGame.put(userToChallenge, match);
                                                Matrix matrix = new Matrix(Utils.MAP_ROW, Utils.MAP_COL);
                                                matrix.renderMatrix();
                                                SocketChannel sc = (SocketChannel) (clientName.getKey(userToChallenge));
                                                sm = new ServerMessage(ServerMessage.STATUS.S_OK, ServerMessage.ACTION.NONE, matrix, ServerMessage.REQUEST.CHALLENGE);
                                                byte[] Match = serialize(sm);
                                                dbm.setPlaying(currentUser, userToChallenge);
                                                // gửi cho cả 2
                                                dataEvent.getServerReactor().send(sc, Match);
                                                dataEvent.getServerReactor().send(dataEvent.getSocket(), Match);
                                                System.out.println("Tạo trận đấu");
                                            } else {
                                                SocketChannel sc = (SocketChannel) (clientName.getKey(userToChallenge));
                                                sm = new ServerMessage(ServerMessage.STATUS.S_WARN, ServerMessage.ACTION.NONE, currentUser, ServerMessage.REQUEST.CHALLENGE);
                                                dataEvent.getServerReactor().send(sc, serialize(sm));
                                                addToRequestMap(currentUser, userToChallenge);
                                            }

                                        } else {
                                            sm = new ServerMessage(ServerMessage.STATUS.S_FAIL, ServerMessage.ACTION.MESSAGE_BOX, "Người chơi đã trong trận đấu khác", ServerMessage.REQUEST.CHALLENGE);
                                            dataEvent.getServerReactor().send(dataEvent.getSocket(), serialize(sm));
                                        }
                                    } else {
                                        sm = new ServerMessage(ServerMessage.STATUS.S_FAIL, ServerMessage.ACTION.MESSAGE_BOX, "Người chơi hiện không Online", ServerMessage.REQUEST.CHALLENGE);
                                        dataEvent.getServerReactor().send(dataEvent.getSocket(), serialize(sm));
                                    }
                                }
                            } catch (SQLException ex) {
                                System.out.println(ex);
                            } catch (IOException ex) {
                                System.out.println(ex);
                            }
                            break;
                        case WIN:
                            String winner = (String) (clientName.get(dataEvent.getSocket()));
                            String loser = ((Match) runningGame.get(winner)).getOther(winner);
                            try {
                                dbm.addPoint(winner);
                            } catch (SQLException ex) {
                                System.out.println("Fail when add point");
                            }
                            try {
                                dbm.setOnline(loser, winner);
                            } catch (SQLException ex) {
                                System.out.println("SQLe when setOnline");
                            }
                            SocketChannel scLoser = (SocketChannel) (clientName.getKey(loser));
                            ServerMessage smWinner = new ServerMessage(ServerMessage.STATUS.S_OK, ServerMessage.ACTION.NONE, loser, ServerMessage.REQUEST.RESULT);
                            try {
                                dataEvent.getServerReactor().send(dataEvent.getSocket(), serialize(smWinner));
                            } catch (IOException ex) {
                                System.out.println("Fail send winner");
                            }
                            ServerMessage smLoser = new ServerMessage(ServerMessage.STATUS.S_FAIL, ServerMessage.ACTION.NONE, winner, ServerMessage.REQUEST.RESULT);
                            try {
                                dataEvent.getServerReactor().send(scLoser, serialize(smLoser));
                            } catch (IOException ex) {
                                System.out.println("Fail send loser");
                            }
                            runningGame.remove(winner);
                            runningGame.remove(loser);
                            break;
                        case REGISTER:
                            if (!(cm.getData() instanceof UserAccount)) {
                                continue;
                            }
                            UserAccount regAccount = (UserAccount) cm.getData();
                            try {
                                if (dbm.Register(regAccount)) {
                                    ServerMessage regSM = new ServerMessage(ServerMessage.STATUS.S_OK, ServerMessage.ACTION.NONE, null, ServerMessage.REQUEST.REGISTER);
                                    dataEvent.getServerReactor().send(dataEvent.getSocket(), serialize(regSM));
                                } else {
                                    ServerMessage regSM = new ServerMessage(ServerMessage.STATUS.S_FAIL, ServerMessage.ACTION.NONE, null, ServerMessage.REQUEST.REGISTER);
                                    dataEvent.getServerReactor().send(dataEvent.getSocket(), serialize(regSM));
                                }
                            } catch (SQLException ex) {
                                System.out.println("Gửi thông tin create new account thất bại 1");
                            } catch (IOException ex) {
                                System.out.println("Gửi thông tin create new account thất bại 2");
                            }
                            break;
                        case QUIT:
                            String user = (String) (clientName.get(dataEvent.getSocket()));
                            if (runningGame.get(user) != null) {
                                String Winner = ((Match) runningGame.get(user)).getOther(user);;
                                String Loser = new String(user);
                                SocketChannel winnerSocket = (SocketChannel) clientName.getKey(Winner);
                                ServerMessage Sm = new ServerMessage(ServerMessage.STATUS.S_WARN, ServerMessage.ACTION.NONE, Loser, ServerMessage.REQUEST.RESULT);
                                try {
                                    dataEvent.getServerReactor().send(winnerSocket, serialize(Sm));
                                } catch (IOException ex) {
                                    System.out.println("Gửi response force close thất bại");
                                }
                                try {
                                    dbm.addPoint(Winner);
                                    dbm.setOnline(Winner,Loser);
                                } catch (SQLException ex) {
                                    System.out.println("Except khi add point khi force close");
                                }
                                runningGame.remove(Winner);
                                runningGame.remove(Loser);
                            }
                            removeFromRequestMap(user);
                            clientName.removeValue(dataEvent.getSocket());
                            System.out.println("Client " + user + " đã thoát game!");
                            break;
                        case MATCH:
                            String curUser = (String) (clientName.get(dataEvent.getSocket()));
                            Match m = (Match) runningGame.get(curUser);
                            int i = ((Integer)(cm.getData()));
                            m.setPoint(curUser, i);
                            String w;
                            if ((w = m.getWinner()) != null)
                            {
                                if (w.equals("\n"))
                                {
                                    //hoa
                                    String otherUser = m.getOther(curUser);
                                    SocketChannel otherSck = (SocketChannel)(clientName.getKey(otherUser));
                                    try {
                                        dbm.draw(curUser, otherUser);
                                        dbm.setOnline(curUser, otherUser);
                                    } catch (SQLException ex) {
                                        System.out.println("Set draw thất bại!");
                                    }
                                    ServerMessage drawMsgMe = new ServerMessage(ServerMessage.STATUS.S_WARN, ServerMessage.ACTION.MESSAGE_BOX, otherUser, ServerMessage.REQUEST.RESULT);
                                    ServerMessage drawMsgOther = new ServerMessage(ServerMessage.STATUS.S_WARN, ServerMessage.ACTION.MESSAGE_BOX, curUser, ServerMessage.REQUEST.RESULT);
                                    try {
                                        dataEvent.getServerReactor().send(dataEvent.getSocket(), serialize(drawMsgMe));
                                        dataEvent.getServerReactor().send(otherSck, serialize(drawMsgOther));
                                    } catch (IOException ex) {
                                        System.out.println("Gửi msg hòa thất bại");
                                    }
                                    runningGame.remove(curUser);
                                    runningGame.remove(otherUser);
                                    break;
                                }
                                String l = m.getOther(w);
                                Utils.debug(getClass(), "Game " + w + " " + l);
                                try {
                                    dbm.addPoint(w);
                                } catch (SQLException ex) {
                                    System.out.println("Fail when add point");
                                }
                                try {
                                    dbm.setOnline(l, w);
                                } catch (SQLException ex) {
                                    System.out.println("SQLe when setOnline");
                                }
                                SocketChannel sckLoser = (SocketChannel) (clientName.getKey(l));
                                SocketChannel sckWinner = (SocketChannel) (clientName.getKey(w));
                                ServerMessage smgWinner = new ServerMessage(ServerMessage.STATUS.S_OK, ServerMessage.ACTION.NONE, l, ServerMessage.REQUEST.RESULT);
                                try {
                                    dataEvent.getServerReactor().send(sckWinner, serialize(smgWinner));
                                } catch (IOException ex) {
                                    System.out.println("Fail send winner");
                                }
                                ServerMessage smgLoser = new ServerMessage(ServerMessage.STATUS.S_FAIL, ServerMessage.ACTION.NONE, w, ServerMessage.REQUEST.RESULT);
                                try {
                                    dataEvent.getServerReactor().send(sckLoser, serialize(smgLoser));
                                } catch (IOException ex) {
                                    System.out.println("Fail send loser");
                                }
                                runningGame.remove(w);
                                runningGame.remove(l);
                            }
                            break;
                        default:
                            break;
                    }
                }
            } else {
                System.out.println("Đéo hiểu gửi gì");
            }
        }
    }
}
