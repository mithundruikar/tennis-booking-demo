package com.example.demo.entities.repository;

import com.example.demo.entities.Player;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface PlayerRepository extends CrudRepository<Player, Long> {
    Player findById(long id);
    @Query(value = "SELECT * FROM PLAYER WHERE NAME = ?1 and CONTACT_NUMBER = ?2" , nativeQuery = true)
    Player findByNameAndContact(String name, String contact);
}
