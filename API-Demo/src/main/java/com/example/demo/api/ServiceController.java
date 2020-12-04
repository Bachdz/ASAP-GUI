package com.example.demo.api;

import com.example.demo.net.sharksystem.asap.ASAPException;
import com.example.demo.net.sharksystem.asap.ASAPPeer;
import com.example.demo.net.sharksystem.cmdline.ASAPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void createPeer (@Valid @NonNull @RequestParam("name") String name) throws ASAPException {
        asapService.doCreateASAPPeer(name);
    }


    @PostMapping(path = "/app")
    public void createApp (@Valid @NonNull @RequestParam("peer") String name, @RequestParam("app") String app) throws ASAPException {
        asapService.doCreateASAPApp(name,app);
    }

    @GetMapping(path = "/peers")
    public Map<String, String> getPeers () {
        Map<String, String> peers = new HashMap<>();
        List<String> peerStorage= asapService.getPeers();
        for(String peerName : peerStorage) {
            peers.put("name",peerName);
        }
        return peers;
    }





    /*@PostMapping
    public void addPerson (@Valid @NonNull @RequestBody Person person) {
        personService.addPerson(person);
    } */


  /*  @GetMapping
    public List<Person> getAllPerson(){
        return personService.selectAllPeople();
    }

    @GetMapping(path ="{id}")
    public Person getPersonById(@PathVariable("id") UUID id) {
        return personService.getPersonById(id).orElse(null);
    }

    @DeleteMapping(path = "{id}")
    public void deletePersonById(@PathVariable("id") UUID id) {
        personService.deletePersonById(id);
    }


    @PutMapping (path = "{id}")
    public void updatePerson(@PathVariable("id") UUID id,@NotBlank @NotNull @RequestBody Person personToUpdate) {
        personService.updatePerson(id, personToUpdate);
    }
*/

}
