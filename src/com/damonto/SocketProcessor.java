package com.damonto;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;

public class SocketProcessor implements Runnable {

    private IMessageReaderFactory messageReaderFactory;
    private IMessageProcessor messageProcessor;
    private MessageBuffer readBuffer;
    private MessageBuffer writeBuffer;

    private Map<Long, Socket> socketMap;

    private Queue<Socket> inboundSocketQueue;
    private Queue<Message> outboundMessageQueue = new LinkedList<>();
    private WriteProxy writeProxy;

    private Selector readSelector;
    private Selector writeSelector;

    private static long nextSocketId = 16 * 1024 - 1;

    private ByteBuffer readByteBuffer = ByteBuffer.allocate(1024 * 1024);
    private ByteBuffer writeByteBuffer = ByteBuffer.allocate(1024 * 1024);

    private Set<Socket> emptyToNotEmptySockets = new HashSet<>();
    private Set<Socket> notEmptyToEmptySockets = new HashSet<>();


    public SocketProcessor(IMessageReaderFactory messageReaderFactory, IMessageProcessor messageProcessor,
                           MessageBuffer readBuffer, MessageBuffer writeBuffer, Queue inboundSocketQueue)
    throws IOException{

        this.messageReaderFactory = messageReaderFactory;
        this.messageProcessor = messageProcessor;
        this.readBuffer = readBuffer;
        this.writeBuffer = writeBuffer;

        this.socketMap = new HashMap();

        this.inboundSocketQueue = inboundSocketQueue;
        this.writeProxy = new WriteProxy(writeBuffer, this.outboundMessageQueue);

        this.readSelector = Selector.open();
        this.writeSelector = Selector.open();

    }


    @Override
    public void run() {
        while (true) {
            try {
                excuteCycle();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                //TODO: ?
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void excuteCycle() throws IOException{
        takeNewSockets();
        readMsgFromSockets();
        writeMsgToSockets();
    }

    public void takeNewSockets() throws IOException {
        Socket socket = inboundSocketQueue.poll();
        while (socket != null) {

            socket.prepareSocket(getNextSocketId(), new MessageWriter(),
                    this.messageReaderFactory.getMessageReader(), this.readBuffer);

            socketMap.put(socket.getSocketId(), socket);

            SelectionKey key = socket.getSocketChannel().register(this.readSelector,
                    SelectionKey.OP_READ);
            key.attach(socket);

            socket = inboundSocketQueue.poll();
        }
    }

    public void readMsgFromSockets() throws IOException {
        if(this.readSelector.selectNow() > 0) {
            Set<SelectionKey> selectionKeys = this.readSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                readMsgFromSocket(key);
                keyIterator.remove();
            }
        }
    }

    public void readMsgFromSocket(SelectionKey key) throws IOException {
        Socket socket = (Socket) key.attachment();

        IMessageReader reader = socket.getMessageReader();
        reader.read(socket, this.readByteBuffer);
        List<Message> messageList = reader.getMessages();

        if (messageList.size() > 0) {
            for (Message m : messageList) {
                m.setSocketId(socket.getSocketId());
                this.messageProcessor.process(m, this.writeProxy);
            }
            messageList.clear();
        }

        if (socket.ifReadEnd()) {
            System.out.println("Socket Closed:" + socket.getSocketId());
            this.socketMap.remove(socket.getSocketId());
            key.attach(null);
            key.cancel();
            key.channel().close();
        }
    }

    public void writeMsgToSockets() throws IOException {
        takeNewOutBoundMessage();
        cancelEmptySockets();
        reigisNotEmptySockets();

        int writeReady = this.writeSelector.selectNow();
        if (writeReady > 0) {
            Set<SelectionKey> selectionKeys = this.writeSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                Socket socket = (Socket) key.attachment();

                socket.getMessageWriter().write(socket, this.writeByteBuffer);

                if (socket.getMessageWriter().isEmpty()) {
                    this.notEmptyToEmptySockets.add(socket);
                }

                keyIterator.remove();
            }

            selectionKeys.clear();
        }
    }


    public void takeNewOutBoundMessage () {
        Message message = this.outboundMessageQueue.poll();

        while (message != null) {
            Socket socket = this.socketMap.get(message.getSocketId());

            if (socket != null) {
                MessageWriter messageWriter = socket.getMessageWriter();
                if (messageWriter.isEmpty()) {
                    notEmptyToEmptySockets.remove(socket);
                    emptyToNotEmptySockets.add(socket);
                }
                messageWriter.enqueue(message);
            }

            message = this.outboundMessageQueue.poll();
        }
    }

    public void cancelEmptySockets() {
        for (Socket socket : notEmptyToEmptySockets) {
            SelectionKey key = socket.getSocketChannel().keyFor(this.writeSelector);
            key.cancel();
        }
        notEmptyToEmptySockets.clear();
    }

    private void reigisNotEmptySockets() throws IOException{
        for (Socket socket : emptyToNotEmptySockets) {
            socket.getSocketChannel().register(this.writeSelector, SelectionKey.OP_WRITE, socket);
        }
        emptyToNotEmptySockets.clear();
    }

    public static long getNextSocketId() {
        return ++nextSocketId;
    }
}
