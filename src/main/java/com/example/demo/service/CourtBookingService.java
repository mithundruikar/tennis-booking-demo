package com.example.demo.service;

import com.example.demo.entities.CourtBooking;
import com.example.demo.entities.PlayerReservation;
import com.example.demo.entities.repository.CourtBookingRepository;
import com.example.demo.entities.repository.PlayerReservationRepository;
import com.example.demo.processor.BookingRequestQueue;
import com.example.demo.processor.booking.BookingRegister;
import com.example.demo.processor.publisher.BookingPublisher;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/tennis")
public class CourtBookingService {

    private BookingRequestQueue bookingRequestQueue;
    private BookingPublisher bookingPublisher;
    private BookingRegister bookingRegister;
    private CourtBookingRepository courtBookingRepository;
    private PlayerReservationRepository playerReservationRepository;

    @Autowired
    public CourtBookingService(BookingRequestQueue bookingRequestQueue, BookingPublisher bookingPublisher, BookingRegister bookingRegister, CourtBookingRepository courtBookingRepository, PlayerReservationRepository playerReservationRepository) {
        this.bookingRequestQueue = bookingRequestQueue;
        this.bookingPublisher = bookingPublisher;
        this.bookingRegister = bookingRegister;
        this.courtBookingRepository = courtBookingRepository;
        this.playerReservationRepository = playerReservationRepository;
    }

    @PostMapping(value = "/reservations", consumes = "application/json", produces = "application/json")
    @Transactional
    public ReservationResponse addReservation(
            @RequestBody @ApiParam(value = "Json Request {\"name\": \"p1\", \"contact\": \"11\", \"reservationDate\": \"2021-10-18\", \"requestType\": \"ADD\"}", example = "{\"name\": \"p1\", \"contact\": \"11\", \"reservationDate\": \"2021-10-18\", \"requestType\": \"ADD\"}")
            final PlayerReservationRequest playerReservationRequest) {

        return bookingRequestQueue.add(playerReservationRequest);
    }


    @GetMapping("/bookings")
    public List<CourtBooking> getBookings() {
        return StreamSupport.stream(courtBookingRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @GetMapping("/bookings/{id}")
    public Optional<CourtBooking> getBookings(@PathVariable Long id) {
        return courtBookingRepository.findById(id);
    }

    @GetMapping("/bookings/byReservation/{reservationId}")
    public Optional<CourtBooking> getBookingsByReservationId(@PathVariable Long reservationId) {
        return courtBookingRepository.finByReservationId(reservationId);
    }

    @GetMapping("/reservations/{id}")
    public Optional<PlayerReservation> getReservation(@PathVariable Long id) {
        return this.playerReservationRepository.findById(id);
    }

    @GetMapping("/reservations/dates/{reservationDate}")
    public List<PlayerReservation> byPlayerCourtAndDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "reservation date in YYYY-MM-DD format") LocalDate reservationDate) {
        return this.playerReservationRepository.findByReservationDate(reservationDate);
    }

}
