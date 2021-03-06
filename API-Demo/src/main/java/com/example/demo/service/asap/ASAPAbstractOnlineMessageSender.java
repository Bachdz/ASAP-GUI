package com.example.demo.service.asap;

import java.io.IOException;
import java.util.*;

public abstract class ASAPAbstractOnlineMessageSender implements ASAPOnlineMessageSender {
    private ASAPStorage source = null;

    public void attachToSource(ASAPStorage source) {
        source.attachASAPMessageAddListener(this);
    }

    public void detachFromStorage() {
        if(this.source != null) {
            this.source.detachASAPMessageAddListener();
        }
    }

    public void sendASAPAssimilateMessage(CharSequence format, CharSequence uri, CharSequence recipient,
                                          byte[] messageAsBytes, int era) throws IOException, ASAPException {
        if(recipient == null) {
            this.sendASAPAssimilateMessage(format, uri, messageAsBytes, era);
        } else {
            Set<CharSequence> recipients = new HashSet<>();
            recipients.add(recipient);
            this.sendASAPAssimilateMessage(format, uri, recipients, messageAsBytes, era);
        }
    }
}
