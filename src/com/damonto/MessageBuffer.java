package com.damonto;

public class MessageBuffer {

    public static int KB = 1024;
    public static int MB = 1024 * KB;

    private static final int CAPACITY_SMALL  = 4    * KB;
    private static final int CAPACITY_MEDIUM = 128  * KB;
    private static final int CAPACITY_LARGE  = 1024 * KB;

    private byte[] smallMessageBuffer  = new byte[1024 * 4   * KB];
    private byte[] mediumMessageBuffer = new byte[128  * 128 * KB];
    private byte[] largeMessageBuffer  = new byte[16   * 1   * MB];

    private QueueIntFlip smallMessageBufferFreeBlocks = new QueueIntFlip(1024);
    private QueueIntFlip mediumMessageBufferFreeBlocks = new QueueIntFlip(128);
    private QueueIntFlip largeMessageBufferFreeBlocks = new QueueIntFlip(16);

    public MessageBuffer() {
        for(int i=0; i<smallMessageBuffer.length; i+= CAPACITY_SMALL){
            this.smallMessageBufferFreeBlocks.put(i);
        }
        for(int i=0; i<mediumMessageBuffer.length; i+= CAPACITY_MEDIUM){
            this.mediumMessageBufferFreeBlocks.put(i);
        }
        for(int i=0; i<largeMessageBuffer.length; i+= CAPACITY_LARGE){
            this.largeMessageBufferFreeBlocks.put(i);
        }
    }

    public Message getMessage() {
        int nextBlock = this.smallMessageBufferFreeBlocks.take();

        if (nextBlock == -1) return null;

        Message message = new Message(this);
        message.setSoureBytesArray(this.smallMessageBuffer);
        message.setCapacity(CAPACITY_SMALL);
        message.setOffset(nextBlock);
        message.setLength(0);

        return message;
    }


    public boolean expandMessage(Message message) {

        if (message.getCapacity() == CAPACITY_SMALL) {
            return moveMessage(message, this.smallMessageBufferFreeBlocks, this.mediumMessageBufferFreeBlocks,
                    this.mediumMessageBuffer, CAPACITY_MEDIUM);
        }
        else if (message.getCapacity() == CAPACITY_MEDIUM) {
            return moveMessage(message, this.mediumMessageBufferFreeBlocks, this.largeMessageBufferFreeBlocks,
                    this.largeMessageBuffer, CAPACITY_LARGE);
        }
        else {
            return false;
        }

    }

    private boolean moveMessage(Message message, QueueIntFlip srcQueueFlip, QueueIntFlip destQueueFlip, byte[] dest,
                                int newCapacity) {
        int nextBlock = destQueueFlip.take();

        if (nextBlock == -1) return false;

        System.arraycopy(message.getSoureBytesArray(), message.getOffset(), dest, nextBlock, message.getLength());
        srcQueueFlip.put(message.getOffset());

        message.setSoureBytesArray(dest);
        message.setCapacity(newCapacity);
        message.setOffset(nextBlock);

        return true;
    }
}
