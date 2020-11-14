/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.ServerMain;

import Server.Controller.DatabaseManager;
import Server.Controller.Reactor;
import java.io.IOException;
import java.sql.SQLException;

/**
 *
 * @author hoang
 */
public class Main {
    public static void main(String[] args) throws IOException, SQLException {
        DatabaseManager dbm = new DatabaseManager();
        Reactor r = new Reactor(12346, dbm);
        r.run();
    }
}
