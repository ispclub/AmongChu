/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Controller;

import Message.ClientMessage;
import Message.ServerMessage;
import Server.Model.Session;
import Server.Model.User;
import Server.Model.UserTable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 *
 * @author hoang
 */
public class GameHandler {
    private Reactor reactor;
    private SocketChannel channel;
    private Session session;
    private DatabaseManager dbm;
    protected final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
    private ByteBuffer msgFromClient = ByteBuffer.allocate(1024);
    private ClientMessage clientMessage;
    public GameHandler(Reactor r, SocketChannel sc, DatabaseManager dm)
    {
        this.dbm = dm;
        this.reactor = r;
        this.channel = sc;
        this.session = new Session();
    }

    private static Object byteBufferToObject(ByteBuffer byteBuffer) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream byteArrayInputStream;
        byteArrayInputStream = new ByteArrayInputStream(byteBuffer.array());
        ObjectInputStream ios = new ObjectInputStream(byteArrayInputStream);
        return ios.readObject();
    }
    
    public void sendMsg(ByteBuffer msg) throws IOException {
        channel.write(msg);
        if (msg.hasRemaining())
        {
            throw new IOException("Send not complete");
        }
    }

    public void getMessage() throws Exception {
        msgFromClient.clear();
        clientMessage = new ClientMessage();
        int numBytes = channel.read(msgFromClient);
        if (numBytes == -1) 
        {
            throw new IOException("Connection Closed");
        }
        Object s = byteBufferToObject(msgFromClient);
        if (s instanceof ClientMessage)
        {
            clientMessage = (ClientMessage)s;
            handleMessage();
        }
    }
    
    public void addToQueue(Object sm) throws IOException
    {
        ByteArrayOutputStream byteArrayOutpuStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutpuStream);
        oos.writeObject(sm);
        oos.flush();
        synchronized (messagesToSend)
        {
            messagesToSend.add(ByteBuffer.wrap(byteArrayOutpuStream.toByteArray()));
        }
    }
    
    private void handleMessage() {
        try
        {
            if (null != clientMessage.getRequest())
            switch (clientMessage.getRequest()) {
                case LOGIN:
                    if (clientMessage.getData() instanceof User)
                    {
                        int temp = -1;
                        if ((temp = dbm.checkLogin((User)clientMessage.getData())) == 0)
                        {
                            System.out.println("Dang nhap thanh cong");
                            dbm.setLogin((User)clientMessage.getData());
                            this.session = dbm.getSession((User)clientMessage.getData());
                            ServerMessage sm = new ServerMessage(ServerMessage.STATUS.S_OK, ServerMessage.ACTION.NONE, ((User)clientMessage.getData()).getAccount_id(), ServerMessage.REQUEST.LOGIN);
                            addToQueue(sm);
                            reactor.sendMessageToClient(channel);
                        }
                        else if (temp == 1)
                        {
                            System.out.println("Dang nhap that bai");
                            ServerMessage sm = new ServerMessage(ServerMessage.STATUS.s_FAIL, ServerMessage.ACTION.NONE, null, ServerMessage.REQUEST.LOGIN);
                            addToQueue(sm);
                            reactor.sendMessageToClient(channel);
                        }
                        else if (temp == 2)
                        {
                            System.out.println("Da dang nhap");
                            ServerMessage sm = new ServerMessage(ServerMessage.STATUS.S_WARN, ServerMessage.ACTION.NONE, null, ServerMessage.REQUEST.LOGIN);
                            addToQueue(sm);
                            reactor.sendMessageToClient(channel);
                        }
                    }   break;
                case LOGOUT:
                    if (clientMessage.getData() instanceof String)
                    {
                        dbm.setLogout((String)clientMessage.getData());
                        System.out.println("Client logout");
                    }   break;
                case TABLEDATA:
                    UserTable ut = dbm.getUserTable();
                    ServerMessage sm = new ServerMessage(ServerMessage.STATUS.S_OK, ServerMessage.ACTION.NONE, ut, ServerMessage.REQUEST.TABLEDATA);
                    addToQueue(sm);
                    reactor.sendMessageToClient(channel);
                    break;
                    // A huge nub here but i don't want to fix it (Perfomance)
                default:
                    break;
            }
        }catch (IOException | SQLException e)
        {
            System.out.println(e);
        }
    }

    void closeConnection() throws IOException {
        channel.close();
    }
    
    public void Logout() throws SQLException
    {
        if (!this.session.isIsLogged())
            return;
        dbm.setLogout(this.session.getAccountID());
    }
    
}
