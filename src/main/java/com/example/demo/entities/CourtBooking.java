package com.example.demo.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "court_id", "booking_date" }) })
@Getter
@ToString
public class CourtBooking implements Serializable {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Exclude
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;
    @Column(name = "booking_date")
    private LocalDate bookingDate;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name="booking_reservation_mapping"
            , joinColumns={ @JoinColumn(name="booking_id") }
            , inverseJoinColumns={ @JoinColumn(name="reservation_id") }
    )
    private Set<PlayerReservation> playerReservations = Collections.emptySet();

    @Enumerated(EnumType.STRING)
    private Status status;

    @Transient
    private int requiredPlayers;

    @EqualsAndHashCode.Exclude
    private LocalDateTime createdTimestamp;

    public CourtBooking(Court court, LocalDate bookingDate, int requiredPlayers) {
        this.court = court;
        this.bookingDate = bookingDate;
        this.playerReservations = new HashSet<>();
        this.status = Status.PENDING;
        this.requiredPlayers = requiredPlayers;
        this.createdTimestamp = LocalDateTime.now(ZoneId.of("UTC"));
    }

    public CourtBooking() {

    }

    public synchronized boolean addPlayerReservation(PlayerReservation playerReservation) {
        if (Status.CONFIRMED.equals(this.status)) {
            return false;
        }
        boolean added = this.playerReservations.add(playerReservation);
        if(!added) {
            return false;
        }

        if (this.playerReservations.size() == requiredPlayers) {
            this.status = Status.CONFIRMED;
        }
        return true;
    }

    public boolean contains(Player player, LocalDate reservationDate) {
        return this.playerReservations.stream().anyMatch(pr -> getBookingDate().equals(reservationDate) && pr.getPlayer().equals(player));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourtBooking that = (CourtBooking) o;
        boolean match = Objects.equals(id, that.id) &&
                Objects.equals(court, that.court) &&
                Objects.equals(bookingDate, that.bookingDate) &&
                status == that.status;
        if(match) {
            List<PlayerReservation> playersCopy = this.playerReservations == null ? Collections.emptyList(): new ArrayList<>(playerReservations);
            List<PlayerReservation> thatPlayersCopy = that.playerReservations == null ? Collections.emptyList():  new ArrayList<>(that.playerReservations);
            Collections.sort(playersCopy, Comparator.comparing(PlayerReservation::getId));
            Collections.sort(thatPlayersCopy, Comparator.comparing(PlayerReservation::getId));
            return Objects.equals(playersCopy, thatPlayersCopy);
        }
        return match;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, court, bookingDate, playerReservations, status);
    }
}
