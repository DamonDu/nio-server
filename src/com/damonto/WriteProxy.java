package com.damonto;

import java.util.Queue;

public class WriteProxy {

    private Queue<Message> messageQueue;
    private MessageBuffer writeBuffer;

    public WriteProxy(MessageBuffer messageBuffer, Queue messageQueue) {
        this.messageQueue = messageQueue;
        this.writeBuffer = messageBuffer;
    }

    public Message getMessage() {
        return this.writeBuffer.getMessage();
    }

    public boolean enqueue (Message message) {
        return this.messageQueue.offer(message);
    }
}
