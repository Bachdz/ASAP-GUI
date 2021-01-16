package com.example.demo.net.sharksystem.cmdline;

import com.example.demo.model.Channel;
import com.example.demo.model.ConnectionResponse;
import com.example.demo.model.Mess;
import com.example.demo.net.sharksystem.Utils;
import com.example.demo.net.sharksystem.asap.*;

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

    private TCPStream connectionAttempt = null ;

    public static final String PEERS_ROOT_FOLDER = "src/asapPeers";
    private Map<String, ASAPPeer> peers = new HashMap();

    private void setOutStreams() throws IOException, ASAPException {
        System.setOut(new PrintStream(outputCollector));
    }

    public ASAPService() throws IOException, ASAPException {
//        this.setOutStreams();
    }


    private List<String> cmds = new ArrayList<>();


    private Map<String, TCPStream> streams = new HashMap<>();
    private long waitPeriod = 1000 * 5; // 5 seconds

    private void setWaitPeriod(long period) {
        this.waitPeriod = period;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                         ASAP API usage                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void doStart() throws IOException, ASAPException {
//        this.setOutStreams();
        this.doInitializeASAPStorages();

    }

    //TODO exception
    public ConnectionResponse doOpen(int port, String engineName) throws ASAPException, UnknownHostException {
        String name = "server:" + port;


        TCPStream stream = new TCPStream(port, true, name);
        try {
            if (this.startTCPStream(name, stream, engineName)) {

                String thisAddress = InetAddress.getLocalHost().toString();
                ConnectionResponse connectionResponse = new ConnectionResponse(engineName,thisAddress,port,true);
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
                ConnectionResponse connectionResponse = new ConnectionResponse(engineName,remoteHost,remotePort,true);
                return connectionResponse;
            } else {
                return null;
            }


        } catch (ASAPException e) {
            System.err.println(e);
            return null;
        }
    }

    public void doKillConnectionAttempt (String host, int port) throws ASAPException{
        try {
            String channelName = host+":" +port;
            TCPStream channel = this.streams.remove(channelName);
            if (channel == null) {
                this.standardError.println("channel does not exist: " + channelName);
                return;
            }
            this.standardOut.println("kill connection to: "+ channelName);
            channel.kill();

            this.standardOut.println(".. done");
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
                this.standardError.println("channel does not exist: " + channelName);
                return;
            }
            this.standardOut.println("kill "+ channelName);
            channel.kill();

            this.standardOut.println(".. done");
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

    public void doCreateASAPMessages(Mess message, String peername, String appName, String uri) throws IOException, ASAPException {
        String messages = message.getMess();
        System.out.println(messages);
        ASAPStorage asapStorage = this.getEngine(peername, appName);
        if (asapStorage == null) {
            this.standardError.println("storage does not exist: " + peername + ":" + appName);
            return;
        }
        asapStorage.add(uri, messages);
    }


    public Collection<Integer> doGetEras(String peername, String appname) {
        String dir = this.PEERS_ROOT_FOLDER + "/" + peername + "/" + appname;
        Collection<Integer> erasInFolder = Utils.getErasInFolder(dir);
        return erasInFolder;
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

    public List<CharSequence> getStorages(String peername) {
        List<CharSequence> storages = new ArrayList<>();
        ASAPPeer asapPeer = this.peers.get(peername);
        for (CharSequence format : asapPeer.getFormats()) {
            storages.add(format);
        }
        return storages;
    }

    //TODO
    public void getReceivedMessages(String peername, String appname, String uri) throws IOException, ASAPException {
        ASAPStorage asapStorage = this.getEngine(peername, appname);
        List<CharSequence> senderList = asapStorage.getSender();
        System.out.println(senderList.toString());
        for (CharSequence sender : senderList) {
            System.out.println("Sender: " + sender.toString());
            ASAPChunkStorage receivedStorage = asapStorage.getReceivedChunksStorage(sender);
            Iterator<CharSequence> mess = receivedStorage.getASAPMessages(uri).getMessagesAsCharSequence();
            while (mess.hasNext()) {
                System.out.println("Messages:" + mess.next().toString());
            }


        }


    }

    public Iterator<CharSequence> getMessages(String peername, String appname, String uri) throws IOException, ASAPException {

        ASAPStorage asapStorage = this.getEngine(peername, appname);
        ASAPChannel channel = asapStorage.getChannel(uri);

        return channel.getMessages().getMessagesAsCharSequence();
    }

    //createPeer
    private void createPeer(String name) throws IOException, ASAPException {
        ExampleASAPChunkReceivedListener asapChunkReceivedListener =
                new ExampleASAPChunkReceivedListener(PEERS_ROOT_FOLDER + "/" + name);

        ASAPPeer asapPeer = ASAPPeerFS.createASAPPeer(name, // peer name
                PEERS_ROOT_FOLDER + "/" + name, // peer folder
                asapChunkReceivedListener);

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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                           method implementations                                   //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////



    public void doList() throws ASAPException {
        this.standardOut.println("connections:");
        for (String connectionName : this.streams.keySet()) {
            this.standardOut.println(connectionName);
        }

        this.doPrintAllInformation();
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


    public void doResetASAPStorages() throws Error {
        try {
            ASAPEngineFS.removeFolder(PEERS_ROOT_FOLDER);
            File rootFolder = new File(PEERS_ROOT_FOLDER);
            rootFolder.mkdirs();
            peers.clear();
        } catch (Error e) {
            throw new Error();
        }
    }

    public void doInitializeASAPStorages() throws IOException, ASAPException {
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



    public Map<String, ASAPPeer> doPrintAllInformation() throws ASAPException {
        try {
            this.standardOut.println(this.peers.keySet().size() + " peers in folder: " + PEERS_ROOT_FOLDER);
            for (String peername : this.peers.keySet()) {
                this.standardOut.println("+++++++++++++++++++");
                this.standardOut.println("Peer: " + peername);
                ASAPPeer asapPeer = this.peers.get(peername);
                for (CharSequence format : asapPeer.getFormats()) {
                    ASAPEngine asapStorage = asapPeer.getEngineByFormat(format);
                    System.out.println("storage: " + format);
                }
                this.standardOut.println("+++++++++++++++++++\n");
            }
        } catch (RuntimeException | IOException | ASAPException e) {
//            this.printUsage(PRINT_ALL_INFORMATION, e.getLocalizedMessage());
        }
        return this.peers;
    }

    public void doPrintStorageInformation(String parameterString) throws ASAPException {
        StringTokenizer st = new StringTokenizer(parameterString);

        try {
            String peername = st.nextToken();
            String appName = st.nextToken();

            // first - get storage
            ASAPStorage asapStorage = this.getEngine(peername, appName);
            if (asapStorage == null) {
                System.err.println("storage does not exist: " + peername + ":" + appName);
                return;
            }

            // iterate URI
            this.standardOut.println(asapStorage.getChannelURIs().size() +
                    " channels in storage " + appName +
                    " (note: channels without messages are considered non-existent)");
            for (CharSequence uri : asapStorage.getChannelURIs()) {
                this.doPrintChannelInformation(parameterString + " " + uri);
            }
        } catch (RuntimeException | IOException | ASAPException e) {
//            this.printUsage(PRINT_STORAGE_INFORMATION, e.getLocalizedMessage());
        }
    }


    public void doPrintChannelInformation(String parameterString) throws ASAPException {
        //                     out.println("example: " + PRINT_CHANNEL_INFORMATION + " Alice chat sn2://abChat");
        StringTokenizer st = new StringTokenizer(parameterString);

        try {
            String peername = st.nextToken();
            String appName = st.nextToken();
            String uri = st.nextToken();

            // first - get storage
            ASAPStorage asapStorage = this.getEngine(peername, appName);
            if (asapStorage == null) {
                this.standardError.println("storage does not exist: " + peername + ":" + appName);
                return;
            }

            this.printChannelInfo(asapStorage, uri, appName);

        } catch (RuntimeException | ASAPException | IOException e) {
//            this.printUsage(CREATE_ASAP_MESSAGE, e.getLocalizedMessage());
        }
    }

    private void printChannelInfo(ASAPStorage asapStorage, CharSequence uri, CharSequence appName)
            throws IOException, ASAPException {

        ASAPChannel channel = asapStorage.getChannel(uri);
        Set<CharSequence> recipients = channel.getRecipients();

        this.standardOut.println("Peer:App:Channel == " + channel.getOwner() + ":" + appName + ":" + channel.getUri());
        this.standardOut.println("#Messages == " + channel.getMessages().size());
        this.standardOut.println("#Recipients == " + recipients.size() +
                " (0 means: open channel - no restrictions - anybody receives from this channel)");
        for (CharSequence recipient : recipients) {
            this.standardOut.println(recipient);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                              helper methods                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String getUriFromStorageName(String storageName) throws ASAPException {
        int i = storageName.indexOf(":");
        if (i < 0) throw new ASAPException("malformed storage name (missing \":\") " + storageName);

        return storageName.substring(i);
    }

    private ASAPEngine getEngine(String storageName) throws ASAPException, IOException {
        // split name into peer and storage
        String[] split = storageName.split(":");

        ASAPEngine asapEngine = this.getEngine(split[0], split[1]);
        if (asapEngine == null) throw new ASAPException("no storage with name: " + storageName);

        return asapEngine;
    }

    private boolean parseOnOffValue(String onOff) throws ASAPException {
        if (onOff.equalsIgnoreCase("on")) return true;
        if (onOff.equalsIgnoreCase("off")) return false;

        throw new ASAPException("unexpected value; expected on or off, found: " + onOff);

    }

    public String getEngineRootFolderByStorageName(String storageName) throws ASAPException, IOException {
        ASAPEngineFS asapEngineFS = (ASAPEngineFS) this.getEngine(storageName);
        return asapEngineFS.getRootFolder();
    }
}