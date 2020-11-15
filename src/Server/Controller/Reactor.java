/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Controller;

import Message.ServerMessage;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.Iterator;


/**
 *
 * @author hoang
 */
public class Reactor extends Thread implements Serializable{
    private static final int BUFFER_SIZE = 1024;
    private static final int LINGER= 5000;
    private int port;
    private Selector selector;
    private DatabaseManager dbm;
    private ServerSocketChannel listeningSocketChannel;
    private final Object objectToSend = null;
    public Reactor(int port, DatabaseManager dm)
    {
        this.dbm = dm;
        this.port = port;
    }
    @Override
    public void run() 
    {
        try {
            init();
        } catch (IOException ex) {
            System.out.println("Server khởi tạo thất bại");
            System.exit(-1);
        }
        while (true)
        {
            try
            {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext())
                {
                    SelectionKey key = keys.next();
                    keys.remove();
                    synchronized (objectToSend)
                    {
                        if (objectToSend != null)
                        {
                            Client client  = (Client)key.attachment();
                            if (dbm.clientNeedUpdate(client.gameHandler.getClientName()))
                                client.gameHandler.addToQueue(objectToSend);
                        }
                    }
                    if (!key.isValid())
                    {
                        
                    }
                    else if (key.isAcceptable())
                    {
                        sayHello(key);
                    } 
                    else if (key.isReadable())
                    {
                        messageFromClient(key);
                    } 
                    else if (key.isWritable())
                    {
                        sendMessageToClient(key);
                    }
                }
                objectToSend = null;
            }catch (IOException | SQLException e)
            {
                System.out.println(e);
                System.exit(-1);
            }
        }
    }
    private void init() throws IOException
    {
        selector = Selector.open();
        listeningSocketChannel = ServerSocketChannel.open();
        listeningSocketChannel.configureBlocking(false);
        listeningSocketChannel.bind(new InetSocketAddress(port));
        listeningSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void sayHello(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        
        GameHandler gameHandler = new GameHandler(this,clientChannel,dbm);
        
        Client client = new Client(gameHandler);
        clientChannel.register(selector, SelectionKey.OP_READ, client);
        clientChannel.setOption(StandardSocketOptions.SO_LINGER, LINGER);
    }

    private void messageFromClient(SelectionKey key) throws IOException, SQLException {
        Client client  = (Client)key.attachment();
        try
        {
            client.gameHandler.getMessage();
        }catch (Exception e)
        {
            System.out.println(e);
            client.gameHandler.Logout();
            key.cancel();
            if (key.channel() != null)
            {
                key.channel().close();
            }
        }
    }

    private void sendMessageToClient(SelectionKey key) throws IOException {
        Client client = (Client) key.attachment();
        try {
            client.sendMsg();
            key.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {
            deleteClient(key);
        }
    }

    private void deleteClient(SelectionKey key) throws IOException {
        Client client = (Client) key.attachment();
        client.gameHandler.closeConnection();
    }
    
    void sendMessageToClient(SocketChannel channel) {
        channel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
        selector.wakeup();
    }
    
    //
    private class Client
    {
        private GameHandler gameHandler;
        
        private Client(GameHandler gm)
        {
            this.gameHandler = gm;
        }
        private void sendMsg() throws IOException
        {
            ByteBuffer msg = null;
            while ((msg = gameHandler.messagesToSend.peek()) != null)
            {
                gameHandler.sendMsg(msg);
                gameHandler.messagesToSend.remove();
            }
        }
    }
    public void sendToAll(Object s) 
    {
        synchronized (objectToSend)
        {
            ServerMessage sm = new ServerMessage(ServerMessage.STATUS.S_WARN, ServerMessage.ACTION.NONE, s, ServerMessage.REQUEST.TABLEDATA);
            objectToSend = sm;
        }
    }
}
