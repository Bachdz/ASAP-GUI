package com.example.demo.net.sharksystem.cmdline;

import com.example.demo.net.sharksystem.asap.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
@Service
public class ASAPService {

    // commands
    public static final String CONNECT = "connect";
    public static final String OPEN = "open";
    public static final String EXIT = "exit";
    public static final String LIST = "list";
    public static final String KILL = "kill";
    public static final String SETWAITING = "setwaiting";
    public static final String CREATE_ASAP_PEER = "newpeer";
    public static final String CREATE_ASAP_APP = "newapp";
    public static final String CREATE_ASAP_CHANNEL = "newchannel";
    public static final String CREATE_ASAP_MESSAGE = "newmessage";
    public static final String RESET_ASAP_STORAGES = "resetstorage";
    public static final String SET_SEND_RECEIVED_MESSAGES = "setSendReceived";
    public static final String PRINT_CHANNEL_INFORMATION = "printChannelInfo";
    public static final String PRINT_STORAGE_INFORMATION = "printStorageInfo";
    public static final String PRINT_ALL_INFORMATION = "printAll";
    public static final String SLEEP = "sleep";
    public static final String SHOW_LOG = "showlog";

    private PrintStream standardOut = System.out;
    private PrintStream standardError = System.err;

    private BufferedReader userInput;

    public static final String PEERS_ROOT_FOLDER = "src/asapPeers";
    private Map<String, ASAPPeer> peers = new HashMap();

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

//    public static void main(String[] args) throws IOException, ASAPException {
//        PrintStream os = System.out;
//
//        os.println("Welcome SN2 version 0.1");
//        CmdLineUI userCmd = new CmdLineUI(os, System.in);
//
//        userCmd.printUsage();
//        //userCmd.runCommandLoop();
//    }


    public void setOutStreams(PrintStream ps) {
        this.standardOut = ps;
        this.standardError = ps;
    }


    public ASAPService() throws IOException, ASAPException {
        this.doInitializeASAPStorages();
    }

    public void printUsage(String cmdString, String comment) throws ASAPException {
        PrintStream out = this.standardOut;

        if(comment == null) comment = " ";
        out.println("malformed command: " + comment);
        out.println("use:");
        switch(cmdString) {
            case CONNECT:
                out.println(CONNECT + " [IP/DNS-Name_remoteHost] remotePort localEngineName");
                out.println("omitting remote host: localhost is assumed");
                out.println("example: " + CONNECT + " localhost 7070 Bob");
                out.println("example: " + CONNECT + " 7070 Bob");
                out.println("in both cases try to connect to localhost:7070 and let engine Bob handle " +
                        "connection when established");
                break;
            case OPEN:
                out.println(OPEN + " localPort engineName");
                out.println("example: " + OPEN + " 7070 Alice");
                out.println("opens a server socket #7070 and let engine Alice handle connection when established");
                break;
            case LIST:
                out.println("lists all open connections / client and server");
                break;
            case KILL:
                out.println(KILL + " channel name");
                out.println("example: " + KILL + " localhost:7070");
                out.println("kills channel named localhost:7070");
                out.println("channel names are produced by using list");
                out.println(KILL + " all .. kills all open connections");
                break;
            case SETWAITING:
                out.println(SETWAITING + " number of millis to wait between two connection attempts");
                out.println("example: " + KILL + " 1000");
                out.println("set waiting period to one second");
                break;
            case CREATE_ASAP_PEER:
                out.println(CREATE_ASAP_PEER + " name");
                out.println("example: " + CREATE_ASAP_PEER + " Alice");
                out.println("create peer called Alice - data kept under a folder called "
                        + PEERS_ROOT_FOLDER + "/Alice");
                break;
            case CREATE_ASAP_APP:
                out.println(CREATE_ASAP_APP + " peername appName");
                out.println("example: " + CREATE_ASAP_APP + " Alice chat");
                break;
            case CREATE_ASAP_CHANNEL:
                out.println(CREATE_ASAP_CHANNEL + " peername appName uri (recipient)+");
                out.println("example: " + CREATE_ASAP_CHANNEL + " Alice chat sn2://abChat Bob Clara");
                break;
            case CREATE_ASAP_MESSAGE:
                out.println(CREATE_ASAP_MESSAGE + " peername appName uri message");
                out.println("example: " + CREATE_ASAP_MESSAGE + " Alice chat sn2://abChat HiBob");
                out.println("note: message can only be ONE string. That would not work:");
                out.println("does not work: " + CREATE_ASAP_MESSAGE + " Alice chat sn2://abChat Hi Bob");
                out.println("five parameters instead of four.");
                break;
            case RESET_ASAP_STORAGES:
                out.println(RESET_ASAP_STORAGES);
                out.println("removes all storages");
                break;
            case SET_SEND_RECEIVED_MESSAGES:
                out.println(SET_SEND_RECEIVED_MESSAGES + " storageName [on | off]");
                out.println("set whether send received messages");
                out.println("example: " + SET_SEND_RECEIVED_MESSAGES + " Alice:chat on");
                break;
            case PRINT_CHANNEL_INFORMATION:
                out.println(PRINT_CHANNEL_INFORMATION + " peername appName uri");
                out.println("example: " + PRINT_CHANNEL_INFORMATION + " Alice chat sn2://abChat");
                break;
            case PRINT_STORAGE_INFORMATION:
                out.println(PRINT_STORAGE_INFORMATION + " peername appName");
                out.println("example: " + PRINT_STORAGE_INFORMATION + " Alice chat");
                break;
            case PRINT_ALL_INFORMATION:
                out.println(PRINT_ALL_INFORMATION);
                break;
            case SLEEP:
                out.println(SLEEP + " milliseconds");
                out.println("example: " + SLEEP + " sleep 1000");
                out.println("process sleeps a second == 1000 ms");
                break;
            case SHOW_LOG:
                out.println(SHOW_LOG);
                break;
            default:
                out.println("unknown command: " + cmdString);
        }
        throw new ASAPException("had to print usage");
    }

    private List<String> cmds = new ArrayList<>();

    public void runCommandLoop(PrintStream os, InputStream is) {
        this.standardOut = os;
        this.userInput = is != null ? new BufferedReader(new InputStreamReader(is)) : null;

        // this.runCommandLoop();
    }


    private Map<String, TCPStream> streams = new HashMap<>();
    private long waitPeriod = 1000*30; // 30 seconds

    private void setWaitPeriod(long period) {
        this.waitPeriod = period;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                         ASAP API usage                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////


    //createPeer
    private void createPeer(String name) throws IOException, ASAPException {
        ExampleASAPChunkReceivedListener asapChunkReceivedListener =
                new ExampleASAPChunkReceivedListener(PEERS_ROOT_FOLDER + "/" + name);

        ASAPPeer asapPeer = ASAPPeerFS.createASAPPeer(name, // peer name
                PEERS_ROOT_FOLDER + "/" + name, // peer folder
                asapChunkReceivedListener);

        //put new created peer in hashmap
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
            this.printUsage(CONNECT, re.getLocalizedMessage());
        } catch (ASAPException e) {
            this.printUsage(CONNECT, e.getLocalizedMessage());
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
            this.printUsage(OPEN, re.getLocalizedMessage());
        } catch (ASAPException e) {
            this.printUsage(OPEN, e.getLocalizedMessage());
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
            this.printUsage(CREATE_ASAP_PEER, e.getLocalizedMessage());
        } catch (IOException | ASAPException e) {
            this.printUsage(CREATE_ASAP_PEER, e.getLocalizedMessage());
        }
    }

/*    public void doCreateASAPApp(String peer, String appName) throws ASAPException {
        try{
            ASAPPeer asapPeer = this.peers.get(peer);
            if(asapPeer != null) {
//                ASAPStorage storage = asapPeer.createEngineByFormat(appName);
                asapPeer.createEngineByFormat(appName);
                *//*
                if(!storage.isASAPManagementStorageSet()) {
                    storage.setASAPManagementStorage(ASAPEngineFS.getASAPStorage(peer,
                            PEERS_ROOT_FOLDER + "/" + peer + "/ASAPManagement",
                            ASAP_1_0.ASAP_MANAGEMENT_FORMAT));
                }*//*
            }
        }
        catch(RuntimeException e) {
            this.printUsage(CREATE_ASAP_APP, e.getLocalizedMessage());
        } catch (IOException | ASAPException e) {
            this.printUsage(CREATE_ASAP_APP, e.getLocalizedMessage());
        }
    }*/

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

           // System.out.println("Hier" + e);
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
            this.printUsage(PRINT_ALL_INFORMATION, e.getLocalizedMessage());
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
            this.printUsage(PRINT_STORAGE_INFORMATION, e.getLocalizedMessage());
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
            this.printUsage(PRINT_STORAGE_INFORMATION, e.getLocalizedMessage());
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
            this.printUsage(CREATE_ASAP_MESSAGE, e.getLocalizedMessage());
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