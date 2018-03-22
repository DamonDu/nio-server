package com.damonto.example;

import com.damonto.IMessageProcessor;
import com.damonto.Message;
import com.damonto.Server;
import com.damonto.http.HttpMessageReaderFactory;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: 38\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n" +
                "<html><body>Hello World!</body></html>";

        byte[] httpResponseBytes = httpResponse.getBytes("UTF-8");

        IMessageProcessor messageProcessor = (request, writeProxy) -> {
            System.out.println("Message Received from socket: " + request.getSocketId());

            Message response = writeProxy.getMessage();
            response.setSocketId(request.getSocketId());
            response.writeToMessage(httpResponseBytes);

            writeProxy.enqueue(response);
        };

        Server server = new Server(9999, messageProcessor, new HttpMessageReaderFactory());

        server.start();

    }


}

