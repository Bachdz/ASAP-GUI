package com.example.demo.service.asap;

import com.example.demo.service.asap.protocol.ASAPConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ASAPConnectionHandler {
    ASAPConnection handleConnection(InputStream is, OutputStream os) throws IOException, ASAPException;
}
