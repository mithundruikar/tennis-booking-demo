package com.example.demo.processor.task;

import com.example.demo.entities.Player;
import com.example.demo.entities.PlayerReservation;
import com.example.demo.entities.RequestType;
import com.example.demo.processor.BookingRequestQueue;
import com.example.demo.processor.booking.BookingRegister;
import com.example.demo.service.PlayerReservationRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BookingProcessorTaskTest {

    @Mock
    private BookingRequestQueue bookingRequestQueue;
    private LinkedBlockingQueue<PlayerReservation> underlyingQueue;
    @Mock
    private BookingRegister bookingRegister;
    @Mock
    private TransactionTemplate transactionTemplate;

    private BookingProcessorTask bookingProcessorTask;


    @Before
    public void setup() {
        this.underlyingQueue = new LinkedBlockingQueue<>(100);
        when(bookingRequestQueue.getQueue()).thenReturn(this.underlyingQueue);
        bookingProcessorTask = new BookingProcessorTask(bookingRequestQueue, bookingRegister, transactionTemplate);
        when(transactionTemplate.execute(any(TransactionCallback.class))).thenAnswer(ans -> ans.getArgument(0, TransactionCallback.class).doInTransaction(null));
    }


    @Test
    public void testQueuePollAndForward() {
        LocalDate now = LocalDate.now();
        PlayerReservation request = new PlayerReservation(new Player("p1", "c1"), now, RequestType.ADD);
        this.underlyingQueue.offer(request);

        this.bookingProcessorTask.forceRun();

        verify(transactionTemplate, times(1)).execute(any());
        verify(this.bookingRegister, times(1)).register(eq(request));
    }
}