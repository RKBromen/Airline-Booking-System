package com.airline.airline_booking_system.flight.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.airline.airline_booking_system.airport.entity.Airports;

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

@Entity
@Table(name = "flights")
public class Flights {
    @Id
    private String id;
    @Column(nullable = false, unique = true)
    private String flight_number;
    @ManyToOne
    @JoinColumn(name = "departure_airport_id", nullable = false)
    private Airports departure_airport_id;
    @ManyToOne
    @JoinColumn(name = "arrival_airport_id", nullable = false)
    private Airports arrival_airport_id;

    public enum FlightStatus {
        ACTIVE, INACTIVE
    }

    @Enumerated(EnumType.STRING)
    private FlightStatus status;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    @OneToMany(mappedBy = "flights", cascade = CascadeType.ALL)
    private List<FlightSchedules> schedules;

    public Flights() {
    }

    public Flights(String id, String flight_number, Airports departure_airport_id, Airports arrival_airport_id,
            FlightStatus status, LocalDateTime created_at, LocalDateTime updated_at) {
        this.id = id;
        this.flight_number = flight_number;
        this.departure_airport_id = departure_airport_id;
        this.arrival_airport_id = arrival_airport_id;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlight_number() {
        return flight_number;
    }

    public void setFlight_number(String flight_number) {
        this.flight_number = flight_number;
    }

    public Airports getDeparture_airport_id() {
        return departure_airport_id;
    }

    public void setDeparture_airport_id(Airports departure_airport_id) {
        this.departure_airport_id = departure_airport_id;
    }

    public Airports getArrival_airport_id() {
        return arrival_airport_id;
    }

    public void setArrival_airport_id(Airports arrival_airport_id) {
        this.arrival_airport_id = arrival_airport_id;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }
}
