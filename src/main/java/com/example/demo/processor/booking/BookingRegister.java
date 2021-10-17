package com.example.demo.processor.booking;

import com.example.demo.entities.*;
import com.example.demo.entities.repository.CourtBookingRepository;
import com.example.demo.entities.repository.CourtRepository;
import com.example.demo.processor.publisher.BookingPublisher;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

public class BookingRegister {

    private Map<Long, DayCourtBooking> dayBookingMap;
    private CourtBookingRepository courtBookingRepository;
    private BookingPublisher bookingPublisher;
    private CourtRepository courtRepository;
    private final int playersPerCourt;
    private final int availableCourts;
    private TransactionTemplate transactionTemplate;

    public BookingRegister(CourtBookingRepository courtBookingRepository, BookingPublisher bookingPublisher, CourtRepository courtRepository, int availableCourts, int playersPerCourt, TransactionTemplate transactionTemplate) {
        assert availableCourts > 0;
        assert playersPerCourt > 0;

        this.courtBookingRepository = courtBookingRepository;
        this.bookingPublisher = bookingPublisher;
        this.dayBookingMap = new ConcurrentHashMap<>(); // ASSUMPTION is that there io DB RECOVERY required.
        // else it can be easily done in post init with DB queries to recover DB state into in-memory register
        this.courtRepository = courtRepository;
        this.availableCourts = availableCourts;
        this.playersPerCourt = playersPerCourt;
        this.transactionTemplate = transactionTemplate;
    }

    @PostConstruct
    public void init() {
        this.transactionTemplate.execute( st -> {
            IntStream.range(1, availableCourts + 1).boxed()
                    .forEach(courtId -> courtRepository.save(new Court(Long.valueOf(courtId))));
            return true;
        });

        recoverStateFromDb();
    }

    private void recoverStateFromDb() {
        // TODO - recover the booking register if there is any issue noticed during persistence earleir.
    }

    public boolean isReservationAllowedFor(LocalDate reservationDate, Player player) {
        long dateIndex = reservationDate.toEpochDay();
        DayCourtBooking bookings = dayBookingMap.get(dateIndex);
        if(bookings != null && !bookings.getDayBookings().isEmpty()) {
            boolean playerHasBookingForTheDay = bookings.getDayBookings().stream().anyMatch(booking -> booking.contains(player, reservationDate));
            CourtBooking ongoingBooking = bookings.getDayBookings().get(bookings.getDayBookings().size() - 1);
            return !playerHasBookingForTheDay && !Status.CONFIRMED.equals(ongoingBooking.getStatus());
        }
        return true;
    } 
    
    public boolean register(PlayerReservation newReservation) {
        long dateIndex = newReservation.getReservationDate().toEpochDay();
        boolean allowed = isReservationAllowedFor(newReservation.getReservationDate(), newReservation.getPlayer());
        if(!allowed) {
            return false;
        }

        DayCourtBooking dayBookings = dayBookingMap.computeIfAbsent(dateIndex, k -> {
            DayCourtBooking dayCourtBooking = new DayCourtBooking(newReservation.getReservationDate());
            return dayCourtBooking;
        });

        Court court = new Court(Long.valueOf(dayBookings.getCurrentCourtId()));
        CourtBooking courtBooking = dayBookings.getDayBookings().get((int) court.getId() - 1);

        boolean registered = courtBooking.addPlayerReservation(newReservation);

        if(registered && Status.CONFIRMED.equals(courtBooking.getStatus())) {
            prepareNextCourtBooking(newReservation.getReservationDate(), dayBookings);
            CourtBooking persistedBooking = courtBookingRepository.save(courtBooking);
            bookingPublisher.publish(persistedBooking);
            return true;
        }

        return registered;
    }

    private void prepareNextCourtBooking(LocalDate reservationDate, DayCourtBooking dayBookings) {
        Long nextCourtId = dayBookings.getNextCourtId();
        if(dayBookings.getDayBookings().size() < nextCourtId) {
            dayBookings.getDayBookings().add(new CourtBooking(new Court(Long.valueOf(nextCourtId)), reservationDate, playersPerCourt));
        }
    }

    public class DayCourtBooking {
        CopyOnWriteArrayList<CourtBooking> dayBookings;

        public DayCourtBooking(LocalDate day) {
            dayBookings = new CopyOnWriteArrayList<>();
            dayBookings.add(new CourtBooking(new Court(1l), day, BookingRegister.this.playersPerCourt));
        }

        public CopyOnWriteArrayList<CourtBooking> getDayBookings() {
            return dayBookings;
        }

        public Long getCurrentCourtId() {
            return Long.valueOf(dayBookings.size());
        }

        public Long getNextCourtId() {
            int nextCourtIndex = dayBookings.size() % availableCourts;
            return Long.valueOf(nextCourtIndex + 1);
        }
    }
}
