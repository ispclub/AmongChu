/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Controller;

import Server.Model.ChangeRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hoang
 */
public class ServerReactor extends Thread {

    private InetAddress interfaceToBind;
    private int port1, port2;
    private ServerQueueRequest sqr;
    private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    private ServerSocketChannel serverChannel, tableChannel;
    private List pendingChange = new LinkedList();
    private Map pendingData = new HashMap();
    private ServerTableControl stc;
    
    public ServerReactor(InetAddress interfaceToBind, int port1, int port2, ServerQueueRequest sqr, ServerTableControl stc) throws IOException {
        this.interfaceToBind = interfaceToBind;
        this.port1 = port1;
        this.port2 = port2;
        this.sqr = sqr;
        this.stc = stc;
        this.selector = this.initSelector();
    }

    private Selector initSelector() throws IOException {
        Selector socketSelector = SelectorProvider.provider().openSelector();
        this.serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        InetSocketAddress isa = new InetSocketAddress(this.interfaceToBind, this.port1);
        serverChannel.socket().bind(isa);
        serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
        //
        this.tableChannel = ServerSocketChannel.open();
        tableChannel.configureBlocking(false);
        InetSocketAddress isa2 = new InetSocketAddress(this.interfaceToBind, this.port2);
        tableChannel.socket().bind(isa2);
        tableChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
        return socketSelector;
    }

    public void send(SocketChannel socket, byte[] data) {
        synchronized (this.pendingChange) {
            // Indicate we want the interest ops set changed
            this.pendingChange.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

            // And queue the data we want written
            synchronized (this.pendingData) {
                List queue = (List) this.pendingData.get(socket);
                if (queue == null) {
                    queue = new ArrayList();
                    this.pendingData.put(socket, queue);
                }
                queue.add(ByteBuffer.wrap(data));
            }
        }

        // Finally, wake up our selecting thread so it can make the required changes
        this.selector.wakeup();
    }

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (this.pendingChange) {
                    Iterator changes = this.pendingChange.iterator();
                    while (changes.hasNext()) {
                        ChangeRequest change = (ChangeRequest) changes.next();
                        switch (change.getType()) {
                            case ChangeRequest.CHANGEOPS:
                                SelectionKey key = change.getSocket().keyFor(this.selector);
                                key.interestOps(change.getOps());
                                break;
                        }
                    }
                    this.pendingChange.clear();
                }
                this.selector.select();
                Iterator selectedKey = this.selector.selectedKeys().iterator();
                while (selectedKey.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKey.next();
                    selectedKey.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        this.accept(key);
                    }
                    else if (key.isReadable()) {
                        this.read(key);
                    }
                    else if (key.isWritable())
                    {
                        this.write(key);
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        //Socket socket = socketChannel.socket();
        socketChannel.configureBlocking(false);
        if (socketChannel.socket().getLocalPort()== this.port2)
        {
            stc.addToList(this, socketChannel);
        }
        socketChannel.register(this.selector, SelectionKey.OP_READ);

        //need to create new game handler here
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        this.readBuffer.clear();
        int numRead;
        try {
            numRead = socketChannel.read(this.readBuffer);
        } catch (IOException ex) {
            key.cancel();
            socketChannel.close();
            //remove client online status from database
            this.sqr.processData(this, socketChannel, null, -1);
            System.out.println("Client disconnected");
            return;
        }
        if (numRead == -1) {
            key.cancel();
            socketChannel.close();
            System.out.println("Client disconnected");
            //remove client online status from database
            this.sqr.processData(this, socketChannel, null, -1);
            return;
        }
        this.sqr.processData(this, socketChannel, this.readBuffer.array(), numRead);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        synchronized (this.pendingData)
        {
            List queue = (List)this.pendingData.get(socketChannel);
            while (!queue.isEmpty())
            {
                ByteBuffer buf = (ByteBuffer)queue.get(0);
                socketChannel.write(buf);
                if (buf.remaining() > 0)
                {
                    //some stuf here or raise new exception
                    break;
                }
                queue.remove(0);
            }
            if (queue.isEmpty())
            {
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }
}
