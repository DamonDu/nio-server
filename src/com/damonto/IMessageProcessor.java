package com.damonto;

public interface IMessageProcessor {

    void process(Message message, WriteProxy writeProxy);
}
