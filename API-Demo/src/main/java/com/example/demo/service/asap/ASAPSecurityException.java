package com.example.demo.service.asap;

public class ASAPSecurityException extends ASAPException {
    public ASAPSecurityException() {
        super();
    }
    public ASAPSecurityException(String message) {
        super(message);
    }
    public ASAPSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
