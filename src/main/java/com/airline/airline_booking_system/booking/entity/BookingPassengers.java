package com.airline.airline_booking_system.booking.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.airline.airline_booking_system.flightschedule.entity.FlightSeats;

@Data
@NoArgsConstructor
@Entity
@Table(name = "booking_passengers")
public class BookingPassengers {
    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Bookings bookings;
    @ManyToOne
    @JoinColumn(name = "flight_seat_id", nullable = false)
    private FlightSeats flight_seats;
    @Column(nullable = false)
    private String full_name;
    private LocalDateTime date_of_birth;

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String document_number;
    private LocalDateTime created_at;

    public BookingPassengers(String id, Bookings bookings, FlightSeats flightSeats, String full_name,
            LocalDateTime date_of_birth, Gender gender, String document_number, LocalDateTime created_at) {
        this.id = id;
        this.bookings = bookings;
        this.flight_seats = flightSeats;
        this.full_name = full_name;
        this.date_of_birth = date_of_birth;
        this.gender = gender;
        this.document_number = document_number;
        this.created_at = created_at;
    }
}
