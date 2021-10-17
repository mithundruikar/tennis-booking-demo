package com.example.demo;

import com.example.demo.entities.*;
import com.example.demo.processor.BookingRequestQueue;
import com.example.demo.processor.publisher.BookingPublisher;
import com.example.demo.service.PlayerReservationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import java.time.LocalDate;

@SpringBootApplication
public class DemoApplication {
	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}


	/*@Bean
	public CommandLineRunner demo(ApplicationContext applicationContext) {
		BookingRequestQueue bookingRequestQueue = applicationContext.getBean(BookingRequestQueue.class);
        BookingPublisher bookingPublisher = applicationContext.getBean(BookingPublisher.class);

		return (args) -> {

            Court court1 = new Court(1l);

            bookingPublisher.addListener(booking -> {
                boolean duplicateAccepted = sendTestReservation(PlayerReservationRequest.builder().name("p1").contact( "c1").requestType(RequestType.ADD).reservationDate(LocalDate.now()).build(),  bookingRequestQueue);
                log.info("Booking request after duplicate accepted {}", duplicateAccepted);

                PlayerReservationRequest build = PlayerReservationRequest.builder().name("p1").contact( "c1").requestType(RequestType.ADD).reservationDate(LocalDate.now().plusDays(1)).build();

                boolean samePlayerRequestForAnotherDay = bookingRequestQueue.add(build).isSuccess();
                log.info("Separate request for another day accepted {}", samePlayerRequestForAnotherDay);
            });


            log.info("reservation 1 accepted {}", sendTestReservation(PlayerReservationRequest.builder().name("p1").contact( "c1").requestType(RequestType.ADD).reservationDate(LocalDate.now()).build(),  bookingRequestQueue));
            log.info("reservation 2 accepted {}", sendTestReservation(PlayerReservationRequest.builder().name("p2").contact( "c1").requestType(RequestType.ADD).reservationDate(LocalDate.now()).build(),  bookingRequestQueue));
            log.info("reservation 3 accepted {}", sendTestReservation(PlayerReservationRequest.builder().name("p3").contact( "c1").requestType(RequestType.ADD).reservationDate(LocalDate.now()).build(),  bookingRequestQueue));
            log.info("reservation 4 accepted {}", sendTestReservation(PlayerReservationRequest.builder().name("p4").contact( "c1").requestType(RequestType.ADD).reservationDate(LocalDate.now()).build(),  bookingRequestQueue));

            log.info("Done");
		};
	}*/

	private Boolean sendTestReservation(PlayerReservationRequest playerReservation, BookingRequestQueue bookingRequestQueue) {
        return bookingRequestQueue.add(playerReservation).isSuccess();
    }

}
