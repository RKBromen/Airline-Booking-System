package com.airline.airline_booking_system.aircraft.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.airline.airline_booking_system.flight.entity.*;

@Entity
@Table(name = "aircraft_seats")
public class AircraftSeats {
    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircrafts aircrafts;
    @Column(nullable = false)
    private String seat_number;

    public enum SeatClass {
        ECONOMY, PREMIUM_ECONOMY, BUSINESS, FIRST
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false)
    private SeatClass seat_class;

    @OneToMany(mappedBy = "seats", cascade = CascadeType.ALL)
    private List<FlightSeats> seats;

    public AircraftSeats() {
    }

    public AircraftSeats(String id, Aircrafts aircrafts, String seat_number, SeatClass seat_class) {
        this.id = id;
        this.aircrafts = aircrafts;
        this.seat_number = seat_number;
        this.seat_class = seat_class;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Aircrafts getAircrafts() {
        return aircrafts;
    }

    public void setAircrafts(Aircrafts aircrafts) {
        this.aircrafts = aircrafts;
    }

    public String getSeat_number() {
        return seat_number;
    }

    public void setSeat_number(String seat_number) {
        this.seat_number = seat_number;
    }

    public SeatClass getSeat_class() {
        return seat_class;
    }

    public void setSeat_class(SeatClass seat_class) {
        this.seat_class = seat_class;
    }
}
