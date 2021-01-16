package com.example.demo.model;

public class ConnectionResponse {


    private String peerName;
    private String ip;
    private int port;
    private boolean initialized;

    public ConnectionResponse () {}

    public ConnectionResponse(String peerName, String ip, int port, boolean initialized) {
        this.peerName = peerName;
        this.ip = ip;
        this.port = port;
        this.initialized = initialized;
    }

    public String getPeerName() {
        return peerName;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
