/*
package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PersonService {
    @Autowired
    private  PersonDao personDao;



  */
/*  public PersonService(@Qualifier("fakeDao") PersonDao personDao) {
        this.personDao = personDao;
    }*//*


    public int addPerson (Person person) {
        return personDao.insertPerson(person);
    }

    public List<Person> selectAllPeople () {

     return personDao.selectAllPeople();

    }


    public Optional<Person> getPersonById(UUID id) {
        return personDao.selectPersonById(id);
    }

    public int deletePersonById(UUID id) {
        return personDao.deletePersonbyID(id);
    }

    public int updatePerson(UUID id, Person newPerson) {
        return personDao.updatePersonbyID(id, newPerson);
    }

}
*/
