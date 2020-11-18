/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Controller;

import Client.View.LobbyForm;
import Server.Model.Message.ClientMessage;
import Server.Model.UserTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.channels.SocketChannel;

/**
 *
 * @author hoang
 */
public class LobbyControl {
    private String username;
    private ConnectThread ct;
    private clientRun main;
    private LobbyForm lf = null;
    private SocketChannel sk = null;
    public LobbyControl(String username, ConnectThread ct, clientRun main, UserTable ut, SocketChannel sk) {
        this.username = username;
        this.sk = sk;
        this.ct = ct;
        this.main = main;
        lf = new LobbyForm();
        lf.setLabelName(username);
        lf.setVisible(true);
        lf.setTable(ut);
        lf.addLogoutListener(new LogoutListener());
    }
    private static byte[] serialize(Object obj) throws IOException 
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    private class LogoutListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            try
            {
                //ClientMessage cm = new ClientMessage(ClientMessage.REQUEST.LOGOUT, null);
                //ct.send(serialize(cm), sk);
                ct.closeConnect(sk);
                main.toLogin();
            }catch (Exception ex)
            {
                System.out.println(ex);
            }
        }
    }
    @Override
    protected void finalize()
    {
        lf.dispose();
    }
    public void close()
    {
        this.finalize();
    }
}
