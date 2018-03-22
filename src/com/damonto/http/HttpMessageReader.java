package com.damonto.http;


import com.damonto.IMessageReader;
import com.damonto.Message;
import com.damonto.MessageBuffer;
import com.damonto.Socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class HttpMessageReader implements IMessageReader {

    private MessageBuffer messageBuffer    = null;

    private List<Message> completeMessages = new ArrayList<Message>();
    private Message       nextMessage      = null;

    public HttpMessageReader() {
    }

    @Override
    public void init(MessageBuffer readMessageBuffer) {
        this.messageBuffer        = readMessageBuffer;
        this.nextMessage          = messageBuffer.getMessage();
        this.nextMessage.setMetaData(new HttpHeaders());
    }

    @Override
    public void read(Socket socket, ByteBuffer byteBuffer) throws IOException {
        int bytesRead = socket.read(byteBuffer);
        byteBuffer.flip();

        if(byteBuffer.remaining() == 0){
            byteBuffer.clear();
            return;
        }

        this.nextMessage.writeToMessage(byteBuffer);

        int endIndex = HttpUtil.parseHttpRequest(this.nextMessage.getSoureBytesArray(), this.nextMessage.getOffset(),
                this.nextMessage.getOffset() + this.nextMessage.getLength(), (HttpHeaders) this.nextMessage.getMetaData());
        if(endIndex != -1){
            Message message = this.messageBuffer.getMessage();
            message.setMetaData(new HttpHeaders());

            message.writePartialMessage(nextMessage, endIndex);

            completeMessages.add(nextMessage);
            nextMessage = message;
        }
        byteBuffer.clear();
    }


    @Override
    public List<Message> getMessages() {
        return this.completeMessages;
    }

}
