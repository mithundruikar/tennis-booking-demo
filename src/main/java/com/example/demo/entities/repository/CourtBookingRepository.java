package com.example.demo.entities.repository;

import com.example.demo.entities.CourtBooking;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CourtBookingRepository  extends CrudRepository<CourtBooking, Long> {
    CourtBooking findById(long id);

    @Query(value = "SELECT * FROM COURT_BOOKING CB INNER JOIN BOOKING_RESERVATION_MAPPING BRM ON CB.ID = BRM.BOOKING_ID WHERE BRM.RESERVATION_ID = ?1" , nativeQuery = true)
    Optional<CourtBooking> finByReservationId(Long reservationId);
}
