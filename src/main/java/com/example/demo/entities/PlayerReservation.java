package com.example.demo.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@EqualsAndHashCode
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "player_id", "reservation_date" }) })
@ToString
public class PlayerReservation  implements Serializable {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Exclude
    private long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "player_id")
    private Player player;
    @Column(name = "reservation_date")
    private LocalDate reservationDate;

    @EqualsAndHashCode.Exclude
    private LocalDateTime createdTimestamp;

    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    public PlayerReservation(Player player, LocalDate reservationDate, RequestType requestType) {
        this.player = player;
        this.reservationDate = reservationDate;
        this.requestType = requestType;
        this.createdTimestamp = LocalDateTime.now(ZoneId.of("UTC"));
    }

    public PlayerReservation() {

    }

    public Player getPlayer() {
        return player;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public long getId() {
        return id;
    }
}
