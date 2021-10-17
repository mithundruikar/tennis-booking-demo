package com.example.demo.entities;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class CourtBookingTest {

    @Test
    public void testEquality() {
        LocalDate now = LocalDate.now();
        CourtBooking courtBooking1 = new CourtBooking(new Court(1l), now, 4);
        CourtBooking courtBooking2 = new CourtBooking(new Court(1l), now, 4);
        assertEquals(courtBooking1, courtBooking2);

        assertTrue(courtBooking1.addPlayerReservation(new PlayerReservation(new Player("p1", "c1"), now, RequestType.ADD)));
        assertTrue(courtBooking1.addPlayerReservation(new PlayerReservation(new Player("p2", "c1"), now, RequestType.ADD)));
        assertTrue(courtBooking1.addPlayerReservation(new PlayerReservation(new Player("p3", "c1"), now, RequestType.ADD)));

        // duplicate entries attempt
        assertFalse(courtBooking1.addPlayerReservation(new PlayerReservation(new Player("p1", "c1"), now, RequestType.ADD)));

        assertTrue(courtBooking1.addPlayerReservation(new PlayerReservation(new Player("p4", "c1"), now, RequestType.ADD)));

        assertEquals(Status.CONFIRMED, courtBooking1.getStatus());

        assertFalse(courtBooking1.addPlayerReservation(new PlayerReservation(new Player("p5", "c1"), now, RequestType.ADD)));


    }
}