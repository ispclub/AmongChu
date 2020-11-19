/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Controller;

import Server.Model.Message.ClientMessage;
import Server.Model.Message.ServerMessage;
import Server.Model.ServerDataEvent;
import Server.Model.UserAccount;
import Server.Model.UserTable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
                String user = (String) (clientName.get(dataEvent.getSocket()));
                try {
                    dbm.setLogout(user);
                } catch (SQLException ex) {
                    System.out.println("Set Logout thất bại");
                }
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
                if (cm.getRequest() == ClientMessage.REQUEST.LOGIN) {
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
                } else if (cm.getRequest() == ClientMessage.REQUEST.CHALLENGE) {
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
                                    //Send request and if ok -> make challenge
                                    SocketChannel sc = (SocketChannel) (clientName.getKey(userToChallenge));
                                    sm = new ServerMessage(ServerMessage.STATUS.S_WARN, ServerMessage.ACTION.NONE, currentUser, ServerMessage.REQUEST.CHALLENGE);
                                    dataEvent.getServerReactor().send(sc, serialize(sm));
                                    Match.put(currentUser, userToChallenge);
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
                }
            }
        }
    }
}
