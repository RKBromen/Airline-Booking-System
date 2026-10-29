package com.airline.airline_booking_system.flightschedule.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.airline.airline_booking_system.aircraft.entity.Aircrafts;
import com.airline.airline_booking_system.booking.entity.Bookings;
import com.airline.airline_booking_system.flight.entity.Flights;
import com.airline.airline_booking_system.flightschedule.enums.FlightScheduleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "flight_schedules")
@Getter
@Setter
@NoArgsConstructor
public class FlightSchedules {
    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flights flights;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircrafts aircrafts;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departure_time;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrival_time;

    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal base_price;

    @OneToMany(mappedBy = "flight_schedules")
    private List<Bookings> bookings;

    @OneToMany(mappedBy = "flight_schedules")
    private List<FlightSeats> seats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightScheduleStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updated_at;

    public FlightSchedules(String id, Flights flights, Aircrafts aircrafts, LocalDateTime departure_time,
            LocalDateTime arrival_time, BigDecimal base_price, FlightScheduleStatus status) {
        this.id = id;
        this.flights = flights;
        this.aircrafts = aircrafts;
        this.departure_time = departure_time;
        this.arrival_time = arrival_time;
        this.base_price = base_price;
        this.status = status;
    }
}
