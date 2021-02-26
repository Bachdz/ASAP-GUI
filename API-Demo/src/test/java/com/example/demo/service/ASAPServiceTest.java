package com.example.demo.service;

import com.example.demo.service.asap.*;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class ASAPServiceTest {

    private ASAPStorage asapStorage=mock(ASAPStorage.class);

    private OutputCollector outputCollector = new OutputCollector();

    private  ASAPPeer asapPeer= mock(ASAPPeer.class);



    @Autowired
    private ASAPService serviceTest;

    @Before
    public void setUp() throws IOException, ASAPException {
//        serviceTest.doStart();
    }

    @Autowired
    private SimpMessagingTemplate template;

    @Test
    void doStart() throws IOException, ASAPException {
//        serviceTest.doStart();
        ASAPService asapService=mock(ASAPService.class);

        asapService.doStart();

        verify(asapService,  times(1)).doStart();

    }


    @Test
    void doCreateASAPPeer() throws IOException, ASAPException {
        String PEERS_TEST_ROOT_FOLDER = "src/test/asapPeersTest";
        String name = "test123456";


        ASAPPeerFS asapPeerFS= mock(ASAPPeerFS.class);

        serviceTest.doCreateASAPPeer("test1345");


        ChunkReceivedListener chunkReceivedListener = new ChunkReceivedListener(PEERS_TEST_ROOT_FOLDER + "/" + name, template);

//        when(asapPeerFS.createASAPPeer(name,PEERS_TEST_ROOT_FOLDER+"/"+name,chunkReceivedListener)).thenReturn(null);


        // erwartet dass die Funktion cretaeASAPPeer aufgerufen wird
        verify(asapPeerFS).createASAPPeer(name,PEERS_TEST_ROOT_FOLDER+"/"+name,chunkReceivedListener);
//        verify(asapPeerFS).newEra();
    }

//    @Test
//    void getCurrentEra() throws IOException, ASAPException {
//        String peerName="test";
//        String appName="testapp";
//        ASAPPeer asapPeer= mock(ASAPPeer.class);
//
//        when(serviceTest.getCurrentEra(peerName,appName)).thenReturn(2);
//
////        serviceTest.getCurrentEra(peerName,appName);
//
//        assertEquals(2,serviceTest.getCurrentEra(peerName,appName));
//
//
//
////        verify(asapPeer,  times(1)).getEngineByFormat(appName);
//
//    }

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