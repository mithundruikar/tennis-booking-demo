
#Running locally
Main class: `com.example.demo.DemoApplication` in application module.<br>

Includes below:
- `com.example.demo.service.CourtBookingService` Rest controller. 
    It exposes below endpoints:
    - /api/tennis/reservations - Post end point reservation creation
    - /api/tennis/bookings - Getter
    - /api/tennis/bookings/{id} - Getter
    - /api/tennis/bookings/byReservation/{reservationId} - Getter
    - /api/tennis/reservations/{id} - Getter
    - /api/tennis/reservations/dates/{reservationDate}
    
- Swagger support. Once you run the app you can launch http://localhost:8080/swagger-ui.html
- IT Test case `com.example.demo.service.CourtBookingServiceIT` which tests Rest end point end to end. 

    It covers below scenarios:
    - Should confirm the booking once 4 (configurable) requests are received
    - Should not allow any further requests once all courts are booked for the day
    - Should not allow more than one reservation by same player for same day
 

#Design - <br>
System is divided into 2 parts by a queue `com.example.demo.processor.BookingRequestQueue`. Rest service hands the reservation request to queue which after initial checks offers to the queue. <br>
Single threaded task `com.example.demo.processor.task.BookingProcessorTask` picks up the tasks from the queue and uses `com.example.demo.processor.booking.BookingRegister` to register the request. <br>

`com.example.demo.entities.PlayerReservation` stands for user request, is persisted before any booking logic triggers. This will open up opportunities of recovering system state in case of fail-overs.


#Pending work:
- Database and in-memory booking map can go out of sync if there are some db failures. Transaction boundaries need to be adjusted to make it more robust.
- Current approach is simple where by if db fails, we can use persisted logs of reservation requests to re-build in-memory booking model which will be in sync with DB. <br>
But these recovery mechanics are pending to be implemented.