/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Controller;

import Server.Model.ChangeRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hoang
 */
public class ConnectThread extends Thread{
    private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    private List pendingChanges = new LinkedList();
    private Map pendingData = new HashMap();
    private ResponseHandler rspHandler;
    public ConnectThread(ResponseHandler rspHandler) throws IOException {
        this.rspHandler = rspHandler;
        this.selector = this.initSelector();
    }

    private Selector initSelector() throws IOException {
        return SelectorProvider.provider().openSelector();
    }
    
    public void closeConnect(SocketChannel sc) throws IOException
    {
        synchronized (this.pendingChanges)
        {
            this.pendingChanges.add(new ChangeRequest(sc, ChangeRequest.CLOSE, 0));
        }
        this.selector.wakeup();
    }
    
    public void send(byte[] data, SocketChannel socketChannel)
    {
        synchronized (this.pendingChanges)
        {
            this.pendingChanges.add(new ChangeRequest(socketChannel, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
            synchronized (this.pendingData)
            {
                List queue = (List) this.pendingData.get(socketChannel);
                if (queue == null)
                {
                    queue = new ArrayList();
                    this.pendingData.put(socketChannel, queue);
                }
                queue.add(ByteBuffer.wrap(data));
            }
        }
        this.selector.wakeup();
    }
    @Override
    public void run() {
        while (true)
        {
            try
            {
                synchronized (this.pendingChanges)
                {
                    Iterator changes = this.pendingChanges.iterator();
                    while (changes.hasNext())
                    {
                        ChangeRequest change = (ChangeRequest)changes.next();
                        switch (change.getType())
                        {
                            case ChangeRequest.CHANGEOPS:
                                SelectionKey key = change.getSocket().keyFor(this.selector);
                                key.interestOps(change.getOps());
                                break;
                            case ChangeRequest.REGISTER:
                                change.getSocket().register(this.selector, change.getOps());
                                break;
                            case ChangeRequest.CLOSE:
                                change.getSocket().close();
                                //change.getSocket().keyFor(this.selector).cancel();
                        }
                        //ChangeRequest change = (ChangeRequest)changes.next();
                    }
                    this.pendingChanges.clear();
                }
                this.selector.select();
                Iterator selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext())
                {
                    SelectionKey key = (SelectionKey)selectedKeys.next();
                    selectedKeys.remove();
                    if (!key.isValid())
                    {
                        continue;
                    }
                    if (key.isConnectable())
                    {
                        this.finishConection(key);
                    }
                    if (key.isReadable())
                    {
                        this.read(key);
                    }
                    if (key.isWritable())
                    {
                        this.write(key);
                    }
                }
            } catch (ClosedChannelException ex) {
                Logger.getLogger(ConnectThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ConnectThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void finishConection(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            socketChannel.finishConnect();
        } catch (IOException e) {
            // Cancel the channel's registration with our selector
            System.out.println(e);
            key.cancel();
            return;
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        this.readBuffer.clear();
        int numRead;
        try
        {
            numRead = socketChannel.read(this.readBuffer);
        }catch (IOException ex)
        {
            key.cancel();
            socketChannel.close();
            //add thông báo here
            return;
        }
        if (numRead == -1)
        {
            key.channel().close();
            key.cancel();
            //add thông báo here
            return;
        }
        rspHandler.processData(this, socketChannel, this.readBuffer.array(), numRead);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        synchronized (this.pendingData) {
            List queue = (List) this.pendingData.get(socketChannel);

            while (!queue.isEmpty()) {
                ByteBuffer buf = (ByteBuffer) queue.get(0);
                socketChannel.write(buf);
                if (buf.remaining() > 0) {
                    //exception here
                    break;
                }
                queue.remove(0);
            }
            if (queue.isEmpty()) {
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }
    
    public SocketChannel initConnection(InetAddress hostAddress, int port) throws IOException
    {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(hostAddress, port));
        synchronized(this.pendingChanges)
        {
            this.pendingChanges.add(new ChangeRequest(socketChannel, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
        }
        this.selector.wakeup();
        return socketChannel;
    }
    
}
