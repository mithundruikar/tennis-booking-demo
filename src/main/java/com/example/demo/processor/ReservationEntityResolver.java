package com.example.demo.processor;

import com.example.demo.entities.Player;
import com.example.demo.entities.PlayerReservation;
import com.example.demo.entities.repository.PlayerRepository;
import com.example.demo.service.PlayerReservationRequest;


public class ReservationEntityResolver {
    private PlayerRepository playerRepository;

    public ReservationEntityResolver(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public PlayerReservation resolve(PlayerReservationRequest playerReservationRequest) {
        Player byNameAndContact = this.playerRepository.findByNameAndContact(playerReservationRequest.getName(), playerReservationRequest.getContact());

        return new PlayerReservation((byNameAndContact == null ? new Player(playerReservationRequest.getName(), playerReservationRequest.getContact()) : byNameAndContact),
                playerReservationRequest.getReservationDate(),
                playerReservationRequest.getRequestType());
    }
}

