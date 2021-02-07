package com.example.demo.model;

import java.util.List;

public class ReceivedMess {
    private CharSequence sender;
    private List<MessByChunk> chunks;


    public ReceivedMess() {}

    public ReceivedMess(CharSequence sender, List<MessByChunk> chunks) {
        this.sender = sender;
        this.chunks = chunks;
    }


    public CharSequence getSender() {
        return sender;
    }

    public void setSender(CharSequence sender) {
        this.sender = sender;
    }

    public List<MessByChunk> getChunk() {
        return chunks;
    }

    public void setChunks(List<MessByChunk> messages) {
        this.chunks = messages;
    }
}
