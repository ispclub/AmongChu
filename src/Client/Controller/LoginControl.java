/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Controller;

import Client.View.LoginForm;
import Server.Model.User;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author hoang
 */
public class LoginControl {
    private String host;
    private int port;
    private LoginForm lf = null;
    private ConnectThread ct;
    public LoginControl(ConnectThread ct)
    {
        this.ct = ct;
        lf = new LoginForm();
        lf.setVisible(true);
        lf.addLoginListener(new LoginListener());
        ct.setParentToShow(lf);
    }
    private class LoginListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e) {
            try
            {
                System.out.println(lf.getUser().getAccount_id() + "\n" + lf.getUser().getPassword());
                User x = lf.getUser();
                ct.Login(x);
                
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
