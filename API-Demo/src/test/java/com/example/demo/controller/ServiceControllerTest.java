package com.example.demo.controller;

import com.example.demo.model.Channel;
import com.example.demo.service.ASAPService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.MockMvcBuilderSupport;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

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
    public void setUp() throws Exception {
        ServiceController serviceController = new ServiceController(asapService);

        mockMvc = MockMvcBuilders.standaloneSetup(serviceController)
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


    }

    @Test(expected = Exception.class)
    public void createPeerException() throws Exception {
        //expect exception to be thrown if peer query parameter is blank string"
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/peer?name=")
        );
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
    }


    @Test(expected = Exception.class)
    public void createAppException() throws Exception {
        //expect exception to be thrown if peer query parameter is blank string"
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/asap/app?peer=&&app=")
        );
    }

    @Test
    public void createChannel() throws Exception {


        Object randomObj = new Object() {
            public final String uri = "uri://test";
            public final Set<CharSequence> recipients = new HashSet<CharSequence>(Arrays.asList("test1", "test2"));
        };
        String json = objectMapper.writeValueAsString(randomObj);

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
    public void createMessages() {

    }

    @Test
    public void terminateConnection() {
    }

    @Test
    public void terminateServer() {
    }

    @Test
    public void openConnection() {
    }

    @Test
    public void doConnect() {
    }

    @Test
    public void getStart() {
    }

    @Test
    public void getPeers() {
    }

    @Test
    public void getLogData() {
    }

    @Test
    public void getReceivedMess() {
    }

    @Test
    public void resetPeers() {
    }

    @Test
    public void doActivateOnlineMess() {
    }

    @Test
    public void doDeactivateOnlineMess() {
    }

    @Test
    public void doSetSendReceived() {
    }

    @Test
    public void doGetSendReceived() {
    }

    @Test
    public void getStorages() {
    }

    @Test
    public void getEras() {
    }

    @Test
    public void getChannels() {
    }

    @Test
    public void getMessages() {
    }
}