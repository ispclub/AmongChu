/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Controller;

import Client.View.LoginForm;
import Server.Model.Message.ClientMessage;
import Server.Model.UserAccount;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.channels.SocketChannel;
import javax.swing.JOptionPane;

/**
 *
 * @author hoang
 */
public class LoginControl {
    private LoginForm lf = null;
    private ConnectThread ct;
    private clientRun cr;
    private SocketChannel sk;
    public LoginControl(ConnectThread ct, clientRun cr, ResponseHandler rh)
    {
        this.cr = cr;
        this.ct = ct;
        lf = new LoginForm();
        lf.setVisible(true);
        lf.addLoginListener(new LoginListener());
        rh.setFrameToShow(lf);
        if (!ct.isAlive())
            ct.start();
    }
    private static byte[] serialize(Object obj) throws IOException 
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    private class LoginListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e) {
            try
            {
                if ((sk = ct.initConnection(null, 12346)) == null){
                    JOptionPane.showMessageDialog(lf, "Không thể kết nối tới server");
                }
                UserAccount x = lf.getUser();
                cr.setSocketChannelMain(sk);
                ClientMessage cm = new ClientMessage(ClientMessage.REQUEST.LOGIN, x);
                ct.send(serialize(cm), sk);
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
