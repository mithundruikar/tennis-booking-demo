package com.example.demo.processor;

import com.example.demo.entities.Player;
import com.example.demo.entities.PlayerReservation;
import com.example.demo.processor.booking.BookingRegister;
import com.example.demo.service.PlayerReservationRequest;
import com.example.demo.service.ReservationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BookingRequestQueue {
    private static final Logger log = LoggerFactory.getLogger(BookingRequestQueue.class);

    private BlockingQueue<PlayerReservation> queue;
    private BookingRegister bookingRegister;
    private EntityManager entityManager;
    private TransactionTemplate transactionTemplate;
    private ReservationEntityResolver reservationEntityResolver;

    public BookingRequestQueue(BookingRegister bookingRegister, EntityManager entityManager, TransactionTemplate transactionTemplate, ReservationEntityResolver reservationEntityResolver) {
        // THIS CAN BE EASILY BE EVENT SOURCED/RECOVERED FROM PlayerReservations from DB
        this.queue = new LinkedBlockingQueue<>(1000);

        this.bookingRegister = bookingRegister;
        this.entityManager = entityManager;
        this.transactionTemplate = transactionTemplate;
        this.reservationEntityResolver = reservationEntityResolver;
    }

    public ReservationResponse add(PlayerReservationRequest playerReservation) {
        if(!bookingRegister.isReservationAllowedFor(playerReservation.getReservationDate(), new Player(playerReservation.getName(), playerReservation.getContact()))) {
            return new ReservationResponse("Reservation now allowed for this combination. Courts are either fully booked or reservation request already exists", false);
        }

        try {
            // RECORD player reservation request first for RECOVERY PURPOSE
            PlayerReservation recordedReservation = this.transactionTemplate.execute(status ->  {
                PlayerReservation resolve = this.reservationEntityResolver.resolve(playerReservation);
                return entityManager.merge(resolve);
            });
            if(Objects.isNull(recordedReservation)) {
                return new ReservationResponse("Error occured while accepting request. Please try again later.", false);
            }
            boolean offered = this.queue.offer(recordedReservation);
            if(!offered) {
                return new ReservationResponse("System is not able to accept new reservations. Please try again later.", false);
            }
            return new ReservationResponse(String.format("Reservation successfully accepted. You will be notified upon booking confirmation. Id for reference %s", recordedReservation.getId()),  true);
        } catch (RuntimeException er) {
            log.error("Error while persisting reservation request {}", playerReservation, er);
            throw er;
        }

    }

    public BlockingQueue<PlayerReservation> getQueue() {
        return queue;
    }
}
