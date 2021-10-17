package com.example.demo.entities.repository;

import com.example.demo.entities.PlayerReservation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface PlayerReservationRepository extends CrudRepository<PlayerReservation, Long> {

    @Query(value = "SELECT * FROM PLAYER_RESERVATION WHERE RESERVATION_DATE = ?1" , nativeQuery = true)
    List<PlayerReservation> findByReservationDate(LocalDate reservationDate);
}
