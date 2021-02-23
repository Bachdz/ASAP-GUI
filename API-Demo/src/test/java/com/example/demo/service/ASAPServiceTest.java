package com.example.demo.service;

import com.example.demo.service.asap.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ASAPServiceTest {

    private ASAPService asapService=mock(ASAPService.class);
    private ASAPStorage asapStorage=mock(ASAPStorage.class);

//    private System system = mock(System.class);
    private OutputCollector outputCollector = new OutputCollector();


    private  ASAPPeerFS asapPeerFS= mock(ASAPPeerFS.class);
    private  ASAPPeer asapPeer= mock(ASAPPeer.class);
    public static final String PEERS_TEST_ROOT_FOLDER = "src/test/asapPeersTest";

    @Autowired
    private SimpMessagingTemplate template;

    @Test
    void doStart() throws IOException, ASAPException {
//        verify(asapService,  times(1)).doInitializeASAPStorages();
    }
    @Test
    void doCreateASAPPeer() throws IOException, ASAPException {
        String name = "test";
        ChunkReceivedListener chunkReceivedListener = new ChunkReceivedListener(PEERS_TEST_ROOT_FOLDER + "/" + name, template);
        // erwartet dass die Funktion cretaeASAPPeer einmal aufgerufen wird
        verify(asapPeerFS,  times(1)).createASAPPeer(name,PEERS_TEST_ROOT_FOLDER+"/"+name,chunkReceivedListener);
    }

    @Test
    void getCurrentEra() throws IOException, ASAPException {
        String peername="test";
        String appname="testapp";

        verify(asapPeer,  times(1)).getEngineByFormat("testapp");

    }

    @Test
    void doOpen() {
    }

    @Test
    void doConnect() {
    }

    @Test
    void doKillConnectionAttempt() {
    }

    @Test
    void doKillServer() {
    }

    @Test
    void doCreateASAPMessages() {
    }



    @Test
    void getChannels() {
    }

    @Test
    void getConsoleLog() {
    }

    @Test
    void getPeers() {
    }

    @Test
    void getStorages() {
    }

    @Test
    void getMessagesByChunk() {
    }

    @Test
    void getReceivedMessages() {
    }

    @Test
    void getMessages() {
    }

    @Test
    void getASAPPeer() {
    }

    @Test
    void doActivateOnlineMessages() {
    }

    @Test
    void doDectivateOnlineMessages() {
    }

    @Test
    void doSetSendReceivedMessage() {
    }

    @Test
    void doGetSendReceivedMessage() {
    }

    @Test
    void doList() {
    }


    @Test
    void doCreateASAPApp() {
    }

    @Test
    void doCreateASAPChannel() {
    }

    @Test
    void doResetASAPStorages() {
    }

    @Test
    void doInitializeASAPStorages() {
    }

    @Test
    void testDoCreateASAPMessages() {
    }

    @Test
    void testGetCurrentEra() {
    }

    @Test
    void testGetChannels() {
    }

    @Test
    void testGetConsoleLog() {
    }

    @Test
    void testGetPeers() {
    }

    @Test
    void testGetStorages() {
    }

    @Test
    void testGetMessagesByChunk() {
    }

    @Test
    void testGetReceivedMessages() {
    }

    @Test
    void testGetMessages() {
    }

    @Test
    void testGetASAPPeer() {
    }

    @Test
    void testDoActivateOnlineMessages() {
    }

    @Test
    void testDoDectivateOnlineMessages() {
    }

    @Test
    void testDoSetSendReceivedMessage() {
    }

    @Test
    void testDoGetSendReceivedMessage() {
    }

    @Test
    void testDoList() {
    }

    @Test
    void testDoCreateASAPPeer() {
    }

    @Test
    void testDoCreateASAPApp() {
    }

    @Test
    void testDoCreateASAPChannel() {
    }

    @Test
    void testDoResetASAPStorages() {
    }

    @Test
    void testDoInitializeASAPStorages() {
    }

    @Test
    void doPrintAllInformation() {
    }

    @Test
    void doPrintStorageInformation() {
    }

    @Test
    void doPrintChannelInformation() {
    }

    @Test
    void getEngineRootFolderByStorageName() {
    }
}