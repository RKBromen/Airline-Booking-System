package com.airline.airline_booking_system.booking.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.catalina.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.airline.airline_booking_system.flight.entity.*;
import com.airline.airline_booking_system.payment.entity.*;

@Entity
@Table(name = "bookings")
public class Bookings {
    @Id
    private String id;
    @Column(nullable = false)
    private String booking_code;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "flight_schedule_id", nullable = false)
    private FlightSchedules flightSchedules;

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, EXPIRED
    }

    @Column(nullable = false)
    private BookingStatus status;
    @Column(nullable = false)
    private float total_amount;
    private LocalDateTime expires_at;
    @Column(nullable = false)
    private LocalDateTime created_at;
    @Column(nullable = false)
    private LocalDateTime updated_at;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<FlightSeats> seats;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingPassengers> passengers;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<Payments> payments;

    public Bookings() {
    }

    public Bookings(String id, String booking_code, User user, FlightSchedules flightSchedules, BookingStatus status,
            float total_amount, LocalDateTime expires_at, LocalDateTime created_at, LocalDateTime updated_at) {
        this.id = id;
        this.booking_code = booking_code;
        this.user = user;
        this.flightSchedules = flightSchedules;
        this.status = status;
        this.total_amount = total_amount;
        this.expires_at = expires_at;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBooking_code() {
        return booking_code;
    }

    public void setBooking_code(String booking_code) {
        this.booking_code = booking_code;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public FlightSchedules getFlightSchedules() {
        return flightSchedules;
    }

    public void setFlightSchedules(FlightSchedules flightSchedules) {
        this.flightSchedules = flightSchedules;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public float getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(float total_amount) {
        this.total_amount = total_amount;
    }

    public LocalDateTime getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(LocalDateTime expires_at) {
        this.expires_at = expires_at;
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
