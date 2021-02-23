package com.example.demo.service.asap;

import com.example.demo.service.asap.protocol.ASAP_AssimilationPDU_1_0;

interface ASAPCommunicationSetting {
    boolean allowedToCreateChannel(ASAP_AssimilationPDU_1_0 asapAssimilationPDU);

    /**
     * @param on if true - message must be encrypted
     */
    void setSendEncryptedMessages(boolean on);

    /**
     * @param on if true - message must be signed
     */
    void setSendSignedMessages(boolean on);

}
