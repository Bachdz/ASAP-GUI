package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.service.asap.ASAPException;
import com.example.demo.service.ASAPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

@RequestMapping("api/v1/asap")
@RestController
public class ServiceController {

    @Autowired
    private final ASAPService asapService;

    @Autowired
    public ServiceController(ASAPService asapService) {
        this.asapService = asapService;
    }



    @PostMapping(path = "/peer")
    public Peer createPeer (@Valid @NonNull @NotBlank @RequestParam(value = "name") String name)  {
        if (name.equals("")) {
            throw new IllegalArgumentException("{\"error\":\"Parameter is invalid\"}");
        }
        try {
            asapService.doCreateASAPPeer(name);
            Peer peer = new Peer(name);
             return peer;
        } catch (ASAPException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
        }
    }


    @PostMapping(path = "/app")
    public App createApp (@Valid @NonNull @RequestParam("peer") String name, @RequestParam("app") CharSequence app)  {
        if (name.equals("") || app.equals("")) {
            throw new IllegalArgumentException("{\"error\":\"Parameter is invalid\"}");
        }
        try {
            asapService.doCreateASAPApp(name, app);
            App storage = new App(app);
            return storage;
        } catch (ASAPException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
        }
    }


    @PostMapping(path = "/channel")
    public Channel createChannel (@RequestBody Channel newChannel, @Valid @NonNull @RequestParam("peer") String peerName, @Valid @NonNull @RequestParam("app") String appName) {
           try {
               asapService.doCreateASAPChannel(peerName,appName,newChannel);
               return newChannel;
           } catch (ASAPException e) {
               throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
           }

    }

    @PostMapping(path = "/addmessages")
    public Message createMessages (@RequestBody Message newMessage, @Valid @NonNull @RequestParam("peer") String peerName, @Valid @NonNull @RequestParam("app") String appName, @Valid @NonNull @RequestParam("uri") String uri) {
           try {
               asapService.doCreateASAPMessages(newMessage,peerName,appName,uri);
               return newMessage;
           } catch (ASAPException | IOException e) {
               throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());

           }

    }

    @PostMapping(path = "/terminateconnection")
    public boolean terminateConnection (@Valid @NonNull @RequestParam("host") String host, @Valid @NonNull @RequestParam("port") int port) {
        try {
            asapService.doKillConnectionAttempt(host,port);
            return true;
        } catch (ASAPException e) {
//            System.err.println("Something went wrong: " + e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
        }
    }


    @PostMapping(path = "/terminate")
    public boolean terminateServer (@Valid @NonNull @RequestParam("port") String port) {
        try {
            asapService.doKillServer(port);
            return true;
        } catch (Exception e) {
            System.err.println("Something went wrong" + e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
//            return false;
        }
    }




    @PostMapping(path = "/openconnection")
    public ConnectionResponse openConnection (@Valid @NonNull @RequestParam("port") int port, @Valid @NonNull @RequestParam("peer") String peerName) {
           try {
               return  asapService.doOpen(port,peerName);
           } catch (ASAPException | UnknownHostException e) {
               System.err.println("Something went wrong" + e);
               throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
           }

    }

    @PostMapping(path = "/connect")
    public ConnectionResponse doConnect (@Valid @NonNull @RequestParam("host") String host, @Valid @NonNull @RequestParam("port") int port,@Valid @NonNull @RequestParam("peer") String peerName) {
           try {
               return  asapService.doConnect(host,port,peerName);
           } catch (ASAPException e) {
               System.err.println("Something went wrong" + e);
               throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
           }
    }


    @GetMapping(path = "/start")
    public boolean getStart (){
        try {
            asapService.doStart();
            return true;
        } catch (IOException |ASAPException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
        }
    }



    @GetMapping(path = "/peers")
    public List<Peer> getPeers () {
        List<String> peerStorage= asapService.getPeers();
        List<Peer> peers = new ArrayList<Peer>() ;
        for(String peerName : peerStorage) {
           Peer peer = new Peer(peerName);
            peers.add(peer);
        }
        return peers;
    }

    @GetMapping(path = "/logdata")
    public List<String> getLogData ()  {
        try {
            List<String> logData = asapService.getConsoleLog();
            return logData;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
        }
    }



    @GetMapping(path = "/received")
    public List<Received> getReceivedMess (@Valid @NonNull @NotBlank @RequestParam(value = "peer") String peer, @Valid @NonNull @NotBlank @RequestParam(value = "storage")String storage, @Valid @NonNull @NotBlank @RequestParam(value = "uri")String uri) {
     try {
         List<Received> received = asapService.getReceivedMessages(peer,storage,uri);
         return received;
     } catch (IOException |ASAPException e) {
         throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());

     }
    }

    @DeleteMapping (path = "/peers")
    public boolean  resetPeers () {
        try {
            asapService.doResetASAPStorages();
            return true;
        } catch (Error e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
        }
    }

    @GetMapping(path = "/activatemess")
    public boolean doActivateOnlineMess (@Valid @NonNull @NotBlank @RequestParam(value = "peer", required = true) String peer) {
       try {
           asapService.doActivateOnlineMessages(peer);
           return true;
       } catch (Exception e) {
           throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
       }
       }
    @GetMapping(path = "/deactivatemess")
    public boolean doDeactivateOnlineMess (@Valid @NonNull @NotBlank @RequestParam(value = "peer", required = true) String peer) {
        try {
            asapService.doDectivateOnlineMessages(peer);
            return true;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
        }
    }


    @GetMapping(path = "/setsendreceived")
    public boolean doSetSendReceived (@Valid @NonNull @NotBlank @RequestParam(value = "peer") String peer, @Valid @NonNull @NotBlank @RequestParam(value = "storage") String storage,@Valid @NonNull @NotBlank @RequestParam(value = "value") boolean value) {
        try {
            asapService.doSetSendReceivedMessage(peer,storage,value);
            return true;
        } catch (ASAPException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
        }
    }
    @GetMapping(path = "/getsendreceived")
    public boolean doGetSendReceived (@Valid @NonNull @NotBlank @RequestParam(value = "peer") String peer, @Valid @NonNull @NotBlank @RequestParam(value = "storage") String storage) {
        try {
           return asapService.doGetSendReceivedMessage(peer,storage);
        } catch (ASAPException | IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
        }
    }



    @GetMapping(path = "/storages")
    public List<App> getStorages (@Valid @NonNull @NotBlank @RequestParam(value = "peer", required = true) String peer) {
        return  asapService.getStorages(peer);
    }


    @GetMapping(path = "/era")
    public int getEras (@Valid @NonNull @NotBlank @RequestParam(value = "peer") String peer, @Valid @NonNull @NotBlank @RequestParam(value = "storage") String storage){
        try{
            int era = asapService.getCurrentEra(peer,storage);
            return era;
        } catch (IOException | ASAPException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());

        }
    }


    @GetMapping(path = "/channels")
    public List<Channel> getChannels (@Valid @NonNull @NotBlank @RequestParam(value = "peer") String peer, @Valid @NonNull @NotBlank @RequestParam(value = "storage") String storage) throws IOException, ASAPException {
        return asapService.getChannels(peer, storage);
    }

    @GetMapping(path = "/messages")
    public List<Chunk> getMessages (@Valid @NonNull @NotBlank @RequestParam(value = "peer") String peer, @Valid @NonNull @NotBlank @RequestParam (value = "storage")String storage , @Valid @NonNull @NotBlank @RequestParam(value = "uri") String uri) {
        try {
            return asapService.getMessagesByChunk(peer, storage,uri);
        } catch (IOException | ASAPException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
        }
    }

}

