package com.damonto;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Socket {

    private SocketChannel socketChannel;
    private MessageWriter messageWriter;
    private IMessageReader messageReader;
    private boolean endOfStreamRead = false;

    private long socketId;

    public Socket(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public void prepareSocket(long socketId, MessageWriter messageWriter,
                          IMessageReader messageReader, MessageBuffer readBuffer)  throws IOException{

        this.socketChannel.configureBlocking(false);

        this.socketId = socketId;
        this.messageWriter = messageWriter;
        this.messageReader = messageReader;

        this.messageReader.init(readBuffer);
    }

    public long getSocketId() {
        return socketId;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public IMessageReader getMessageReader() {
        return messageReader;
    }

    public MessageWriter getMessageWriter() {
        return messageWriter;
    }

    public boolean ifReadEnd() {
        return this.endOfStreamRead;
    }

    public int read(ByteBuffer byteBuffer) throws IOException{
        int bytesRead = this.socketChannel.read(byteBuffer);
        int totalBytesRead = bytesRead;

        while (bytesRead > 0) {
            bytesRead = this.socketChannel.read(byteBuffer);
            totalBytesRead += bytesRead;
        }
        if (bytesRead == -1) {
            this.endOfStreamRead = true;
        }

        return totalBytesRead;
    }

    public int write(ByteBuffer byteBuffer) throws IOException {
        int byteWritten = this.socketChannel.write(byteBuffer);
        int totalBytesWritten = byteWritten;

        while (byteWritten > 0) {
            byteWritten = this.socketChannel.write(byteBuffer);
            totalBytesWritten += byteWritten;
        }

        return totalBytesWritten;
    }
}
