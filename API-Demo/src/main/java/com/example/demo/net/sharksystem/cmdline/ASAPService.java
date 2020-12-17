package com.example.demo.net.sharksystem.cmdline;

import com.example.demo.net.sharksystem.asap.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;
import java.io.PrintStream;


@Service
public class ASAPService{



    private PrintStream standardOut = System.out;
    private PrintStream standardError = System.err;
    private OutputCollector outputCollector = new OutputCollector();

    private BufferedReader userInput;

    public static final String PEERS_ROOT_FOLDER = "src/asapPeers";
    private Map<String, ASAPPeer> peers = new HashMap();

    private void setOutStreams() throws IOException, ASAPException {
        System.setOut(new PrintStream(outputCollector));
    }

    public ASAPService () throws IOException, ASAPException {

    }



    private List<String> cmds = new ArrayList<>();


    private Map<String, TCPStream> streams = new HashMap<>();
    private long waitPeriod = 1000*30; // 30 seconds

    private void setWaitPeriod(long period) {
        this.waitPeriod = period;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                         ASAP API usage                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void doStart() throws IOException, ASAPException {
        this.setOutStreams();
        this.doInitializeASAPStorages();
    }

    //get console log data
    public List<String> getConsoleLog () throws IOException {
        return this.outputCollector.getLines();
    }

    public List<String> getPeers() {
        List<String> peersName = new ArrayList<>();
        for(String peerName : this.peers.keySet()) {

            peersName.add(peerName);
        }
        System.out.println(peersName);
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
        if(asapPeer == null) {
            throw new ASAPException("peer does not exist: " + peername);
        }

        return asapPeer.getEngineByFormat(appName);
    }

    public ASAPPeer getASAPPeer(String peerName) throws ASAPException {
        ASAPPeer asapPeer = this.peers.get(peerName);
        if(asapPeer == null) {
            throw new ASAPException("engine does not exist: " + peerName);
        }

        return asapPeer;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                               attach layer 2 (ad-hoc) protocol to ASAP                                 //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void startTCPStream(String name, TCPStream stream, String peerName) throws ASAPException {
        stream.setWaitPeriod(this.waitPeriod);
        ASAPPeer asapPeer = this.getASAPPeer(peerName);

        stream.setListener(new TCPStreamCreatedHandler(asapPeer));
        stream.start();
        this.streams.put(name, stream);
    }



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




    public void doConnect(String parameterString) throws ASAPException {
        StringTokenizer st = new StringTokenizer(parameterString);

        try {
            String remoteHost = st.nextToken();
            String remotePortString = st.nextToken();
            String engineName = null;
            if(!st.hasMoreTokens()) {
                // no remote host set - shift
                engineName = remotePortString;
                remotePortString = remoteHost;
                remoteHost = "localhost";
            } else {
                engineName = st.nextToken();
            }
            int remotePort = Integer.parseInt(remotePortString);

            String name =  remoteHost + ":" + remotePortString;

            this.startTCPStream(name,  new TCPStream(remotePort, false, name), engineName);
        }
        catch(RuntimeException re) {
//            this.printUsage(CONNECT, re.getLocalizedMessage());
        } catch (ASAPException e) {
//            this.printUsage(CONNECT, e.getLocalizedMessage());
        }
    }

    public void doOpen(String parameterString) throws ASAPException {
        StringTokenizer st = new StringTokenizer(parameterString);

        try {
            String portString = st.nextToken();
            String engineName = st.nextToken();

            int port = Integer.parseInt(portString);
            String name =  "server:" + port;

            this.startTCPStream(name,  new TCPStream(port, true, name), engineName);
        }
        catch(RuntimeException re) {
//            this.printUsage(OPEN, re.getLocalizedMessage());
        } catch (ASAPException e) {
//            this.printUsage(OPEN, e.getLocalizedMessage());
        }
    }

    public void doList() throws ASAPException {
        this.standardOut.println("connections:");
        for(String connectionName : this.streams.keySet()) {
            this.standardOut.println(connectionName);
        }

        this.doPrintAllInformation();
    }



    public void doCreateASAPPeer(String peerName) throws ASAPException {

        try {
            this.createPeer(peerName);
        }
        catch(RuntimeException e) {
//            this.printUsage(CREATE_ASAP_PEER, e.getLocalizedMessage());
        } catch (IOException | ASAPException e) {
//            this.printUsage(CREATE_ASAP_PEER, e.getLocalizedMessage());
        }
    }

    public void doCreateASAPApp(String peer, CharSequence appName) throws ASAPException {
        try{
            ASAPPeer asapPeer = this.peers.get(peer);
            if(asapPeer != null) {
//              ASAPStorage storage = asapPeer.createEngineByFormat(appName);
                asapPeer.createEngineByFormat(appName);

            }
        }
        catch(RuntimeException e) {
//            this.printUsage(CREATE_ASAP_APP, e.getLocalizedMessage());
        } catch (IOException | ASAPException e) {
//            this.printUsage(CREATE_ASAP_APP, e.getLocalizedMessage());
        }
    }
/*    public void doCreateASAPChannel(String peerName, String app, String uriString, String[] recip) throws ASAPException {
//        StringTokenizer st = new StringTokenizer(parameterString);

        try {
            String peername = peerName;
            String appName = app;
            String uri = uriString;

            ASAPStorage storage = this.getEngine(peername, appName);

            Set<CharSequence> recipients = new HashSet<>();

            // one recipient is mandatory - provoke an exception otherwise

            for (String pers : recip) {
                        recipients.add(pers);
            }
//            recipients.add(recipient);

            // optional recipients
//            while(st.hasMoreTokens()) {
//                recipients.add(st.nextToken());
//            }

            // finally add peername
            recipients.add(peername);

            storage.createChannel(peername, uri, recipients);
        }
        catch(RuntimeException e) {
            this.printUsage(CREATE_ASAP_CHANNEL, e.getLocalizedMessage());
        } catch (IOException | ASAPException e) {
            this.printUsage(CREATE_ASAP_CHANNEL, e.getLocalizedMessage());
        }
    }*/

/*    public void doCreateASAPMessage(String parameterString) throws ASAPException {
        StringTokenizer st = new StringTokenizer(parameterString);

        try {
            String peername = st.nextToken();
            String appName = st.nextToken();
            String uri = st.nextToken();
            String message = st.nextToken();

            // first - get storage
            ASAPStorage asapStorage = this.getEngine(peername, appName);
            if(asapStorage == null) {
                this.standardError.println("storage does not exist: " + peername + ":" + appName);
                return;
            }
            asapStorage.add(uri, message);
        }
        catch(RuntimeException e) {
            this.printUsage(CREATE_ASAP_MESSAGE, e.getLocalizedMessage());
        } catch (IOException | ASAPException e) {
            this.printUsage(CREATE_ASAP_MESSAGE, e.getLocalizedMessage());
        }
    }*/

    public void doResetASAPStorages() throws Error{
        try {
            ASAPEngineFS.removeFolder(PEERS_ROOT_FOLDER);
            File rootFolder = new File(PEERS_ROOT_FOLDER);
            rootFolder.mkdirs();
            peers.clear();
        } catch (Error e) {
            throw new Error();
        }
    }

    public void doInitializeASAPStorages () throws IOException, ASAPException{
        // set up peers
        File rootFolder = new File(PEERS_ROOT_FOLDER);
        if(rootFolder.exists()) {
            // each root folder is a peer - per definition in this very application
            String[] peerNames = rootFolder.list();

            // set up peers
            for(String peerName : peerNames) {
                this.createPeer(peerName);
            }
        }
    }

 /*   public void doResetASAPStorages() {
        ASAPEngineFS.removeFolder(PEERS_ROOT_FOLDER);
        File rootFolder = new File(PEERS_ROOT_FOLDER);
        rootFolder.mkdirs();
    }*/

/*
    public void doSetSendReceivedMessage(String parameterString) throws ASAPException {
        StringTokenizer st = new StringTokenizer(parameterString);

        try {
            String storageName = st.nextToken();
            String onOff = st.nextToken();

            boolean on = this.parseOnOffValue(onOff);

            ASAPEngine engine = this.getEngine(storageName);
            engine.setBehaviourSendReceivedChunks(on);
        }
        catch(RuntimeException | IOException | ASAPException e) {
            this.printUsage(SET_SEND_RECEIVED_MESSAGES, e.getLocalizedMessage());
        }
    }
*/


    public Map<String, ASAPPeer>  doPrintAllInformation() throws ASAPException {
        try {
            this.standardOut.println(this.peers.keySet().size() + " peers in folder: " + PEERS_ROOT_FOLDER);
            for(String peername : this.peers.keySet()) {
                this.standardOut.println("+++++++++++++++++++");
                this.standardOut.println("Peer: " + peername);
                ASAPPeer asapPeer = this.peers.get(peername);
                for (CharSequence format : asapPeer.getFormats()) {
                    ASAPEngine asapStorage = asapPeer.getEngineByFormat(format);
                    System.out.println("storage: " + format);
                }
                this.standardOut.println("+++++++++++++++++++\n");
            }
        }
        catch(RuntimeException | IOException | ASAPException e) {
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
            if(asapStorage == null) {
                System.err.println("storage does not exist: " + peername + ":" + appName);
                return;
            }

            // iterate URI
            this.standardOut.println(asapStorage.getChannelURIs().size() +
                    " channels in storage " + appName +
                    " (note: channels without messages are considered non-existent)");
            for(CharSequence uri : asapStorage.getChannelURIs()) {
                this.doPrintChannelInformation(parameterString + " " + uri);
            }
        }
        catch(RuntimeException | IOException | ASAPException e) {
//            this.printUsage(PRINT_STORAGE_INFORMATION, e.getLocalizedMessage());
        }
    }

    public void doSleep(String parameterString) throws ASAPException {
        StringTokenizer st = new StringTokenizer(parameterString);

        try {
            Thread.sleep(Long.parseLong(parameterString));
        }
        catch(InterruptedException e) {
            this.standardOut.println("sleep interrupted");
        }
        catch(RuntimeException e) {
//            this.printUsage(PRINT_STORAGE_INFORMATION, e.getLocalizedMessage());
        }
    }

    private void doShowLog() {
        if(this.cmds.size() < 1) return;

        boolean first = true;
        for(String c : this.cmds) {
            if (!first) {
                this.standardOut.println("\\n\" + ");
            } else {
                first = false;
            }
            this.standardOut.print("\"");
            this.standardOut.print(c);
        }
        this.standardOut.println("\"");
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
            if(asapStorage == null) {
                this.standardError.println("storage does not exist: " + peername + ":" + appName);
                return;
            }

            this.printChannelInfo(asapStorage, uri, appName);

        }
        catch(RuntimeException | ASAPException | IOException e) {
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
        for(CharSequence recipient : recipients) {
            this.standardOut.println(recipient);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                              helper methods                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String getUriFromStorageName(String storageName) throws ASAPException {
        int i = storageName.indexOf(":");
        if(i < 0) throw new ASAPException("malformed storage name (missing \":\") " + storageName);

        return storageName.substring(i);
    }

    private ASAPEngine getEngine(String storageName) throws ASAPException, IOException {
        // split name into peer and storage
        String[] split = storageName.split(":");

        ASAPEngine asapEngine = this.getEngine(split[0], split[1]);
        if(asapEngine == null) throw new ASAPException("no storage with name: " + storageName);

        return asapEngine;
    }

    private boolean parseOnOffValue(String onOff) throws ASAPException {
        if(onOff.equalsIgnoreCase("on")) return true;
        if(onOff.equalsIgnoreCase("off")) return false;

        throw new ASAPException("unexpected value; expected on or off, found: " + onOff);

    }

    public String getEngineRootFolderByStorageName(String storageName) throws ASAPException, IOException {
        ASAPEngineFS asapEngineFS = (ASAPEngineFS) this.getEngine(storageName);
        return asapEngineFS.getRootFolder();
    }
}