package com.example.demo.api;

import com.example.demo.model.Peer;
import com.example.demo.model.Storage;
import com.example.demo.model.StorageResponse;
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
import java.util.ArrayList;
import java.util.List;

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
    public Peer createPeer (@Valid @NonNull @NotBlank @RequestParam(value = "name", required = true) String name) throws ASAPException {
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
        List<Storage> returnStorage = new ArrayList<Storage>() ;



        for(CharSequence temp : storage) {
            Storage unit = new Storage(temp);
            returnStorage.add(unit);
        }


        return returnStorage;
    }


}

