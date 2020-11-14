/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Controller;

import Client.ClientMain.Main;
import Client.View.LobbyForm;
import Server.Model.UserTable;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;

/**
 *
 * @author hoang
 */
public class TableControlThread extends Thread{
    private JTable table;
    private ConnectThread ct;
    private Main main;
    private JFrame frame;
    private UserTable ut;
    private String user;
    
    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public void setUt(UserTable ut) {
        this.ut = ut;
    }
    public TableControlThread(JTable table, ConnectThread ct, Main main, JFrame frame, String user) {
        this.table = table;
        this.ct = ct;
        this.main = main;
        this.frame = frame;
        this.user = user;
    }

    public TableControlThread() {
    }

    public JTable getTable() {
        return table;
    }

    public ConnectThread getCt() {
        return ct;
    }

    public Main getMain() {
        return main;
    }

    public void setTable(JTable table) {
        this.table = table;
    }

    public void setCt(ConnectThread ct) {
        this.ct = ct;
    }

    public void setMain(Main main) {
        this.main = main;
    }
    
    @Override
    public void run()
    {
        LobbyForm lbf = (LobbyForm)frame;
        lbf.setLabelName(user);
        while (true)
        {
            try {
                ct.getUserTable();
                //Cast to jTable table;
                sleep(1000);
                //lbf.clearTable();
                lbf.setTable(ut);
            } catch (IOException | InterruptedException ex) {
                JOptionPane.showMessageDialog(frame, "Hiển thị danh sách thất bại");
            }
        }
    }
    
}
