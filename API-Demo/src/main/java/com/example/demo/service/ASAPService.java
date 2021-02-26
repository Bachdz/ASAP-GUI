package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.service.asap.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.UnknownHostException;
import java.util.*;
import java.io.PrintStream;
import java.net.InetAddress;


@Service
public class ASAPService {
    private PrintStream standardOut = System.out;
    private PrintStream standardError = System.err;
    private OutputCollector outputCollector = new OutputCollector();
    @Autowired
    private SimpMessagingTemplate template;
    private TCPStream connectionAttempt = null;
    public static final String PEERS_ROOT_FOLDER = "src/asapPeers";
    private Map<String, ASAPPeer> peers = new HashMap();
    private Map<String, TCPStream> streams = new HashMap<>();
    private long waitPeriod = 1000 * 5; // 5 seconds

    public ASAPService() {}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                         ASAP API usage                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void doStart() throws IOException, ASAPException {
        this.setOutStreams();
        this.doInitializeASAPStorages();

    }
    private void setOutStreams() {
        System.setOut(new PrintStream(outputCollector));
    }
    private void doInitializeASAPStorages() throws IOException, ASAPException {
        // set up peers
        File rootFolder = new File(PEERS_ROOT_FOLDER);
        if (rootFolder.exists()) {
            // each root folder is a peer - per definition in this very application
            String[] peerNames = rootFolder.list();

            // set up peers
            for (String peerName : peerNames) {
                this.createPeer(peerName);
            }
        }
    }



    //TODO exception
    public ConnectionResponse doOpen(int port, String engineName) throws ASAPException, UnknownHostException {
        String name = "server:" + port;

        TCPStream stream = new TCPStream(port, true, name);
        try {
            if (this.startTCPStream(name, stream, engineName)) {

                String thisAddress = InetAddress.getLocalHost().toString();
                ConnectionResponse connectionResponse = new ConnectionResponse(engineName, thisAddress, port, true);
                return connectionResponse;
            } else {
                return null;
            }
        } catch (ASAPException e) {
            System.err.println(e);
            return null;
        }
    }

    public ConnectionResponse doConnect(String remoteHost, int remotePort, String engineName) throws ASAPException {
        try {
            String name = remoteHost + ":" + remotePort;
            TCPStream stream = new TCPStream(remoteHost, remotePort, false, name);
            this.streams.put(name, stream);
            if (this.startTCPStream(name, stream, engineName)) {
                ConnectionResponse connectionResponse = new ConnectionResponse(engineName, remoteHost, remotePort, true);
                return connectionResponse;
            } else {
                return null;
            }


        } catch (ASAPException e) {
            System.err.println(e);
            return null;
        }
    }

    public void doKillConnectionAttempt(String host, int port) throws ASAPException {
        try {
            String channelName = host + ":" + port;
            TCPStream channel = this.streams.remove(channelName);
            if (channel == null) {
                System.err.println("channel does not exist: " + channelName);
                return;
            }
            System.out.println("kill connection to: " + channelName);
            channel.kill();

            System.out.println(".. done");
        } catch (Exception e) {
            System.err.println(e);
        }
    }


    //TODO: EXCEPTION
    public void doKillServer(String port) {
        try {
            String channelName = "server:" + port;
            TCPStream channel = this.streams.remove(channelName);
            if (channel == null) {
                System.err.println("channel does not exist: " + channelName);
                return;
            }
            System.out.println("kill " + channelName);
            channel.kill();

            System.out.println(".. done");
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    private boolean startTCPStream(String name, TCPStream stream, String peerName) throws ASAPException {
        stream.setWaitPeriod(this.waitPeriod);
        ASAPPeer asapPeer = this.getASAPPeer(peerName);
        stream.setListener(new TCPStreamCreatedHandler(asapPeer));

        stream.start();

        // Check if socket is initialized
        synchronized (stream) {
            while (stream.isInitialized()) {
                try {
                    stream.wait();
                } catch (InterruptedException e) {
                    System.err.println(e);
                }
            }
        }

        //check if smt went wrong
        if (!stream.getFatalError()) {
            this.streams.put(name, stream);
            return true;
        }
        return false;

    }

    public void doCreateASAPMessages(Message message, String peername, String appName, String uri) throws IOException, ASAPException {
        String messages = message.getMess();
//        System.out.println(messages);
        ASAPStorage asapStorage = this.getEngine(peername, appName);
        if (asapStorage == null) {
            this.standardError.println("storage does not exist: " + peername + ":" + appName);
            return;
        }
        asapStorage.add(uri, messages);
    }


    public int getCurrentEra(String peername, String appname) throws IOException, ASAPException {
        ASAPStorage asapStorage = this.getEngine(peername, appname);
        return asapStorage.getEra();
    }


    public List<Channel> getChannels(String peername, String appname) throws IOException, ASAPException {
        //TODO try catch error
        List<Channel> returnChannels = new ArrayList<>();
        ASAPStorage asapStorage = this.getEngine(peername, appname);
        for (CharSequence uri : asapStorage.getChannelURIs()) {

            ASAPChannel channel = asapStorage.getChannel(uri);
            Set<CharSequence> recipients = channel.getRecipients();

            Channel returnChannel = new Channel(uri, recipients);
            returnChannels.add(returnChannel);
        }
        return returnChannels;
    }


    //get console log data
    public List<String> getConsoleLog() throws IOException {
        return this.outputCollector.getLines();
    }

    public List<String> getPeers() {
        List<String> peersName = new ArrayList<>();
        for (String peerName : this.peers.keySet()) {
            peersName.add(peerName);
        }
//        System.out.println(peersName);
        return peersName;
    }

    public List<App> getStorages(String peername) {
        ASAPPeer asapPeer = this.peers.get(peername);
        List<App> returnStorage = new ArrayList<>() ;
        for (CharSequence format : asapPeer.getFormats()) {
            App app = new App(format);
            returnStorage.add(app);
        }
        return returnStorage;
    }


    public List<Chunk> getMessagesByChunk(String peername, String appname, String uri) throws IOException, ASAPException {
        ASAPStorage asapStorage = this.getEngine(peername, appname);

        // get all Eras in that storage
        String dir = this.PEERS_ROOT_FOLDER + "/" + peername + "/" + appname;
        Collection<Integer> erasInFolder = Utils.getErasInFolder(dir);

        List<Chunk> returnArray = new ArrayList<>();

        //loop through all Eras
        for (int i : erasInFolder) {

            //get chunk out of that era
            ASAPChunk chunk = asapStorage.getChunkStorage().getChunk(uri, i);

            List<CharSequence> messArray = new ArrayList<>();

            //get all messages in that chunk
            Iterator<CharSequence> mess = chunk.getMessagesAsCharSequence();

            //save to array
            while (mess.hasNext()) {
                messArray.add(mess.next());
            }

            //create return object
            Chunk returnMess = new Chunk(i, messArray);

            //add obj to return array
            returnArray.add(returnMess);
        }

        return returnArray;

    }


    //TODO
    public List<Received> getReceivedMessages(String peername, String appname, String uri) throws IOException, ASAPException {
        ASAPStorage asapStorage = this.getEngine(peername, appname);
        List<Received> receivedList = new ArrayList<>();
        List<CharSequence> senderList = asapStorage.getSender();


//        System.out.println(senderList.toString());
        //lop through all sender
        for (CharSequence sender : senderList) {

            List<Chunk> chunksList = new ArrayList<>();


            // get all Eras in that sender storage
            String dir = this.PEERS_ROOT_FOLDER + "/" + peername + "/" + appname + "/" + sender;
            Collection<Integer> erasInFolder = Utils.getErasInFolder(dir);

            //loop through all Eras
            for (int i : erasInFolder) {
                List<CharSequence> messList = new ArrayList<>();

                //Get Received Storage
                ASAPChunkStorage receivedStorage = asapStorage.getReceivedChunksStorage(sender);

                //get Chunk out of Uri and Era
                ASAPChunk chunk = receivedStorage.getChunk(uri, i);


                //get all messages in that chunk
                Iterator<CharSequence> mess = chunk.getMessagesAsCharSequence();

                //save to array
                while (mess.hasNext()) {
                    messList.add(mess.next());
                }

                //create return object
                Chunk returnChunk = new Chunk(i, messList);
                chunksList.add(returnChunk);
            }


            //create new received object
            Received received = new Received();
            received.setSender(sender);
            received.setChunks(chunksList);



            receivedList.add(received);

        }

        return receivedList;

    }

    public Iterator<CharSequence> getMessages(String peername, String appname, String uri) throws IOException, ASAPException {
        ASAPStorage asapStorage = this.getEngine(peername, appname);
        ASAPChannel channel = asapStorage.getChannel(uri);

        return channel.getMessages().getMessagesAsCharSequence();
    }

    //createPeer
    private void createPeer(String name) throws IOException, ASAPException {
        ChunkReceivedListener chunkReceivedListener =
                new ChunkReceivedListener(PEERS_ROOT_FOLDER + "/" + name, template);

        ASAPPeer asapPeer = ASAPPeerFS.createASAPPeer(name, // peer name
                PEERS_ROOT_FOLDER + "/" + name, // peer folder
                chunkReceivedListener);

        //put newly created peer in hashmap
        this.peers.put(name, asapPeer);
    }

    private ASAPEngine getEngine(String peername, String appName) throws ASAPException, IOException {
        ASAPPeer asapPeer = this.peers.get(peername);
        if (asapPeer == null) {
            throw new ASAPException("peer does not exist: " + peername);
        }

        return asapPeer.getEngineByFormat(appName);
    }

    public ASAPPeer getASAPPeer(String peerName) throws ASAPException {
        ASAPPeer asapPeer = this.peers.get(peerName);
        if (asapPeer == null) {
            throw new ASAPException("engine does not exist: " + peerName);
        }
        return asapPeer;
    }


    //TODO: Exception ?
    public void doActivateOnlineMessages(String peerName) {
        try {
            ASAPPeer asapPeer = this.peers.get(peerName);
            if (asapPeer != null) {
                asapPeer.activateOnlineMessages();
            }

        } catch (RuntimeException e) {
//            this.printUsage(CREATE_ASAP_APP, e.getLocalizedMessage());
        }
    }

    //TODO: Exception ?
    public void doDectivateOnlineMessages(String peerName) {
        try {
            ASAPPeer asapPeer = this.peers.get(peerName);
            if (asapPeer != null) {
                asapPeer.deactivateOnlineMessages();
            }

        } catch (RuntimeException e) {
//            this.printUsage(CREATE_ASAP_APP, e.getLocalizedMessage());
        }
    }

    //TODO: exception ?
    public void doSetSendReceivedMessage(String peerName, String appName, boolean value) throws ASAPException {
        try {
            ASAPEngine engine = this.getEngine(peerName, appName);
            engine.setBehaviourSendReceivedChunks(value);
        } catch (RuntimeException | IOException | ASAPException e) {
//            this.printUsage(SET_SEND_RECEIVED_MESSAGES, e.getLocalizedMessage());
        }
    }

    //TODO: exception ?
    public boolean doGetSendReceivedMessage(String peerName, String appName) throws ASAPException, IOException {
        ASAPEngine engine = this.getEngine(peerName, appName);
        return engine.getBehaviourSendReceivedChunks();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                               attach layer 2 (ad-hoc) protocol to ASAP                                 //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class TCPStreamCreatedHandler implements TCPStreamCreatedListener {
        private final ASAPPeer asapPeer;

        public TCPStreamCreatedHandler(ASAPPeer asapPeer) {
            this.asapPeer = asapPeer;
        }

        @Override
        public void streamCreated(TCPStream channel) {
            ASAPService.this.standardOut.println("Channel created");

            try {
                this.asapPeer.handleConnection(
                        channel.getInputStream(),
                        channel.getOutputStream());
            } catch (IOException | ASAPException e) {
                ASAPService.this.standardOut.println("call of engine.handleConnection failed: "
                        + e.getLocalizedMessage());
            }
        }
    }

    public void doCreateASAPPeer(String peerName) throws ASAPException {

        try {
            this.createPeer(peerName);
        } catch (RuntimeException e) {
//            this.printUsage(CREATE_ASAP_PEER, e.getLocalizedMessage());
        } catch (IOException | ASAPException e) {
//            this.printUsage(CREATE_ASAP_PEER, e.getLocalizedMessage());
        }
    }

    public void doCreateASAPApp(String peer, CharSequence appName) throws ASAPException {
        try {
            ASAPPeer asapPeer = this.peers.get(peer);
            if (asapPeer != null) {
//              ASAPStorage storage = asapPeer.createEngineByFormat(appName);
                asapPeer.createEngineByFormat(appName);

            }
        } catch (RuntimeException e) {
//            this.printUsage(CREATE_ASAP_APP, e.getLocalizedMessage());
        } catch (IOException | ASAPException e) {
//            this.printUsage(CREATE_ASAP_APP, e.getLocalizedMessage());
        }
    }

    public void doCreateASAPChannel(String peerName, String app, Channel channel) throws ASAPException {
        try {
            CharSequence uri = channel.getUri();
            Set<CharSequence> recipients = channel.getRecipients();
            // one recipient is mandatory - provoke an exception otherwise

            ASAPStorage storage = this.getEngine(peerName, app);

            // finally add peername
            recipients.add(peerName);

            storage.createChannel(peerName, uri, recipients);
        } catch (RuntimeException e) {
//            this.printUsage(CREATE_ASAP_CHANNEL, e.getLocalizedMessage());
        } catch (IOException | ASAPException e) {
//            this.printUsage(CREATE_ASAP_CHANNEL, e.getLocalizedMessage());
        }
    }

    //TODO Exeption handling
    public void doResetASAPStorages() throws Error {
            ASAPEngineFS.removeFolder(PEERS_ROOT_FOLDER);
            File rootFolder = new File(PEERS_ROOT_FOLDER);
            rootFolder.mkdirs();
            peers.clear();

    }



}