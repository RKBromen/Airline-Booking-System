package com.airline.airline_booking_system.flight.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.airline.airline_booking_system.aircraft.entity.AircraftSeats;
import com.airline.airline_booking_system.booking.entity.BookingPassengers;
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
@Table(name = "flight_seats")
public class FlightSeats {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "flight_schedule_id", nullable = false)
    private FlightSchedules schedules;

    @ManyToOne
    @JoinColumn(name = "aircraft_seat_id", nullable = false)
    private AircraftSeats seats;

    public enum FlightSeatsStatus {
        AVAILABLE, HELD, BOOKED
    }

    @Column(nullable = false)
    private FlightSeatsStatus status;
    @Column(nullable = false)
    private float price;

    @ManyToOne
    @JoinColumn(name = "held_by_booking_id", nullable = false)
    private Bookings bookings;
    private LocalDateTime held_until;

    @OneToMany(mappedBy = "flight_seat", cascade = CascadeType.ALL)
    private List<BookingPassengers> passengers;

    public FlightSeats() {
    }

    public FlightSeats(String id, FlightSchedules schedules, AircraftSeats seats, FlightSeatsStatus status, float price,
            Bookings bookings, LocalDateTime held_until) {
        this.id = id;
        this.schedules = schedules;
        this.seats = seats;
        this.status = status;
        this.price = price;
        this.bookings = bookings;
        this.held_until = held_until;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FlightSchedules getSchedules() {
        return schedules;
    }

    public void setSchedules(FlightSchedules schedules) {
        this.schedules = schedules;
    }

    public AircraftSeats getSeats() {
        return seats;
    }

    public void setSeats(AircraftSeats seats) {
        this.seats = seats;
    }

    public FlightSeatsStatus getStatus() {
        return status;
    }

    public void setStatus(FlightSeatsStatus status) {
        this.status = status;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public Bookings getBookings() {
        return bookings;
    }

    public void setBookings(Bookings bookings) {
        this.bookings = bookings;
    }

    public LocalDateTime getHeld_until() {
        return held_until;
    }

    public void setHeld_until(LocalDateTime held_until) {
        this.held_until = held_until;
    }
}
