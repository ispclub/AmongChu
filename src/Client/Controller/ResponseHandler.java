/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Controller;

import Client.Model.ClientDataEvent;
import Client.View.LobbyForm;
import Client.View.LoginForm;
import Server.Model.Message.ServerMessage;
import Server.Model.UserTable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author hoang
 */
public class ResponseHandler implements Runnable
{
    private JFrame frameToShow;
    private clientRun main;

    public void setMain(clientRun main) {
        this.main = main;
    }
    
    public void setFrameToShow(JFrame frameToShow) {
        this.frameToShow = frameToShow;
    }
    private List queue = new LinkedList();
    private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException 
    {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
    public void processData(ConnectThread ct, SocketChannel socket, byte[] data, int count)
    {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        synchronized (queue)
        {
            queue.add(new ClientDataEvent(ct, socket, data));
            queue.notify();
        }
    }
    @Override
    public void run() {
        ClientDataEvent cDataEvent;
        while (true) {
            synchronized (queue) {
                try {
                    queue.wait();
                } catch (InterruptedException ex) {
                    //handle it here
                }
                cDataEvent = (ClientDataEvent) queue.remove(0);
            }
            try {
                Object o = deserialize(cDataEvent.getData());
                if (o instanceof ServerMessage)
                {
                    ServerMessage sm = (ServerMessage)o;
                    if (sm.getRequest() == ServerMessage.REQUEST.LOGIN)
                    {
                        switch (sm.getStatus())
                        {
                            case S_OK:
                                JOptionPane.showMessageDialog(frameToShow, "Đăng nhập thành công");
                                main.toLobby(((LoginForm)frameToShow).getUser().getUsername(), (UserTable)(sm.getData()));
                                break;
                            case S_WARN:
                                JOptionPane.showMessageDialog(frameToShow, "Tài khoản đã đăng nhập ở vị trí khác");
                                break;
                            case S_FAIL:
                                JOptionPane.showMessageDialog(frameToShow, "Tài khoản hoặc mật khẩu không chính xác");
                        }
                    } else if (sm.getRequest() == ServerMessage.REQUEST.TABLEDATA)
                    {
                        if (frameToShow instanceof LobbyForm)
                        {
                            ((LobbyForm)frameToShow).setTable((UserTable)(sm.getData()));
                        }
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex);
            } catch (ClassNotFoundException ex) {
                System.out.println(ex);
            }
            
        }
    }
    
}
