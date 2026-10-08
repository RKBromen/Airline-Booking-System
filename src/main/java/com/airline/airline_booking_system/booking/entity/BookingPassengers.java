package com.airline.airline_booking_system.booking.entity;

import java.time.LocalDateTime;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.airline.airline_booking_system.flight.entity.FlightSeats;

@Entity
@Table(name = "booking_passengers")
public class BookingPassengers {
    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Bookings bookings;
    @ManyToOne
    @JoinColumn(name = "flight_seat_id")
    private FlightSeats flightSeats;
    @Column(nullable = false)
    private String full_name;
    private LocalDate date_of_birth;

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String document_number;
    private LocalDateTime created_at;

    public BookingPassengers() {
    }

    public BookingPassengers(String id, Bookings bookings, FlightSeats flightSeats, String full_name,
            LocalDate date_of_birth, Gender gender, String document_number, LocalDateTime created_at) {
        this.id = id;
        this.bookings = bookings;
        this.flightSeats = flightSeats;
        this.full_name = full_name;
        this.date_of_birth = date_of_birth;
        this.gender = gender;
        this.document_number = document_number;
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Bookings getBookings() {
        return bookings;
    }

    public void setBookings(Bookings bookings) {
        this.bookings = bookings;
    }

    public FlightSeats getFlightSeats() {
        return flightSeats;
    }

    public void setFlightSeats(FlightSeats flightSeats) {
        this.flightSeats = flightSeats;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public LocalDate getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(LocalDate date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getDocument_number() {
        return document_number;
    }

    public void setDocument_number(String document_number) {
        this.document_number = document_number;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}
