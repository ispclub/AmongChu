/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Model;

import Client.Controller.ConnectThread;
import java.nio.channels.SocketChannel;

/**
 *
 * @author hoang
 */
public class ClientDataEvent {

    private ConnectThread connectThread;
    private SocketChannel socket;
    public byte[] data;

    public ClientDataEvent() {
    }

    public ClientDataEvent(ConnectThread connectThread, SocketChannel socket, byte[] data) {
        this.connectThread = connectThread;
        this.socket = socket;
        this.data = data;
    }

    public ConnectThread getConnectThread() {
        return connectThread;
    }

    public SocketChannel getSocket() {
        return socket;
    }

    public byte[] getData() {
        return data;
    }

    public void setConnectThread(ConnectThread connectThread) {
        this.connectThread = connectThread;
    }

    public void setSocket(SocketChannel socket) {
        this.socket = socket;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
