package com.airline.airline_booking_system.booking.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.airline.airline_booking_system.flightschedule.entity.FlightSchedules;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.airline.airline_booking_system.payment.entity.*;
import com.airline.airline_booking_system.user.entity.Users;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bookings")
public class Bookings {
    @Id
    private String id;
    @Column(nullable = false)
    private String booking_code;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

    @ManyToOne
    @JoinColumn(name = "flight_schedules_id", nullable = false)
    private FlightSchedules flight_schedules;

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, EXPIRED
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;
    @Column(nullable = false)
    private float total_amount;
    private LocalDateTime expires_at;
    @Column(nullable = false)
    private LocalDateTime created_at;
    @Column(nullable = false)
    private LocalDateTime updated_at;

    @OneToMany(mappedBy = "bookings", cascade = CascadeType.ALL)
    private List<FlightSeats> seats;

    @OneToMany(mappedBy = "bookings", cascade = CascadeType.ALL)
    private List<BookingPassengers> passengers;

    @OneToMany(mappedBy = "bookings", cascade = CascadeType.ALL)
    private List<Payments> payments;

    public Bookings(String id, String booking_code, Users user, FlightSchedules flightSchedules, BookingStatus status,
            float total_amount, LocalDateTime expires_at, LocalDateTime created_at, LocalDateTime updated_at,
            List<FlightSeats> seats, List<BookingPassengers> passengers, List<Payments> payments) {
        this.id = id;
        this.booking_code = booking_code;
        this.users = user;
        this.flight_schedules = flightSchedules;
        this.status = status;
        this.total_amount = total_amount;
        this.expires_at = expires_at;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.seats = seats;
        this.passengers = passengers;
        this.payments = payments;
    }
}
