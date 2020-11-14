/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.ClientMain;

import Client.Controller.ConnectThread;
import Client.Controller.LobbyControl;
import Client.Controller.LoginControl;
import java.util.Vector;

/**
 *
 * @author hoang
 */
public class Main {
    private ConnectThread ct= null;
    private LoginControl lc = null;
    private LobbyControl lyc = null;
    public static void main(String[] args)
    {
        new Main();
    }
    private Main()
    {
        ct = new ConnectThread();
        ct.setMain(this);
        ct.connect("localhost",12346);
        lc = new LoginControl(ct);
    }
    public void toLobby(String user)
    {
        lc.close();
        lc = null;
        lyc = new LobbyControl(ct, user, this);
    }
    public void toLogin()
    {
        lyc.close();
        lyc = null;
        lc = new LoginControl(ct);
    }
}
