package com.example.demo.api;

import com.example.demo.net.sharksystem.asap.ASAPException;
import com.example.demo.net.sharksystem.cmdline.CmdLineUI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("api/v1/asap")
@RestController
public class ServiceController {

    @Autowired
    private final CmdLineUI cmdService;

    @Autowired
    public ServiceController(CmdLineUI cmdService) {
        this.cmdService = cmdService;
    }



    @PostMapping(path = "/peer")
    public void createPeer (@Valid @NonNull @RequestParam("name") String name) throws ASAPException {
        cmdService.doCreateASAPPeer(name);
    }


    @PostMapping(path = "/app")
    public void createApp (@Valid @NonNull @RequestParam("peer") String name, @RequestParam("app") String app) throws ASAPException {
        cmdService.doCreateASAPApp(name,app);
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
