package com.example.demo.api;

import com.example.demo.model.*;
import com.example.demo.net.sharksystem.asap.ASAPException;
import com.example.demo.net.sharksystem.asap.ASAPPeer;
import com.example.demo.net.sharksystem.cmdline.ASAPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.*;

@RequestMapping("api/v1/asap")
@CrossOrigin
@RestController
public class ServiceController {

    @Autowired
    private final ASAPService asapService;

    @Autowired
    public ServiceController(ASAPService asapService) {
        this.asapService = asapService;
    }



    @PostMapping(path = "/peer")
    public Peer createPeer (@Valid @NonNull @NotBlank @RequestParam(value = "name") String name) throws ASAPException {
        if (name.equals("")) {
            throw new IllegalArgumentException("{\"error\":\"Parameter is invalid\"}");
        }
        Peer peer = null;
        try {
            asapService.doCreateASAPPeer(name);
             peer = new Peer(name);
        } catch (ASAPException e) {
            throw new Error(e);
        }
        return peer;
    }


    @PostMapping(path = "/app")
    public Storage createApp (@Valid @NonNull @RequestParam("peer") String name, @RequestParam("app") CharSequence app) throws ASAPException {
        if (name.equals("") || app.equals("")) {
            throw new IllegalArgumentException("{\"error\":\"Parameter is invalid\"}");
        }
        Storage storage= null;
        try {
            asapService.doCreateASAPApp(name, app);
            storage = new Storage(app);
        } catch (ASAPException e) {
            throw new Error(e);
        }
        return storage;
    }


    @PostMapping(path = "/channel")
    public Channel createChannel (@RequestBody Channel newChannel, @Valid @NonNull @RequestParam("peer") String peerName, @Valid @NonNull @RequestParam("app") String appName) throws ASAPException {
           try {
               asapService.doCreateASAPChannel(peerName,appName,newChannel);
           } catch (ASAPException e) {
               newChannel = null;
               System.err.println("Something went wrong" + e);
           }

            return newChannel;
    }

    @PostMapping(path = "/addmessages")
    public Mess createMessages (@RequestBody Mess newMess, @Valid @NonNull @RequestParam("peer") String peerName, @Valid @NonNull @RequestParam("app") String appName,@Valid @NonNull @RequestParam("uri") String uri) throws ASAPException {
           try {
                System.out.println(newMess.toString());
               asapService.doCreateASAPMessages(newMess,peerName,appName,uri);
           } catch (ASAPException | IOException e) {
               newMess = null;
               System.err.println("Something went wrong" + e);
           }

            return newMess;
    }


    @GetMapping(path = "/start")
    public void getStart () throws IOException, ASAPException {
      asapService.doStart();
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
    public List<String> getLogData () throws IOException {
        List<String> logData= asapService.getConsoleLog();

        return logData;
    }


    @DeleteMapping (path = "/peers")
    public boolean  resetPeers () {
        try {
            asapService.doResetASAPStorages();
            return true;
        } catch (Error e) {
            return false;
        }
    }




    @GetMapping(path = "/storages")
    public List<Storage> getStorages (@Valid @NonNull @NotBlank @RequestParam(value = "peer", required = true) String peer) {
        List<CharSequence> storage= asapService.getStorages(peer);
        List<Storage> returnStorage = new ArrayList<>() ;



        for(CharSequence temp : storage) {
            Storage unit = new Storage(temp);
            returnStorage.add(unit);
        }


        return returnStorage;
    }


    @GetMapping(path = "/eras")
    public Collection<Integer> getEras (@Valid @NonNull @NotBlank @RequestParam(value = "peer") String peer, @Valid @NonNull @NotBlank @RequestParam(value = "storage") String storage){
        Collection<Integer> eras = asapService.doGetEras(peer,storage);
        return eras ;
    }


    @GetMapping(path = "/channels")
    public List<Channel> getChannels (@Valid @NonNull @NotBlank @RequestParam(value = "peer") String peer, @Valid @NonNull @NotBlank @RequestParam(value = "storage") String storage) throws IOException, ASAPException {
        return asapService.getChannels(peer, storage);
    }

    @GetMapping(path = "/messages")
    public Iterator<CharSequence> getMessages (@Valid @NonNull @NotBlank @RequestParam(value = "peer") String peer, @Valid @NonNull @NotBlank @RequestParam (value = "storage")String storage , @Valid @NonNull @NotBlank @RequestParam(value = "uri") String uri) throws IOException, ASAPException {
        return asapService.getMessages(peer, storage,uri);
    }

}

