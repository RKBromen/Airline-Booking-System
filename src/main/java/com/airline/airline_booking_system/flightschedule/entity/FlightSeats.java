package com.airline.airline_booking_system.flightschedule.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.airline.airline_booking_system.aircraft.entity.AircraftSeats;
import com.airline.airline_booking_system.booking.entity.BookingPassengers;
import com.airline.airline_booking_system.booking.entity.Bookings;

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
@Table(name = "flight_seats")
public class FlightSeats {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "flight_schedule_id", nullable = false)
    private FlightSchedules flight_schedules;

    @ManyToOne
    @JoinColumn(name = "aircraft_seat_id", nullable = false)
    private AircraftSeats seats;

    public enum FlightSeatsStatus {
        AVAILABLE, HELD, BOOKED
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightSeatsStatus status;
    @Column(nullable = false)
    private float price;

    @ManyToOne
    @JoinColumn(name = "held_by_booking_id")
    private Bookings bookings;
    private LocalDateTime held_until;

    @OneToMany(mappedBy = "flightSeats", cascade = CascadeType.ALL)
    private List<BookingPassengers> passengers;

    public FlightSeats(String id, FlightSchedules schedules, AircraftSeats seats, FlightSeatsStatus status, float price,
            Bookings bookings, LocalDateTime held_until) {
        this.id = id;
        this.flight_schedules = schedules;
        this.seats = seats;
        this.status = status;
        this.price = price;
        this.bookings = bookings;
        this.held_until = held_until;
    }
}
