package com.damonto.http;

import com.damonto.IMessageReader;
import com.damonto.IMessageReaderFactory;

public class HttpMessageReaderFactory implements IMessageReaderFactory {

    public HttpMessageReaderFactory() {
    }

    @Override
    public IMessageReader getMessageReader() {
        return new HttpMessageReader();
    }
}