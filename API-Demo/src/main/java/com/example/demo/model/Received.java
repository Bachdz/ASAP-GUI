package com.example.demo.model;

import java.util.List;

public class Received {
    private CharSequence sender;
    private List<Chunk> chunks;


    public Received() {}

    public Received(CharSequence sender, List<Chunk> chunks) {
        this.sender = sender;
        this.chunks = chunks;
    }


    public CharSequence getSender() {
        return sender;
    }

    public void setSender(CharSequence sender) {
        this.sender = sender;
    }

    public List<Chunk> getChunk() {
        return chunks;
    }

    public void setChunks(List<Chunk> messages) {
        this.chunks = messages;
    }
}
