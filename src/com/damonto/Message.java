package com.damonto;

import java.nio.ByteBuffer;

public class Message {

    private long socketId;

    private byte[] soureBytesArray = null;
    private int offset;
    private int capacity;
    private int length;
    private Object metaData;

    private MessageBuffer messageBuffer;

    public Message (MessageBuffer messageBuffer) {
        this.messageBuffer = messageBuffer;
    }

    public void setSocketId(long socketId) {
        this.socketId = socketId;
    }

    public void setSoureBytesArray(byte[] soureBytesArray) {
        this.soureBytesArray = soureBytesArray;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setMetaData(Object metaData) {
        this.metaData = metaData;
    }

    public long getSocketId() {
        return socketId;
    }

    public int getCapacity() {
        return capacity;
    }

    public byte[] getSoureBytesArray() {
        return soureBytesArray;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public Object getMetaData() {
        return metaData;
    }

    public int writeToMessage(ByteBuffer byteBuffer) {
        int remaining = byteBuffer.remaining();

        while (this.length + remaining > this.capacity) {
            if (!this.messageBuffer.expandMessage(this)) {
                return -1;
            }
        }

        int bytesToCopy = Math.min(remaining, this.capacity - this.length);
        byteBuffer.get(this.soureBytesArray, this.offset + this.length, bytesToCopy);
        this.length += bytesToCopy;

        return bytesToCopy;
    }

    public int writeToMessage(byte[] byteArray, int offset, int length) {
        int remaining = length;

        while (this.length + remaining > this.capacity) {
            if (!this.messageBuffer.expandMessage(this)) {
                return -1;
            }
        }

        int bytesToCopy = Math.min(remaining, this.capacity - this.length);
        System.arraycopy(byteArray, offset, this.soureBytesArray,
                this.offset + this.length, bytesToCopy);
        this.length += bytesToCopy;

        return bytesToCopy;
    }

    public int writeToMessage(byte[] byteArray) {
        return writeToMessage(byteArray, 0, byteArray.length);
    }

    public void writePartialMessage(Message message, int lastIndex) {
        int start = message.offset + lastIndex;
        int lengthPartialMessage = message.offset + message.length - lastIndex;
        System.arraycopy(message.soureBytesArray, start, this.soureBytesArray, this.offset,
                lengthPartialMessage);
    }

}
