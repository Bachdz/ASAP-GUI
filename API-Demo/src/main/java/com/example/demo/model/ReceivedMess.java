package com.example.demo.model;

import java.util.List;

public class ReceivedMess {
    private CharSequence sender;
    private List<CharSequence> messages;


    public ReceivedMess() {}

    public ReceivedMess(CharSequence sender, List<CharSequence> messages) {
        this.sender = sender;
        this.messages = messages;
    }


    public CharSequence getSender() {
        return sender;
    }

    public void setSender(CharSequence sender) {
        this.sender = sender;
    }

    public List<CharSequence> getMessages() {
        return messages;
    }

    public void setMessages(List<CharSequence> messages) {
        this.messages = messages;
    }
}
