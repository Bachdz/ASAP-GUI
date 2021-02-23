package com.example.demo.service;

import com.example.demo.service.asap.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                 ASAP API: callbacks ASAPChunkReceived                                  //
////////////////////////////////////////////////////////////////////////////////////////////////////////////
public class ChunkReceivedListener implements ASAPChunkReceivedListener {
    private final String rootFolder;
    private final SimpMessagingTemplate template;



//    private List<ASAPChunkReceivedParameters> receivedList = new ArrayList<>();


    public ChunkReceivedListener(String rootFolder, final SimpMessagingTemplate template) {
        this.rootFolder = rootFolder;
        this.template = template;
    }

    @Override
    public void chunkReceived(String format, String sender, String uri, int era) throws IOException {
        ASAPChunkReceivedParameters received = new ASAPChunkReceivedParameters(format, sender, uri, era);
//        this.receivedList.add(received);
        //notify the user about new received chunk
        this.template.convertAndSend("/received/user", received);
        //notify that there is new message in @uri
        this.template.convertAndSend("/received/message/"+uri, received);
        //notify app that channel should reload
        this.template.convertAndSend("/received/channel/"+format, received);
    }

//    public List<ASAPChunkReceivedParameters> getReceivedList() { return this.receivedList; }

    public class ASAPChunkReceivedParameters {
        private final String format;
        private final String sender;
        private final String uri;
        private final int era;
        private ASAPChunkReceivedParameters(String format, String sender, String uri, int era) {
            System.out.println("ASAPChunkReceivedParameters: chunk received: " + format + " | " + sender + " | " + uri);
            this.format = format;
            this.sender = sender;
            this.uri = uri;
            this.era = era;
        }
        public String getFormat() { return this.format; }
        public String getSender() { return this.sender; }
        public String getUri() { return this.uri; }
        public int getEra() { return this.era; }


    }
}
