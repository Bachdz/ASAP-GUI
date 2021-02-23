package com.example.demo.service.asap.sharknet;

import com.example.demo.service.asap.ASAPConnectionHandler;
import com.example.demo.service.asap.ASAPException;

import java.io.IOException;

public interface SharkNetPeer extends ASAPConnectionHandler {
    CharSequence getOwnerID();

    void sendSharkNetMessage(byte[] message, CharSequence topic, CharSequence recipient,
                                    boolean sign, boolean encrypted) throws IOException, ASAPException;
    void addSharkNetMessageListener(SharkNetMessageListener snLister);
    void removeSharkNetMessageListener(SharkNetMessageListener snLister);
}
