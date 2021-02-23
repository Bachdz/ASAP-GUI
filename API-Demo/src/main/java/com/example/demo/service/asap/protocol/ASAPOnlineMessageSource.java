package com.example.demo.service.asap.protocol;

import java.io.IOException;
import java.io.OutputStream;

public interface ASAPOnlineMessageSource {
    void sendStoredMessages(ASAPConnection asapConnection, OutputStream os) throws IOException;
}
