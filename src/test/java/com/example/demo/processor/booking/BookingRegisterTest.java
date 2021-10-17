package com.example.demo.processor.booking;

import com.example.demo.entities.*;
import com.example.demo.entities.repository.CourtBookingRepository;
import com.example.demo.entities.repository.CourtRepository;
import com.example.demo.processor.publisher.BookingPublisher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BookingRegisterTest {

    @Mock
    private CourtBookingRepository courtBookingRepository;
    @Mock
    private BookingPublisher bookingPublisher;
    @Mock
    private CourtRepository courtRepository;
    @Mock
    private TransactionTemplate transactionTemplate;
    @Mock
    private EntityManager entityManager;

    private BookingRegister bookingRegister;

    @Before
    public void setup() {
        this.bookingRegister = new BookingRegister(courtBookingRepository, bookingPublisher, courtRepository, 3, 4, transactionTemplate, entityManager);
        when(courtBookingRepository.save(any(CourtBooking.class))).thenAnswer(ans -> {
            CourtBooking argument = ans.getArgument(0, CourtBooking.class);
            if(argument.getId() == null) {
                argument.setId(Long.valueOf(1));
            }
            return argument;
        });
        when(entityManager.merge(any(CourtBooking.class))).thenAnswer(ans -> ans.getArgument(0));
        when(transactionTemplate.execute(any(TransactionCallback.class))).thenAnswer(ans -> ans.getArgument(0, TransactionCallback.class).doInTransaction(null));

    }


    @Test
    public void registerNewReservation() {
        LocalDate now = LocalDate.now();
        PlayerReservation newReservation = new PlayerReservation(new Player("p1", "c1"), now, RequestType.ADD);
        assertTrue(this.bookingRegister.register(newReservation));
        assertFalse(this.bookingRegister.register(newReservation));
    }

    @Test
    public void shouldConfirmBookingAndPublishOnMatching() {
        LocalDate now = LocalDate.now();
        assertTrue(this.bookingRegister.register(new PlayerReservation(new Player("p1", "c1"), now,  RequestType.ADD)));
        assertTrue(this.bookingRegister.register(new PlayerReservation(new Player("p2", "c1"), now,  RequestType.ADD)));
        assertTrue(this.bookingRegister.register(new PlayerReservation(new Player("p3", "c1"), now,  RequestType.ADD)));
        assertTrue(this.bookingRegister.register(new PlayerReservation(new Player("p4", "c1"), now,  RequestType.ADD)));

        ArgumentCaptor<CourtBooking> courtBookingArgumentCaptor = ArgumentCaptor.forClass(CourtBooking.class);
        verify(bookingPublisher, times(1)).publish(courtBookingArgumentCaptor.capture());

        verify(courtBookingRepository, times(1)).save(any(CourtBooking.class));

        CourtBooking courtBooking = courtBookingArgumentCaptor.getValue();
        Set<PlayerReservation> playerReservations = courtBooking.getPlayerReservations();
        assertEquals(Status.CONFIRMED, courtBooking.getStatus());
        assertTrue(playerReservations.size() == 4);
        List<String> playerNames = playerReservations.stream().map(PlayerReservation::getPlayer).map(Player::getName).collect(Collectors.toList());
        Collections.sort(playerNames);
        assertEquals(Arrays.asList("p1", "p2", "p3", "p4"), playerNames);
    }

    @Test
    public void shouldNotConfirmBookingAndPublishOnNoMatch() {
        LocalDate now = LocalDate.now();
        assertTrue(this.bookingRegister.register(new PlayerReservation(new Player("p1", "c1"), now,  RequestType.ADD)));
        assertTrue(this.bookingRegister.register(new PlayerReservation(new Player("p2", "c1"), now,  RequestType.ADD)));
        assertTrue(this.bookingRegister.register(new PlayerReservation(new Player("p3", "c1"), now,  RequestType.ADD)));
        assertTrue(this.bookingRegister.register(new PlayerReservation(new Player("p4", "c1"), now.plusDays(1),  RequestType.ADD)));

        ArgumentCaptor<CourtBooking> courtBookingArgumentCaptor = ArgumentCaptor.forClass(CourtBooking.class);
        verify(bookingPublisher, times(0)).publish(courtBookingArgumentCaptor.capture());

        verify(courtBookingRepository, times(2)).save(any(CourtBooking.class));
    }

    @Test
    public void testQuickValidation() {
        LocalDate now = LocalDate.now();
        assertTrue(this.bookingRegister.register(new PlayerReservation(new Player("p1", "c1"), now,  RequestType.ADD)));
        assertTrue(this.bookingRegister.register(new PlayerReservation(new Player("p2", "c1"), now,  RequestType.ADD)));
        assertTrue(this.bookingRegister.register(new PlayerReservation(new Player("p3", "c1"), now,  RequestType.ADD)));
        assertTrue(this.bookingRegister.register(new PlayerReservation(new Player("p4", "c1"), now,  RequestType.ADD)));

        assertTrue(this.bookingRegister.register(new PlayerReservation(new Player("p4", "c1"), now.plusDays(1),  RequestType.ADD)));

        // No booking allowed if already booked for the day
        assertFalse(this.bookingRegister.isReservationAllowedFor(now,  new Player("p1", "c1")));
        // should continue booking for other courts
        assertTrue(this.bookingRegister.isReservationAllowedFor(now,  new Player("p6", "c1")));

        // No booking allowed if duplicate reservation found
        assertFalse(this.bookingRegister.isReservationAllowedFor(now.plusDays(1),  new Player("p4", "c1")));

        // allow booking for new player new day
        assertTrue(this.bookingRegister.isReservationAllowedFor(now.plusDays(1),  new Player("p1", "c1")));
        assertTrue(this.bookingRegister.isReservationAllowedFor(now.plusDays(2),  new Player("p1", "c1")));
    }

    @Test
    public void testCourtCyclicCounter() {
        LocalDate now = LocalDate.now();
        BookingRegister.DayCourtBooking dayCourtBooking = this.bookingRegister.new DayCourtBooking(now);
        assertEquals(1, dayCourtBooking.getDayBookings().size());

        assertEquals(Long.valueOf(1), dayCourtBooking.getCurrentCourtId());
        assertEquals(Long.valueOf(2), dayCourtBooking.getNextCourtId());

        dayCourtBooking.getDayBookings().add(new CourtBooking(new Court(dayCourtBooking.getNextCourtId()), now, 4));
        assertEquals(Long.valueOf(2), dayCourtBooking.getCurrentCourtId());
        assertEquals(Long.valueOf(3), dayCourtBooking.getNextCourtId());

        dayCourtBooking.getDayBookings().add(new CourtBooking(new Court(dayCourtBooking.getNextCourtId()), now, 4));
        assertEquals(Long.valueOf(3), dayCourtBooking.getCurrentCourtId());
        assertEquals(Long.valueOf(1), dayCourtBooking.getNextCourtId());
    }

}