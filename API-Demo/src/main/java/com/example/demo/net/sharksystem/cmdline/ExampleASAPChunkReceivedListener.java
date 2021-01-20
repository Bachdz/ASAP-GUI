package com.example.demo.net.sharksystem.cmdline;

import com.example.demo.net.sharksystem.asap.ASAPChunkReceivedListener;
import com.example.demo.net.sharksystem.asap.ASAPMessages;
import com.example.demo.net.sharksystem.asap.util.Helper;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                 ASAP API: callbacks ASAPChunkReceived                                  //
////////////////////////////////////////////////////////////////////////////////////////////////////////////
public class ExampleASAPChunkReceivedListener implements ASAPChunkReceivedListener {
    private final String rootFolder;
    private final SimpMessagingTemplate template;



    private List<ASAPChunkReceivedParameters> receivedList = new ArrayList<>();

    public ExampleASAPChunkReceivedListener(String rootFolder, final SimpMessagingTemplate template) {
        this.rootFolder = rootFolder;
        this.template = template;
    }

    @Override
    public void chunkReceived(String format, String sender, String uri, int era) throws IOException {

        ASAPChunkReceivedParameters received = new ASAPChunkReceivedParameters(format, sender, uri, era);

        this.receivedList.add(received);
        System.out.println("Notify the app about new received chunk");
        this.template.convertAndSend("/received/user",received);
    //TODO
        ASAPMessages receivedMessages =
                Helper.getMessagesByChunkReceivedInfos(format, sender, uri, this.rootFolder, era);

        Iterator<CharSequence> mess = receivedMessages.getMessagesAsCharSequence();
        while (mess.hasNext()) {
            System.out.println("Got new received Messages:" + mess.next().toString());
        }



    }

    public List<ASAPChunkReceivedParameters> getReceivedList() { return this.receivedList; }

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
