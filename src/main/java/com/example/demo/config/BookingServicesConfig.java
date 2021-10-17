package com.example.demo.config;

import com.example.demo.entities.repository.CourtBookingRepository;
import com.example.demo.entities.repository.CourtRepository;
import com.example.demo.entities.repository.PlayerRepository;
import com.example.demo.processor.BookingRequestQueue;
import com.example.demo.processor.ReservationEntityResolver;
import com.example.demo.processor.booking.BookingRegister;
import com.example.demo.processor.publisher.BookingPublisher;
import com.example.demo.processor.task.BookingProcessorTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;

@Configuration
public class BookingServicesConfig {

    @Bean
    public BookingPublisher bookingPublisher() {
        return new BookingPublisher();
    }

    @Bean
    public BookingRegister bookingRegister(CourtBookingRepository courtBookingRepository, BookingPublisher bookingPublisher, CourtRepository courtRepository, TransactionTemplate transactionTemplate, EntityManager entityManager) {
        return new BookingRegister(courtBookingRepository, bookingPublisher, courtRepository, 3, 4, transactionTemplate, entityManager);
    }

    @Bean
    public ReservationEntityResolver reservationEntityResolver(PlayerRepository playerRepository) {
        return new ReservationEntityResolver(playerRepository);
    }

    @Bean
    public BookingRequestQueue bookingRequestQueue(BookingRegister bookingRegister, EntityManager entityManager, TransactionTemplate transactionTemplate, ReservationEntityResolver reservationEntityResolver) {
        return new BookingRequestQueue(bookingRegister, entityManager, transactionTemplate, reservationEntityResolver);
    }

    @Bean
    public BookingProcessorTask bookingProcessorTask(BookingRequestQueue bookingRequestQueue, BookingRegister bookingRegister) {
        return new BookingProcessorTask(bookingRequestQueue, bookingRegister);
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
