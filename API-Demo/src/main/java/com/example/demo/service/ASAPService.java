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

/**
 * This class work as a service class in the API. It used to perform interaction with the ASAP Framework
 * The ASAP-API will be called and used in this class.
 * @author Thomas Schwoter, Bach Do
 */
@Service
public class ASAPService {
    @Autowired
    private SimpMessagingTemplate template;


    private OutputCollector outputCollector = new OutputCollector();


    public static final String PEERS_ROOT_FOLDER = "src/asapPeers";

    private Map<String, ASAPPeer> peers = new HashMap();
    private Map<String, TCPStream> streams = new HashMap<>();

    private long waitPeriod = 1000 * 5; // 5 seconds

    public ASAPService() {
    }

    /**
     * intialize ASAP Peers, Storages
     * catch output stream to output collector
     *
     * @throws IOException
     * @throws ASAPException
     */
    public void doStart() throws IOException, ASAPException {
        this.setOutStreams();
        this.doInitializeASAPStorages();

    }

    /**
     * set system.out stream to collector
     */
    private void setOutStreams() {
        System.setOut(new PrintStream(outputCollector));
    }

    /**
     * init asap peers and storage
     * @throws IOException
     * @throws ASAPException
     */
    private void doInitializeASAPStorages() throws IOException, ASAPException {

        File rootFolder = new File(PEERS_ROOT_FOLDER);
        if (rootFolder.exists()) {
            String[] peerNames = rootFolder.list();

            // set up peers
            for (String peerName : peerNames) {
                this.createPeer(peerName);
            }
        }
    }

    /**
     * get terminal output / system.out
     * @throws IOException
     * */
    public List<String> getConsoleLog() throws IOException {
        return this.outputCollector.getLines();
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                         ASAP API usage                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * create new ASAP-Messages
     * @param message Message object
     * @param peername name of peer to add mess
     * @param appName name of the engine
     * @param uri uri of the message
     * @throws IOException
     * @throws ASAPException
     * */
    public void doCreateASAPMessages(Message message, String peername, String appName, String uri) throws IOException, ASAPException {
        String messages = message.getMess();
        ASAPStorage asapStorage = this.getEngine(peername, appName);
        if (asapStorage == null) {
            System.err.println("storage does not exist: " + peername + ":" + appName);
            return;
        }
        asapStorage.add(uri, messages);
    }

    /**
     * get the current Era of Engine
     * @param peername  name of peer
     * @param appname name of engine
     * @throws IOException
     * @throws ASAPException
     * */
    public int getCurrentEra(String peername, String appname) throws IOException, ASAPException {
        ASAPStorage asapStorage = this.getEngine(peername, appname);
        return asapStorage.getEra();
    }

    /**
     * get all available channels out of engine
     * @param peername name of asap peer
     * @param appname name of engine
     * @return list of available channels
     * @throws IOException
     * @throws ASAPException
     * */
    public List<Channel> getChannels(String peername, String appname) throws IOException, ASAPException {
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



    /**
     * get all available peers
     * @return list of Peer objects
     * */
    public List<Peer> getPeers() {
        List<Peer> peersName = new ArrayList<>();
        for (String peerName : this.peers.keySet()) {
            Peer peer = new Peer(peerName);
            peersName.add(peer);
        }
        return peersName;
    }

    /**
     * get all available storages in peer
     * @param peerName name of peer
     * @return list of App objects
     * */
    public List<App> getStorages(String peerName) {
        ASAPPeer asapPeer = this.peers.get(peerName);
        List<App> returnStorage = new ArrayList<>() ;
        for (CharSequence format : asapPeer.getFormats()) {
            App app = new App(format);
            returnStorage.add(app);
        }
        return returnStorage;
    }

    /**
     * get all messages within a related chunk
     * @param peername name of peer
     * @param appname name of engine
     * @param uri name of the uri
     * @return list of Chunk objects
     * @throws IOException
     * @throws ASAPException
     * */
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

    /**
     * get all received messages within related chunk
     * @param peername name of peer
     * @param appname name of engine
     * @param uri name of the uri
     * @return list of Received objects
     * @throws IOException
     * @throws ASAPException
     * */
    public List<Received> getReceivedMessages(String peername, String appname, String uri) throws IOException, ASAPException {
        ASAPStorage asapStorage = this.getEngine(peername, appname);
        List<Received> receivedList = new ArrayList<>();
        List<CharSequence> senderList = asapStorage.getSender();


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

    /**
     * create new peer
     * @param name name of the want to create peer
     * @throws IOException
     * @throws ASAPException
     * */
    private void createPeer(String name) throws IOException, ASAPException {
        ChunkReceivedListener chunkReceivedListener =
                new ChunkReceivedListener(PEERS_ROOT_FOLDER + "/" + name, template);

        ASAPPeer asapPeer = ASAPPeerFS.createASAPPeer(name, // peer name
                PEERS_ROOT_FOLDER + "/" + name, // peer folder
                chunkReceivedListener);

        //put newly created peer in hashmap
        this.peers.put(name, asapPeer);
    }




    /**
     * activate online message sending property of asap. This allows the user to start continuously send messages to the encountered peer
     * @param peerName name of peer
     */
    public void doActivateOnlineMessages(String peerName) {
            ASAPPeer asapPeer = this.peers.get(peerName);
            if (asapPeer != null) {
                asapPeer.activateOnlineMessages();
            }
    }
    /**
     * deactivate "online message sending" property of asap. This allows the user to stop continuously send messages to the encountered peer
     * @param peerName name of peer
     */
    public void doDectivateOnlineMessages(String peerName) {
            ASAPPeer asapPeer = this.peers.get(peerName);
            if (asapPeer != null) {
                asapPeer.deactivateOnlineMessages();
            }
    }

    /**
     * set "send received chunk" property of asap. This allow user to send all received chunk to the next encoutered peer
     * @param peerName name of peer
     * @param appName name of engine
     * @param value value of property true/false == on/off
     * @throws ASAPException
     * @throws IOException
     * */
    public void doSetSendReceivedMessage(String peerName, String appName, boolean value) throws ASAPException, IOException {
            ASAPEngine engine = this.getEngine(peerName, appName);
            engine.setBehaviourSendReceivedChunks(value);
    }

    /**
     * get "send received chunk" property value
     * @param peerName name of peer
     * @param appName name of engine
     * @throws ASAPException
     * @throws IOException
     * */
    public boolean doGetSendReceivedMessage(String peerName, String appName) throws ASAPException, IOException {
        ASAPEngine engine = this.getEngine(peerName, appName);
        return engine.getBehaviourSendReceivedChunks();
    }


    /**
     * create new asap peer
     * @param peerName new peer
     * @throws ASAPException
     * @throws IOException
     * */
    public void doCreateASAPPeer(String peerName) throws ASAPException, IOException {
            this.createPeer(peerName);
    }


    /**
     * create new asap engine (app)
     * @param peer name of peer
     * @param appName name of new app
     * @throws ASAPException
     * @throws IOException
     * */
    public void doCreateASAPApp(String peer, CharSequence appName) throws ASAPException, IOException {
            ASAPPeer asapPeer = this.peers.get(peer);
            if (asapPeer != null) {
                asapPeer.createEngineByFormat(appName);
            }
    }

    /**
     * create new asap channel
     * @param peerName name of peer
     * @param app name of app
     * @param channel new Channel object
     * @throws ASAPException
     * @throws IOException
     * */
    public void doCreateASAPChannel(String peerName, String app, Channel channel) throws ASAPException, IOException {
            CharSequence uri = channel.getUri();
            //get recipients list
            Set<CharSequence> recipients = channel.getRecipients();

            ASAPStorage storage = this.getEngine(peerName, app);

            // finally add peername
            recipients.add(peerName);

            storage.createChannel(peerName, uri, recipients);

    }

    /**
     * reset asap storages
     * @throws Error
     * */
    public void doResetASAPStorages() throws Error {
            ASAPEngineFS.removeFolder(PEERS_ROOT_FOLDER);
            File rootFolder = new File(PEERS_ROOT_FOLDER);
            rootFolder.mkdirs();
            peers.clear();
    }


    /**
     * sub method to get engine
     * @param peername
     * @param appName
     * @throws ASAPException
     * @throws IOException
     * */
    private ASAPEngine getEngine(String peername, String appName) throws ASAPException, IOException {
        ASAPPeer asapPeer = this.peers.get(peername);
        if (asapPeer == null) {
            throw new ASAPException("peer does not exist: " + peername);
        }

        return asapPeer.getEngineByFormat(appName);
    }

    /**
     * sub method to get peer
     * @param peerName
     * @throws ASAPException
     * */
    private ASAPPeer getASAPPeer(String peerName) throws ASAPException {
        ASAPPeer asapPeer = this.peers.get(peerName);
        if (asapPeer == null) {
            throw new ASAPException("engine does not exist: " + peerName);
        }
        return asapPeer;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                      Simulate TCP Connection                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     *  open a server as host
     * @param port port number
     * @param engineName name of peer to handle the connection
     * @throws ASAPException
     * @throws UnknownHostException
     * */
    public ConnectionResponse doOpen(int port, String engineName) throws UnknownHostException, ASAPException {
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
            throw new ASAPException("Error while open a server" + e);

        }
    }

    /**
     * connect to a server
     * @param remoteHost ip/name of server
     * @param remotePort port of server
     * @param engineName name of peer for handling the connection
     * @throws ASAPException
     * */
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
        } catch (Exception e) {
            System.err.println(e);
            throw new ASAPException("Connection to server error " + e );
        }
    }

    /**
     * kill a connection to a server
     * @param host ip / name of server
     * @param port port of server
     * @throws ASAPException
     * */
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
            throw new ASAPException("Counld't close connection " + e );

        }
    }

    /**
     * close a host/server
     * @param port of server
     * @throws ASAPException
     * */
    public void doKillServer(String port) throws ASAPException{
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
            throw new ASAPException("couldn't kill server "+ e);
        }

    }


    /**
     * method to start TCP Stream
     * @param name name of connection.
     * @param stream stream object
     * @param peerName name of peer to handle stream
     * @throws ASAPException
     * */
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

        //check if smt went wrong, if not then return true
        if (!stream.getFatalError()) {
            this.streams.put(name, stream);
            return true;
        }

        throw new ASAPException("couldn't start TCP Stream");

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
            System.out.println("Channel created");
            try {
                this.asapPeer.handleConnection(
                        channel.getInputStream(),
                        channel.getOutputStream());
            } catch (IOException | ASAPException e) {
                System.out.println("call of engine.handleConnection failed: "
                        + e.getLocalizedMessage());
            }
        }
    }




}