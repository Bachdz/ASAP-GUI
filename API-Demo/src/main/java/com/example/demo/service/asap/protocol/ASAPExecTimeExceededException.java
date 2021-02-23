package com.example.demo.service.asap.protocol;

import com.example.demo.service.asap.ASAPException;

public class ASAPExecTimeExceededException extends ASAPException {
    public ASAPExecTimeExceededException() {
        super();
    }

    public ASAPExecTimeExceededException(String message) {
        super(message);
    }
}
