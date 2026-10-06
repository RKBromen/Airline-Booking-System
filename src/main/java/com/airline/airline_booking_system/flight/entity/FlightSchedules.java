package com.airline.airline_booking_system.flight.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.airline.airline_booking_system.aircraft.entity.Aircrafts;
import com.airline.airline_booking_system.booking.entity.Bookings;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "flight_schedules")
public class FlightSchedules {
    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "flight_id", nullable = false)
    private Flights flights;
    @ManyToOne
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircrafts aircrafts;
    @Column(nullable = false)
    private LocalDateTime departure_time;
    @Column(nullable = false)
    private LocalDateTime arrival_time;
    @Column(nullable = false)
    private float base_price;

    @OneToMany(mappedBy = "flight_schedule", cascade = CascadeType.ALL)
    private List<Bookings> bookings;

    @OneToMany(mappedBy = "flight_schedule", cascade = CascadeType.ALL)
    private List<FlightSeats> seats;

    public enum FlightSchedulesStatus {
        SCHEDULED, BOARDING, DEPARTED, COMPLETED, CANCELLED
    }

    @Column(nullable = false)
    private FlightSchedulesStatus status;
    @Column(nullable = false)
    private LocalDateTime created_at;
    @Column(nullable = false)
    private LocalDateTime updated_at;

    public FlightSchedules() {
    }

    public FlightSchedules(String id, Flights flights, Aircrafts aircrafts, LocalDateTime departure_time,
            LocalDateTime arrival_time, float base_price, FlightSchedulesStatus status, LocalDateTime created_at,
            LocalDateTime updated_at) {
        this.id = id;
        this.flights = flights;
        this.aircrafts = aircrafts;
        this.departure_time = departure_time;
        this.arrival_time = arrival_time;
        this.base_price = base_price;
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

    public Flights getFlights() {
        return flights;
    }

    public void setFlights(Flights flights) {
        this.flights = flights;
    }

    public Aircrafts getAircrafts() {
        return aircrafts;
    }

    public void setAircrafts(Aircrafts aircrafts) {
        this.aircrafts = aircrafts;
    }

    public LocalDateTime getDeparture_time() {
        return departure_time;
    }

    public void setDeparture_time(LocalDateTime departure_time) {
        this.departure_time = departure_time;
    }

    public LocalDateTime getArrival_time() {
        return arrival_time;
    }

    public void setArrival_time(LocalDateTime arrival_time) {
        this.arrival_time = arrival_time;
    }

    public float getBase_price() {
        return base_price;
    }

    public void setBase_price(float base_price) {
        this.base_price = base_price;
    }

    public FlightSchedulesStatus getStatus() {
        return status;
    }

    public void setStatus(FlightSchedulesStatus status) {
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
