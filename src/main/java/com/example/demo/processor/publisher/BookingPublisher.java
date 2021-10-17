package com.example.demo.processor.publisher;

import com.example.demo.entities.CourtBooking;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class BookingPublisher {

    private static final Logger log = LoggerFactory.getLogger(BookingPublisher.class);

    private List<Consumer<CourtBooking>> consumers = new ArrayList<>();

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        executorService = Executors.newSingleThreadExecutor();
    }

    public void publish(CourtBooking courtBooking) {
        log.info("Booking confirmed for {}", courtBooking);
        executorService.submit(() -> consumers.forEach(consumer -> consumer.accept(courtBooking)));
    }


    @VisibleForTesting
    public void addListener(Consumer<CourtBooking> bookingConsumer) {
        consumers.add(bookingConsumer);
    }

    @VisibleForTesting
    public void clearListeners() {
        consumers.clear();
    }
}
