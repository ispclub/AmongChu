/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Controller;

import Client.ClientMain.Main;
import Message.ClientMessage;
import Message.ServerMessage;
import Server.Model.User;
import Server.Model.UserTable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author hoang
 */
public class ConnectThread extends Thread{
    private boolean isConnected;
    private SocketChannel socketChannel;
    private InetSocketAddress serverAddress;
    private Selector selector;
    private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
    private final ByteBuffer messageFromServer = ByteBuffer.allocate(1024);
    private volatile boolean timeToSend = false;
    private ServerMessage serverMessage;
    private JFrame parentToShow;
    private Main main;
    private String username;
    
    public void setMain(Main main) {
        this.main = main;
    }
    public void connect(String host, int port)
    {
        serverAddress = new InetSocketAddress(host, port);
        new Thread(this).start();
    }

    public void setParentToShow(JFrame parentToShow) {
        this.parentToShow = parentToShow;
    }
    
    @Override
    public void run()
    {
        try
        {
            //setup connection
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(serverAddress);
            isConnected = true;
            //setup selector
            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            //
            while (isConnected || !messagesToSend.isEmpty())
            {
                if (timeToSend)
                {
                    socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    timeToSend = false;
                }
                selector.select();
                for (SelectionKey key : selector.selectedKeys())
                {
                    selector.selectedKeys().remove(key);
                    if (!key.isValid())
                    {
                        continue;
                    } else if (key.isConnectable())
                    {
                        makeConnection(key);
                    } else if (key.isReadable())
                    {
                        msgFromServer(key);
                    } else if (key.isWritable())
                    {
                        msgToServer(key);
                    }
                }
            }
        } catch (Exception ex) 
        {
            JOptionPane.showMessageDialog(parentToShow, "Không thể kết nối tới server \n" + ex.toString());
            //lf.showMessage("Khong thể kết nối tới server \n" + ex.toString());
        } 
    }

    private static Object byteBufferToObject(ByteBuffer byteBuffer) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream byteArrayInputStream;
        byteArrayInputStream = new ByteArrayInputStream(byteBuffer.array());
        ObjectInputStream ios = new ObjectInputStream(byteArrayInputStream);
        return ios.readObject();
    }
    
    private void makeConnection(SelectionKey key) throws IOException {
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
    }

    private void msgFromServer(SelectionKey key) throws IOException, ClassNotFoundException {
        messageFromServer.clear();
        int bytesInBuffer = socketChannel.read(messageFromServer);
        if (bytesInBuffer == -1) {
            throw new IOException("Buffer corrupt");
        }
        Object o = byteBufferToObject(messageFromServer);
        if (o instanceof ServerMessage)
        {
            if (((ServerMessage)o).getRequest() == ServerMessage.REQUEST.LOGIN)
            {
                if (null != ((ServerMessage)o).getStatus())
                switch (((ServerMessage)o).getStatus()) {
                    case S_OK:
                        //Đăng nhập thành công
                        JOptionPane.showMessageDialog(parentToShow, "Đăng nhập thành công");
                        main.toLobby(username, (UserTable)(((ServerMessage)o).getData()));
                        break;
                    case s_FAIL:
                        username = null;
                        JOptionPane.showMessageDialog(parentToShow, "Tên tài khoản hoặc mật khẩu không đúng!");
                        //lf.showMessage("Tên tài khoản hoặc mật khẩu không đúng!");
                        break;
                    case S_WARN:
                        username = null;
                        JOptionPane.showMessageDialog(parentToShow, "Tài khoản đã được đăng nhập tại một vị trí khác!");
                        break;
                    default:
                        break;
                }
            } else if (((ServerMessage)o).getRequest() == ServerMessage.REQUEST.TABLEDATA)
            {
                //setup data to table and point here
                //tct.setUt((UserTable)(((ServerMessage)o).getData()));
            }
        }
    }

    private void msgToServer(SelectionKey key) throws IOException {
        ByteBuffer msg;
        synchronized (messagesToSend)
        {
            while ((msg = messagesToSend.peek()) != null)
            {
                socketChannel.write(msg);
                if (msg.hasRemaining())
                {
                    return;
                }
                messagesToSend.remove();
            }
            key.interestOps(SelectionKey.OP_READ);
        }
    }
    
    private void sendObject(Object o) throws IOException
    {
        ByteArrayOutputStream byteArrayOutpuStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutpuStream);
        oos.writeObject(o);
        oos.flush();
        synchronized (messagesToSend)
        {
            messagesToSend.add(ByteBuffer.wrap(byteArrayOutpuStream.toByteArray()));
        }
        timeToSend = true;
        selector.wakeup();
    }
    public void Login(User x) throws IOException {
        ClientMessage cm = new ClientMessage(ClientMessage.REQUEST.LOGIN, x);
        username = x.getAccount_id();
        sendObject(cm);
    }
    public void Logout(String user) throws IOException
    {
        ClientMessage cm = new ClientMessage(ClientMessage.REQUEST.LOGOUT, user);
        sendObject(cm);
    }

    public void getUserTable() throws IOException {
        ClientMessage cm = new ClientMessage(ClientMessage.REQUEST.TABLEDATA, null);
        sendObject(cm);
    }
    
}
