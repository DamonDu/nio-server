package com.damonto;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MessageWriter {

    private List<Message> writeMessges = new ArrayList<>();
    private Message message;
    private int bytesWritten = 0;

    public void enqueue(Message message) {
        if (this.message == null) {
            this.message = message;
        }
        else {
            this.writeMessges.add(message);
        }
    }

    public void write(Socket socket, ByteBuffer byteBuffer) throws IOException{
        byteBuffer.put(this.message.getSoureBytesArray(), this.message.getOffset() + this.bytesWritten,
                this.message.getLength());
        byteBuffer.flip();
        this.bytesWritten = socket.write(byteBuffer);
        byteBuffer.clear();

        if (bytesWritten >= this.message.getLength()) {
            if (this.writeMessges.size() > 0) {
                this.writeMessges.remove(0);
            }
            else {
                this.writeMessges = null;
            }
        }
    }

    public boolean isEmpty() {
        return this.writeMessges.isEmpty() && this.message == null;
    }
}
