/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Controller;

import java.io.IOException;
import java.sql.SQLException;

/**
 *
 * @author hoang
 */
public class serverRun {
    public static void main(String[] args) throws SQLException, IOException {
        ServerQueueRequest sqr = new ServerQueueRequest();
        ServerTableControl stc = new ServerTableControl();
        ServerReactor sr = new ServerReactor(null, 12346, 12347, sqr, stc);
        new Thread(sqr).start();
        stc.setSr(sr);
        sr.start();
        stc.start();
    }
}
