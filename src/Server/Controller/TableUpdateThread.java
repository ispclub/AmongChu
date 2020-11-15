/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Controller;

import Server.Model.UserTable;
import java.sql.SQLException;

/**
 *
 * @author hoang
 */
public class TableUpdateThread {
    private DatabaseManager dbm;
    private Reactor reactor;
    private UserTable lastTable;

    public TableUpdateThread(DatabaseManager dbm, Reactor reactor) throws SQLException {
        this.dbm = dbm;
        this.reactor = reactor;
        lastTable = dbm.getUserTable();
    }
    public void run() throws SQLException, InterruptedException
    {
        while (true)
        {
            UserTable ut = dbm.getUserTable();
            if (!ut.equals(lastTable))
            {
                lastTable = ut;
                reactor.sendToAll(ut);
            }
            Thread.sleep(1000);
        }
    }
}
