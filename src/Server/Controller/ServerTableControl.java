/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Controller;

import Server.Model.Message.ServerMessage;
import Server.Model.ServerDataEvent;
import Server.Model.UserTable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author hoang
 */
public class ServerTableControl extends Thread {

    private DatabaseManager dbm;
    private Set list;
    private ServerReactor sr;

    public ServerTableControl() throws SQLException {
        dbm = new DatabaseManager();
        list = new HashSet();
    }

    public void setSr(ServerReactor sr) {
        this.sr = sr;
    }

    public void addToList(SocketChannel socketChannel) {
        synchronized (list) {
            list.add(socketChannel);
            list.notify();
        }
    }

    public void removeFromList(SocketChannel socketChannel) {
        synchronized (list) {
            list.remove(socketChannel);
            list.notify();
        }
    }

    private static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    @Override
    public void run() {
        ServerDataEvent dataEvent;
        while (true) {
            synchronized (list) {
                try {
                    list.wait(1000);
                } catch (InterruptedException ex) {
                    System.out.println(ex);
                }
                UserTable ut = null;
                try {
                    ut = dbm.getUserTable();
                } catch (SQLException ex) {
                    System.out.println(ex);
                }
                ServerMessage sm = new ServerMessage(ServerMessage.STATUS.S_WARN, ServerMessage.ACTION.NONE, ut, ServerMessage.REQUEST.TABLEDATA);
                byte[] dta = null;
                try {
                    dta = serialize(sm);
                } catch (IOException ex) {
                    System.out.println(ex);
                }
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    sr.send((SocketChannel) (it.next()), dta);
                }
            }
        }
    }
}
