package com.airline.airline_booking_system.flightschedule.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.math.*;

import com.airline.airline_booking_system.aircraft.entity.Aircrafts;
import com.airline.airline_booking_system.booking.entity.Bookings;

import com.airline.airline_booking_system.flight.entity.Flights;
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
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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
    private BigDecimal base_price;

    @OneToMany(mappedBy = "flight_schedules", cascade = CascadeType.ALL)
    private List<Bookings> bookings;

    @OneToMany(mappedBy = "flight_schedules", cascade = CascadeType.ALL)
    private List<FlightSeats> seats;

    public enum FlightSchedulesStatus {
        SCHEDULED, BOARDING, DEPARTED, COMPLETED, CANCELLED
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightSchedulesStatus status;
    @Column(nullable = false)
    private LocalDateTime created_at;
    @Column(nullable = false)
    private LocalDateTime updated_at;

    public FlightSchedules(String id, Flights flights, Aircrafts aircrafts, LocalDateTime departure_time,
            LocalDateTime arrival_time, BigDecimal base_price, FlightSchedulesStatus status, LocalDateTime created_at,
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
}
