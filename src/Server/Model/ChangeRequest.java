/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Model;

import java.nio.channels.SocketChannel;

/**
 *
 * @author hoang
 */
public class ChangeRequest {
    public static final int REGISTER = 1;
    public static final int CHANGEOPS = 2;
    public static final int CLOSE = 3;
    private SocketChannel socket;
    private int type;
    private int ops;

    public ChangeRequest(SocketChannel socket, int type, int ops) {
        this.socket = socket;
        this.type = type;
        this.ops = ops;
    }

    public SocketChannel getSocket() {
        return socket;
    }

    public int getType() {
        return type;
    }

    public int getOps() {
        return ops;
    }

    public void setSocket(SocketChannel socket) {
        this.socket = socket;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setOps(int ops) {
        this.ops = ops;
    }
}
