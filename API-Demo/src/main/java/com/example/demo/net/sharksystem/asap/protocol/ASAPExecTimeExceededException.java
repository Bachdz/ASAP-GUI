package com.example.demo.net.sharksystem.asap.protocol;

import com.example.demo.net.sharksystem.asap.ASAPException;

public class ASAPExecTimeExceededException extends ASAPException {
    public ASAPExecTimeExceededException() {
        super();
    }

    public ASAPExecTimeExceededException(String message) {
        super(message);
    }
}
