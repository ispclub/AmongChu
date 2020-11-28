/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Controller;

import Client.Model.Matrix;
import Client.View.LobbyForm;
import Client.View.RequestForm;
import Server.Model.Message.ClientMessage;
import Server.Model.UserTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import javax.swing.JOptionPane;

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
    private ResponseHandler rh;
    private ArrayList requestList = new ArrayList();

    public LobbyControl(String username, ConnectThread ct, clientRun main, UserTable ut, SocketChannel sk, ResponseHandler rh) {

        this.username = username;
        this.sk = sk;
        this.ct = ct;
        this.main = main;
        lf = new LobbyForm();
        lf.setLabelName(username);
        lf.setVisible(true);
        lf.setTable(ut);
        rh.setFrameToShow(lf);
        lf.addLogoutListener(new LogoutListener());
        lf.addChallengeListener(new ChallengeListener());
    }

    private static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public void show(String user, int status) {
        String msg = "";
        if (status == 1) {
            msg = "Bạn đã chiến thắng " + user + ", bạn có muốn đấu lại?";
        } 
        else if (status == -1)
        {
            msg = "Bạn đã thua " + user + ", bạn có muốn đấu lại?";
        }
        else if (status == 0)
        {
            msg = "Bạn đã hòa " + user + ", bạn có muốn đấu lại?";
        }
        int choice = JOptionPane.showConfirmDialog(lf, msg, "AmongChu", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            ClientMessage sm = new ClientMessage(ClientMessage.REQUEST.CHALLENGE, user);
            try {
                ct.send(serialize(sm), sk);
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
        lf.setVisible(true);
    }

    public void forceShow() {
        lf.setVisible(true);
    }

    private class LogoutListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                //ClientMessage cm = new ClientMessage(ClientMessage.REQUEST.LOGOUT, null);
                //ct.send(serialize(cm), sk);
                ct.closeConnect(sk);
                Thread.sleep(100);
                main.toLogin();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    private class ChallengeListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String user = lf.getUserNameSelectedRow();
            if (user == null) {
                JOptionPane.showMessageDialog(lf, "Vui lòng chọn đối thủ");
                return;
            }
            ClientMessage sm = new ClientMessage(ClientMessage.REQUEST.CHALLENGE, user);
            try {
                ct.send(serialize(sm), sk);
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }

    }

    public void newRequest(String user) {
        RequestForm rf = new RequestForm(this);
        requestList.add(rf);
        rf.setUser(user);
        rf.setVisible(true);
    }

    public void confirmRequest(String user) {
        for (Object o : requestList) {
            RequestForm rf = (RequestForm) o;
            rf.close();
        }
        requestList.clear();
        ClientMessage sm = new ClientMessage(ClientMessage.REQUEST.CHALLENGE, user);
        try {
            ct.send(serialize(sm), sk);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    public void createMatch(Matrix matrix) {
        for (Object o : requestList) {
            RequestForm rf = (RequestForm) o;
            rf.close();
        }
        requestList.clear();
        lf.setVisible(false);
        main.toGame(matrix);
    }

    @Override
    protected void finalize() {
        lf.dispose();
    }

    public void close() {
        this.finalize();
    }
}
