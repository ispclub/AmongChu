/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Model;

import Server.Controller.ServerReactor;
import java.nio.channels.SocketChannel;

/**
 *
 * @author hoang
 */
public class ServerDataEvent {
    private ServerReactor serverReactor;
    private SocketChannel socket;
    public byte[] data;

    public ServerDataEvent(ServerReactor serverReactor, SocketChannel socket, byte[] data) {
        this.serverReactor = serverReactor;
        this.socket = socket;
        this.data = data;
    }

    public ServerReactor getServerReactor() {
        return serverReactor;
    }

    public SocketChannel getSocket() {
        return socket;
    }

    public byte[] getData() {
        return data;
    }

    public void setServerReactor(ServerReactor serverReactor) {
        this.serverReactor = serverReactor;
    }

    public void setSocket(SocketChannel socket) {
        this.socket = socket;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
    
}
