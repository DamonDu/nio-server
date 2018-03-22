package com.damonto;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;

public class SocketAccepter implements Runnable {

    private int tcpPort = 0;
    private ServerSocketChannel serverSocketChannel = null;
    private Queue<Socket> socketQueue = null;

    public SocketAccepter(int tcpPort, Queue socketQueue) {
        this.tcpPort = tcpPort;
        this.socketQueue = socketQueue;
    }

    @Override
    public void run() {
        try {
             this.serverSocketChannel = ServerSocketChannel.open();
             this.serverSocketChannel.bind(new InetSocketAddress(tcpPort));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketQueue.add(new Socket(socketChannel));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
