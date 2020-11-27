/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;


/**
 *
 * @author hoang
 */
public class Utils {
    public static final int WINDOW_WIDTH = 720;
    public static final int WINDOW_HEIGHT = 460;
    public static final String DEFAULT_FONT = "Shree Devanagari 714";
    public static final int MAP_ROW = 10;
    public static final int MAP_COL = 10;
    public static final int PIKACHU_NUMBER = 25;
    public static final boolean DEBUG = true;
    public static final String BT_QUIT = "quit";
    public static final int port1 = 12346;
    public static final int port2 = 12347;
    public static final String host = "34.92.139.146";

    public static void debug(Class clz,String debug){
        if (DEBUG){
            debug = debug == null ? "Null debug string!" : debug;
            String name = clz.getCanonicalName()==null?"Debug": clz.getCanonicalName();
            System.out.println(name+":"+debug);
        }
    }
}
