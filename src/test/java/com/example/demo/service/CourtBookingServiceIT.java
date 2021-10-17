package com.example.demo.service;

import com.example.demo.DemoApplication;
import com.example.demo.entities.*;
import com.example.demo.processor.BookingRequestQueue;
import com.example.demo.processor.publisher.BookingPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CourtBookingServiceIT {

    @LocalServerPort
    private int port;

    @Autowired
    private BookingRequestQueue bookingRequestQueue;

    @Autowired
    private BookingPublisher bookingPublisher;

    private ObjectMapper objectMapper;
    private TestRestTemplate restTemplate;

    private BlockingQueue<CourtBooking> confirmedBookings;

    @Before
    public void setup() {
        confirmedBookings = new ArrayBlockingQueue(100);
        restTemplate = new TestRestTemplate();
        objectMapper = new ObjectMapper().registerModule(new ParameterNamesModule()).registerModule(new Jdk8Module()).registerModule(new JavaTimeModule());

        bookingPublisher.clearListeners();
        bookingPublisher.addListener(confirmedBookings::add);
    }


    @Test
    public void shouldBookCourtWithFourBookings() throws Exception {
        LocalDate bookingDate = LocalDate.of(2021, 10, 30);
        PlayerReservationRequest request1 = new PlayerReservationRequest("P1", "112",   bookingDate, RequestType.ADD);
        PlayerReservationRequest request2 = new PlayerReservationRequest("P2", "112",   bookingDate, RequestType.ADD);
        PlayerReservationRequest request3 = new PlayerReservationRequest("P3", "112",   bookingDate, RequestType.ADD);
        PlayerReservationRequest request4 = new PlayerReservationRequest("P4", "112",   bookingDate, RequestType.ADD);

        assertTrue(getPostResponse(request1, "/api/tennis/reservations", ReservationResponse.class).isSuccess());
        assertTrue(getPostResponse(request2, "/api/tennis/reservations", ReservationResponse.class).isSuccess());
        assertTrue(getPostResponse(request3, "/api/tennis/reservations", ReservationResponse.class).isSuccess());
        assertTrue(getPostResponse(request4, "/api/tennis/reservations", ReservationResponse.class).isSuccess());

        CourtBooking poll = confirmedBookings.poll(1000, TimeUnit.SECONDS);
        assertEquals(Status.CONFIRMED, poll.getStatus());
        assertEquals(new Court(1l), poll.getCourt());
        assertEquals(bookingDate, poll.getBookingDate());
        assertEquals(4, poll.getPlayerReservations().size());
        assertTrue(confirmedBookings.isEmpty());
    }


    @Test
    public void showRejectRequestIfAllCourtsAreBookedForTheDay() throws Exception {
        LocalDate bookingDate = LocalDate.of(2021, 11, 30);
        PlayerReservationRequest request1 = new PlayerReservationRequest("P1", "112",   bookingDate, RequestType.ADD);
        PlayerReservationRequest request2 = new PlayerReservationRequest("P2", "112",   bookingDate, RequestType.ADD);
        PlayerReservationRequest request3 = new PlayerReservationRequest("P3", "112",   bookingDate, RequestType.ADD);
        PlayerReservationRequest request4 = new PlayerReservationRequest("P4", "112",   bookingDate, RequestType.ADD);

        assertTrue(getPostResponse(request1, "/api/tennis/reservations", ReservationResponse.class).isSuccess());
        assertTrue(getPostResponse(request2, "/api/tennis/reservations", ReservationResponse.class).isSuccess());
        assertTrue(getPostResponse(request3, "/api/tennis/reservations", ReservationResponse.class).isSuccess());
        assertTrue(getPostResponse(request4, "/api/tennis/reservations", ReservationResponse.class).isSuccess());

        CourtBooking poll = confirmedBookings.poll(10, TimeUnit.SECONDS);
        assertEquals(Status.CONFIRMED, poll.getStatus());
        assertEquals(new Court(1l), poll.getCourt());
        assertEquals(bookingDate, poll.getBookingDate());
        assertEquals(4, poll.getPlayerReservations().size());
        assertTrue(confirmedBookings.isEmpty());

        PlayerReservationRequest request5 = new PlayerReservationRequest("P5", "112",   bookingDate, RequestType.ADD);
        PlayerReservationRequest request6 = new PlayerReservationRequest("P6", "112",   bookingDate, RequestType.ADD);
        PlayerReservationRequest request7 = new PlayerReservationRequest("P7", "112",   bookingDate, RequestType.ADD);
        PlayerReservationRequest request8 = new PlayerReservationRequest("P8", "112",   bookingDate, RequestType.ADD);

        assertTrue(getPostResponse(request5, "/api/tennis/reservations", ReservationResponse.class).isSuccess());
        assertTrue(getPostResponse(request6, "/api/tennis/reservations", ReservationResponse.class).isSuccess());
        assertTrue(getPostResponse(request7, "/api/tennis/reservations", ReservationResponse.class).isSuccess());
        assertTrue(getPostResponse(request8, "/api/tennis/reservations", ReservationResponse.class).isSuccess());

        CourtBooking poll2 = confirmedBookings.poll(10, TimeUnit.SECONDS);
        assertEquals(Status.CONFIRMED, poll2.getStatus());
        assertEquals(new Court(2l), poll2.getCourt());
        assertEquals(bookingDate, poll2.getBookingDate());
        assertEquals(4, poll2.getPlayerReservations().size());
        assertTrue(confirmedBookings.isEmpty());


        PlayerReservationRequest request9 = new PlayerReservationRequest("P9", "112",   bookingDate, RequestType.ADD);
        PlayerReservationRequest request10 = new PlayerReservationRequest("P10", "112",   bookingDate, RequestType.ADD);
        PlayerReservationRequest request11 = new PlayerReservationRequest("P11", "112",   bookingDate, RequestType.ADD);
        PlayerReservationRequest request12 = new PlayerReservationRequest("P12", "112",   bookingDate, RequestType.ADD);

        assertTrue(getPostResponse(request9, "/api/tennis/reservations", ReservationResponse.class).isSuccess());
        assertTrue(getPostResponse(request10, "/api/tennis/reservations", ReservationResponse.class).isSuccess());
        assertTrue(getPostResponse(request11, "/api/tennis/reservations", ReservationResponse.class).isSuccess());
        assertTrue(getPostResponse(request12, "/api/tennis/reservations", ReservationResponse.class).isSuccess());

        CourtBooking poll3 = confirmedBookings.poll(10, TimeUnit.SECONDS);
        assertEquals(Status.CONFIRMED, poll3.getStatus());
        assertEquals(new Court(3l), poll3.getCourt());
        assertEquals(bookingDate, poll3.getBookingDate());
        assertEquals(4, poll3.getPlayerReservations().size());
        assertTrue(confirmedBookings.isEmpty());


        // SHOULD REJECT request further
        PlayerReservationRequest request13 = new PlayerReservationRequest("P13", "112",   bookingDate, RequestType.ADD);
        assertFalse(getPostResponse(request13, "/api/tennis/reservations", ReservationResponse.class).isSuccess());
    }


    @Test
    public void showRejectSecondRequestForSameDayForSamePlayer() throws Exception {
        LocalDate bookingDate = LocalDate.of(2021, 10, 31);
        PlayerReservationRequest request1 = new PlayerReservationRequest("P1", "112",   bookingDate, RequestType.ADD);
        assertTrue(getPostResponse(request1, "/api/tennis/reservations", ReservationResponse.class).isSuccess());
        assertFalse(getPostResponse(request1, "/api/tennis/reservations", ReservationResponse.class).isSuccess());
    }

    private <T, R> R getPostResponse(T body, String path, Class<R> clazz) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<T> entity = new HttpEntity(body, headers);
        ResponseEntity<String> response =  restTemplate.exchange(
                createURLWithPort(path),
                HttpMethod.POST, entity, String.class);
        return objectMapper.readValue(response.getBody(), clazz);
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

}