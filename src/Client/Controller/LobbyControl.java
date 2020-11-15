/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Controller;

import Client.ClientMain.Main;
import Client.View.LobbyForm;
import Server.Model.UserTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author hoang
 */
public class LobbyControl {
    private String host;
    private int port;
    private LobbyForm lf = null;
    private ConnectThread ct;
    private String user;
    private Main main;
    public LobbyControl(ConnectThread ct, String user, Main main, UserTable ut)
    {
        this.main = main;
        this.user = user;
        this.ct = ct;
        lf = new LobbyForm();
        lf.setVisible(true);
        lf.setTable(ut);
        lf.addLogoutListener(new LogoutListener());
        // add Table ControlThread and cast it to ConnectThread
    }
    private class LogoutListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            try
            {
                ct.Logout(user);
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
