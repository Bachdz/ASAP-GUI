package com.example.demo.service.asap.management;

import com.example.demo.service.asap.ASAPEngine;
import com.example.demo.service.asap.ASAPException;

import java.io.IOException;
import java.util.Collection;

public class ASAPManagementStorageImpl implements ASAPManagementStorage {
    private final ASAPEngine asapEngine;

    public ASAPManagementStorageImpl(ASAPEngine asapEngine) {
        this.asapEngine = asapEngine;
    }

    @Override
    public void notifyChannelCreated(CharSequence appName, CharSequence channelUri, CharSequence uri,
                                     Collection<CharSequence> recipients) throws ASAPException, IOException {

        byte[] createClosedASAPChannelMessage = ASAPManagementMessage.getCreateClosedASAPChannelMessage(
                this.asapEngine.getOwner(),
                appName,
                uri,
                recipients);

        // put into a channel
        CharSequence newUri = ASAPManagementMessageHandler.createUniqueUri();
        this.asapEngine.add(newUri, createClosedASAPChannelMessage);
        this.asapEngine.setRecipients(newUri, recipients);
    }
}
