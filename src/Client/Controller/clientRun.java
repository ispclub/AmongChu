/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Controller;

import Client.Model.Matrix;
import Server.Model.UserTable;
import static Utils.Utils.WINDOW_HEIGHT;
import static Utils.Utils.WINDOW_WIDTH;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 *
 * @author hoang
 */
public class clientRun {
    private ConnectThread ct = null;
    private ResponseHandler rsp = null;
    private LoginControl lc = null;
    private SocketChannel socketChannelMain = null;
    private SocketChannel socketChannelTable = null;

    public void setSocketChannelMain(SocketChannel socketChannelMain) {
        this.socketChannelMain = socketChannelMain;
    }
    private LobbyControl lyc = null;
    public static void main(String[] args) throws IOException {
        new clientRun();
    }
    private clientRun() throws IOException
    {
        rsp = new ResponseHandler();
        rsp.setMain(this);
        new Thread(rsp).start();
        ct = new ConnectThread(rsp);
        //ct.initConnection(null, 12346);
        toLogin();
    }
    public void toLogin() throws IOException
    {
        if (lyc != null)
        {
            lyc.close();
            lyc = null;
        }
        if (socketChannelTable != null)
        {
            ct.closeConnect(socketChannelTable);
            socketChannelTable = null;
        }
        lc = new LoginControl(ct, this, rsp);
    }
    public void toLobby(String username, UserTable ut) throws IOException
    {
        lc.close();
        lc = null;
        lyc = new LobbyControl(username, ct, this, ut, socketChannelMain, rsp);
        rsp.setLc(lyc);
        socketChannelTable = ct.initConnection(null, 12347);
    }
    public void toGame(Matrix matrix)
    {
        PikachuController pika = new PikachuController(matrix, ct);
        pika.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
        pika.setLocationRelativeTo(null);
        pika.start();
    }
}
