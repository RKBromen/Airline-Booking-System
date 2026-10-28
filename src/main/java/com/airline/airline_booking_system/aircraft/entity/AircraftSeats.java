package com.airline.airline_booking_system.aircraft.entity;

import java.util.List;

import com.airline.airline_booking_system.flightschedule.entity.FlightSeats;

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
import jakarta.persistence.UniqueConstraint;

import com.airline.airline_booking_system.aircraft.enums.SeatClass;

@Entity
@Table(name = "aircraft_seats", uniqueConstraints = @UniqueConstraint(
        name = "uk_aircraft_seat", columnNames = {"aircraft_id", "seat_number"}))
public class AircraftSeats {
    @Id
    @Column(length = 36)
    private String id;
    @ManyToOne
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircrafts aircrafts;
    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false)
    private SeatClass seatClass;

    @OneToMany(mappedBy = "seats", cascade = CascadeType.ALL)
    private List<FlightSeats> seats;

    public AircraftSeats() {
    }

    public AircraftSeats(String id, Aircrafts aircrafts, String seat_number, SeatClass seat_class) {
        this.id = id;
        this.aircrafts = aircrafts;
        this.seatNumber = seat_number;
        this.seatClass = seat_class;
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

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(SeatClass seatClass) {
        this.seatClass = seatClass;
    }
}
