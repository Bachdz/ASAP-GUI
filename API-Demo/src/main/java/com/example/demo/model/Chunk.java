package com.example.demo.model;

import java.util.List;

public class Chunk {
    private int era;
    private List<CharSequence> messages;

    public Chunk() {
    }

    public Chunk(int era, List<CharSequence> messages) {
        this.era = era;
        this.messages = messages;
    }


    public int getEra() {
        return era;
    }

    public void setEra(int era) {
        this.era = era;
    }

    public List<CharSequence> getMessages() {
        return messages;
    }

    public void setMessages(List<CharSequence> messages) {
        this.messages = messages;
    }
}
