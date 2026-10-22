package com.airline.airline_booking_system.aircraft.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.airline.airline_booking_system.flightschedule.entity.FlightSchedules;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AircraftsStatus status;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    @OneToMany(mappedBy = "aircrafts", cascade = CascadeType.ALL)
    private List<AircraftSeats> seats;

    @OneToMany(mappedBy = "aircrafts", cascade = CascadeType.ALL)
    private List<FlightSchedules> schedules;

    public Aircrafts(String id, String registration_no, AircraftsStatus status, LocalDateTime created_at,
            LocalDateTime updated_at, List<AircraftSeats> seats) {
        this.id = id;
        this.registration_no = registration_no;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.seats = seats;
    }
}
