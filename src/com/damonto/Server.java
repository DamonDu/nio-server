package com.damonto;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Server {

    private SocketAccepter socketAccepter = null;
    private SocketProcessor socketProcessor = null;
    private IMessageProcessor messageProcessor = null;
    private IMessageReaderFactory messageReaderFactory = null;

    private int tcpPort = 0;

    public Server(int tcpPort, IMessageProcessor messageProcessor, IMessageReaderFactory messageReaderFactory) {
        this.tcpPort = tcpPort;
        this.messageProcessor = messageProcessor;
        this.messageReaderFactory = messageReaderFactory;
    }

    public void start() {

        try {
            Queue<Socket> socketQueue = new ArrayBlockingQueue<Socket>(1024);
            this.socketAccepter = new SocketAccepter(tcpPort, socketQueue);

            MessageBuffer readBuffer = new MessageBuffer();
            MessageBuffer writeBuffer = new MessageBuffer();

            this.socketProcessor = new SocketProcessor(this.messageReaderFactory, this.messageProcessor,
                    readBuffer, writeBuffer, socketQueue);

            Thread accepter = new Thread(socketAccepter);
            Thread processor = new Thread(socketProcessor);

            accepter.run();
            processor.run();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
