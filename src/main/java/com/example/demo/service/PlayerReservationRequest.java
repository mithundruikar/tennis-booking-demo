package com.example.demo.service;

import com.example.demo.entities.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class PlayerReservationRequest {
    private String name;
    private String contact;
    //private Long courtId;
    private LocalDate reservationDate;
    private RequestType requestType;

}
