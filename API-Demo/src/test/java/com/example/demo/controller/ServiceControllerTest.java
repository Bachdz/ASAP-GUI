package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.service.ASAPService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.*;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;


@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ServiceControllerTest {
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;


    @Mock
    private ASAPService asapService;

    @InjectMocks
    private ServiceController serviceController;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(new ServiceController(asapService))
                .build();
    }

    @Test
    public void createPeer() throws Exception {
        // test proper action, expect 200 and json content
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/peer?name=test")
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("{'name':'test'}"));

        // test perform action without query variable, expect bad request status 400
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/peer")
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(asapService,times(1)).doCreateASAPPeer("test");


    }

    @Test(expected = Exception.class)
    public void createPeerException() throws Exception {
        //expect exception to be thrown if peer query parameter is blank string"
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/peer?name=")
        );

        verify(asapService,times(0)).doCreateASAPPeer("");
    }


    @Test
    public void createApp() throws Exception {
        // test proper action, expect 200 and json content
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/app?peer=test&&app=test")
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("{'name':'test'}"));

        // test perform action without query variable, expect bad request status 400
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/app")
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        //verify method called one time
        verify(asapService,times(1)).doCreateASAPApp("test", "test");

    }


    @Test(expected = Exception.class)
    public void createAppException() throws Exception {
        //expect exception to be thrown if peer query parameter is blank string"
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/app?peer=&&app=")
        );
        verify(asapService,times(0)).doCreateASAPApp("","");
    }

    @Test
    public void createChannel() throws Exception {


        Object randomObj = new Object() {
            public final String uri = "uri://test";
            public final Set<CharSequence> recipients = new HashSet<CharSequence>(Arrays.asList("test1", "test2"));
        };
        String json = objectMapper.writeValueAsString(randomObj);
        // perfrom proper action, expect 200 and posted json object as response
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/channel?peer=test&app=test").content(json).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))

                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(json));

        // test perform action with bad query variable, expect bad request status 400
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/channel?app=test").content(json).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // test perform action without body content, expect bad request status 400
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/channel?peer=test&app=test").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void createMessages() throws Exception {
        Object randomObj = new Object() {
            public final String mess  = "This is a test message";
        };


        String json = objectMapper.writeValueAsString(randomObj);

        // perform proper action, expect 200 and posted json object as response
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/addmessages?peer=test&app=test&uri=test").content(json).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))

                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(json));

        // perform action with bad query variable, expect bad request status 400
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/addmessages?app=test").content(json).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // perform action without body content, expect bad request status 400
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/addmessages?peer=test&app=test&uri=test").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());


    }

    @Test
    public void terminateConnection() throws Exception {
        // perform proper action with correct query variable type, expect 200 and string content true
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/terminateconnection?host=test&port=1234")
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));

        // perform proper action with correct wrong variable type of {port}, expect bad request status 400
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/terminateconnection?host=test&port=string")
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());



        // perform action without query variable, expect bad request status 400
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/terminateconnection")
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        //verify method called one time
        verify(asapService,times(1)).doKillConnectionAttempt("test", 1234);
    }

    @Test
    public void terminateServer() throws Exception {
        // perform proper action with correct query variable type, expect 200 and string content true
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/terminate?port=1234")
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));

        // perform action without query variable, expect bad request status 400
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/terminate")
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        //verify method called one time
        verify(asapService,times(1)).doKillServer("1234");
    }

    @Test
    public void openConnection() throws Exception {
        Object randomObj = new Object() {
            public final String peerName  = "testPeer";
            public final String ip  = "localhost";
            public final int port  = 1234;
            public final boolean initialized  = true;
        };

        ConnectionResponse response = new ConnectionResponse("testPeer","localhost",1234,true);

        String expectedJson = objectMapper.writeValueAsString(randomObj);


        when(asapService.doOpen(1234,"test")).thenReturn(response);


        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/asap/openconnection?port=1234&peer=test")
        )       .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedJson));

        //verify method called one time
        verify(asapService,times(1)).doOpen(1234,"test");

    }

    @Test
    public void doConnect() throws Exception {

        Object randomObj = new Object() {
            public final String peerName  = "testPeer";
            public final String ip  = "testIp";
            public final int port  = 1234;
            public final boolean initialized  = true;
        };

        ConnectionResponse response = new ConnectionResponse("testPeer","testIp",1234,true);

        String expectedJson = objectMapper.writeValueAsString(randomObj);


        when(asapService.doConnect("testIp",1234,"testPeer")).thenReturn(response);


        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/connect?host=testIp&port=1234&peer=testPeer")
        )       .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedJson));

        //verify method called one time
        verify(asapService,times(1)).doConnect("testIp",1234,"testPeer");
    }

    @Test
    public void getStart() throws Exception {
        // perform proper action with correct query variable type, expect 200 and string content true
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/asap/start")
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));
        //verify method called one time
        verify(asapService,times(1)).doStart();
    }

    @Test
    public void getPeers() throws Exception {
        Peer peer1= new Peer("testPeer1");
        Peer peer2= new Peer("testPeer2");


        List<Peer> expectedReturnList = Arrays.asList(peer1, peer2);

        String expectedJson = objectMapper.writeValueAsString(expectedReturnList);

        when(asapService.getPeers()).thenReturn(Arrays.asList("testPeer1","testPeer2"));


        //expect response
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/asap/peers"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedJson));

        //verify method called one time
        verify(asapService,times(1)).getPeers();
    }

    @Test
    public void getLogData() throws Exception {
        List<String> expectedReturnList = Arrays.asList("test log output","test log");

        String expectedJson = objectMapper.writeValueAsString(expectedReturnList);

        when(asapService.getConsoleLog()).thenReturn(Arrays.asList("test log output","test log"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/asap/logdata"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedJson));
        //verify method called one time
        verify(asapService,times(1)).getConsoleLog();
    }

    @Test
    public void getReceivedMess() throws Exception {
        Chunk chunk = new Chunk(1,Arrays.asList("test mess"));
        Chunk chunk1 = new Chunk (2, Arrays.asList("test mess"));

        List<Chunk> chunks = Arrays.asList(chunk, chunk1);

        Received testObj= new Received("testPeer2", chunks);

        List<Received> expectedReturn = Arrays.asList(testObj);

        String expectedJson = objectMapper.writeValueAsString(expectedReturn);

        when(asapService.getReceivedMessages("test","test","test")).thenReturn(expectedReturn);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/asap/received?peer=test&storage=test&uri=test"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedJson));
        //verify method called one time
        verify(asapService,times(1)).getReceivedMessages("test","test","test");
    }

    @Test
    public void resetPeers() throws Exception {
        // perform proper action with correct query variable type, expect 200 and string content true
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/v1/asap/peers")
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));
        //verify method called one time
        verify(asapService,times(1)).doResetASAPStorages();
    }

    @Test
    public void doActivateOnlineMess() throws Exception {
        // perform proper action with correct query variable type, expect 200 and string content true
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/asap/activatemess?peer=test")
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));
        //verify method called one time
        verify(asapService,times(1)).doActivateOnlineMessages("test");


    }

    @Test
    public void doDeactivateOnlineMess() throws Exception {
        // perform proper action with correct query variable type, expect 200 and string content true
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/asap/deactivatemess?peer=test")
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));
        //verify method called one time
        verify(asapService,times(1)).doDectivateOnlineMessages("test");


    }

    @Test
    public void doSetSendReceived() throws Exception {

        // perform proper action with correct query variable type, expect 200 and string content true
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/asap/setsendreceived?peer=test&storage=test&value=true")
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));
        //verify method called one time
        verify(asapService,times(1)).doSetSendReceivedMessage("test","test",true);
    }

    @Test
    public void doGetSendReceived() throws Exception {
        when(asapService.doGetSendReceivedMessage("test","test")).thenReturn(true);

        // perform proper action with correct query variable type, expect 200 and string content true
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/asap/getsendreceived?peer=test&storage=test")
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));
        //verify method called one time
        verify(asapService,times(1)).doGetSendReceivedMessage("test","test");
    }

    @Test
    public void getStorages() throws Exception {
            App app = new App("test");
            App app1 = new App("test1");

            List<App> expectedReturnList = Arrays.asList(app, app1);

            String expectedJson = objectMapper.writeValueAsString(expectedReturnList);

            when(asapService.getStorages("test")).thenReturn(expectedReturnList);


            //expect response
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/asap/storages?peer=test"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().json(expectedJson));

            //verify method called one time
            verify(asapService,times(1)).getStorages("test");
    }

    @Test
    public void getEras() throws Exception {
        when(asapService.getCurrentEra("test","test")).thenReturn(1);

        //expect response
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/asap/era?peer=test&storage=test"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("1"));

        //verify method called one time
        verify(asapService,times(1)).getCurrentEra("test","test");



    }

    @Test
    public void getChannels() throws Exception {
        Set<CharSequence> mockRecipients = new HashSet<CharSequence>(Arrays.asList("test1", "test2"));
        Channel mockChannel = new Channel("test", mockRecipients);

        List<Channel> expectedReturnList = Arrays.asList(mockChannel);

        String expectedJson = objectMapper.writeValueAsString(expectedReturnList);

        when(asapService.getChannels("test","test")).thenReturn(expectedReturnList);


        //expect response
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/asap/channels?peer=test&storage=test"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedJson));

        //verify method called one time
        verify(asapService,times(1)).getChannels("test","test");

    }

    @Test
    public void getMessages() throws Exception{
        Chunk mockChunk = new Chunk(1,Arrays.asList("test mess"));
        Chunk mockChunk1 = new Chunk (2, Arrays.asList("test mess"));

        List<Chunk> expectedReturnList = Arrays.asList(mockChunk,mockChunk1);

        String expectedJson = objectMapper.writeValueAsString(expectedReturnList);

        when(asapService.getMessagesByChunk("test","test","test")).thenReturn(expectedReturnList);


        //expect response
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/asap/messages?peer=test&storage=test&uri=test"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedJson));

        //verify method called one time
        verify(asapService,times(1)).getMessagesByChunk("test","test","test");

    }
}