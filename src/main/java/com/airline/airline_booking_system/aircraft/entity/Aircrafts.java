package com.airline.airline_booking_system.aircraft.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.airline.airline_booking_system.flight.entity.*;

@Entity
@Table(name = "aircrafts")
public class Aircrafts {
    @Id
    private String id;
    @Column(nullable = false, unique = true)
    private String registration_no;

    public enum AircraftsStatus {
        ACTIVE, BLOCKED, DELETED
    }

    @Column(nullable = false)
    private AircraftsStatus status;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    @OneToMany(mappedBy = "aircraft_seats", cascade = CascadeType.ALL)
    private List<AircraftSeats> seats;

    @OneToMany(mappedBy = "flight_schedules", cascade = CascadeType.ALL)
    private List<FlightSchedules> schedules;

    public Aircrafts() {
    }

    public Aircrafts(String id, String registration_no, AircraftsStatus status, LocalDateTime created_at,
            LocalDateTime updated_at, List<AircraftSeats> seats) {
        this.id = id;
        this.registration_no = registration_no;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.seats = seats;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRegistration_no() {
        return registration_no;
    }

    public void setRegistration_no(String registration_no) {
        this.registration_no = registration_no;
    }

    public AircraftsStatus getStatus() {
        return status;
    }

    public void setStatus(AircraftsStatus status) {
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

    public List<AircraftSeats> getSeats() {
        return seats;
    }

    public void setSeats(List<AircraftSeats> seats) {
        this.seats = seats;
    }
}
