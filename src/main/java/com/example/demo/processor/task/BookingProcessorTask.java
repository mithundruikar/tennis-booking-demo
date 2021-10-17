package com.example.demo.processor.task;

import com.example.demo.entities.PlayerReservation;
import com.example.demo.processor.BookingRequestQueue;
import com.example.demo.processor.booking.BookingRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BookingProcessorTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(BookingProcessorTask.class);

    private BookingRequestQueue bookingRequestQueue;
    private BookingRegister bookingRegister;
    private ExecutorService executorService;
    private volatile boolean stop;

    private TransactionTemplate transactionTemplate;

    public BookingProcessorTask(BookingRequestQueue bookingRequestQueue, BookingRegister bookingRegister, TransactionTemplate transactionTemplate) {
        this.bookingRequestQueue = bookingRequestQueue;
        this.bookingRegister = bookingRegister;
        stop = false;
        this.transactionTemplate = transactionTemplate;
    }

    @PostConstruct
    public void init() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this);
    }

    public void stop() {
        this.stop = true;
        this.executorService.shutdown();
    }

    @Override
    public void run() {
        while(!stop) {
            forceRun();
        }
    }

    protected void forceRun() {
        PlayerReservation poll = null;
        try {
            poll = this.bookingRequestQueue.getQueue().poll(1, TimeUnit.SECONDS);
            if(poll != null) {
                processRequest(poll);
            }
        } catch (Exception e) {
           log.warn("error in the polling task {}. continuing next poll", poll, e);
        }
    }

    private void processRequest(PlayerReservation poll) {
        log.info("Processing next reservation request {}", poll);
        try {
            transactionTemplate.execute(status -> bookingRegister.register(poll));
        } catch (TransactionException te) {
            log.info("reservation handling trasnaction failed. Need to run recovery for Booking Register", te);
            throw te;
        }
    }
}
