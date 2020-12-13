package com.example.demo.model;

import java.util.List;

public class StorageResponse {

    private List<Storage> data;

    private List<String> messages;

    public StorageResponse(List<Storage> data, List<String> messages) {
        this.data = data;
        this.messages = messages;
    }

    public List<Storage> getData() {
        return data;
    }

    public void setData(List<Storage> data) {
        this.data = data;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
