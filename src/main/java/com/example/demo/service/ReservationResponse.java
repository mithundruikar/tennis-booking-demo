package com.example.demo.service;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@ToString
public class ReservationResponse implements Serializable {
    private String message;
    private boolean success;
}
