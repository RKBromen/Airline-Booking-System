package com.airline.airline_booking_system.aircraft.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.airline.airline_booking_system.flightschedule.entity.FlightSchedules;
import com.airline.airline_booking_system.aircraft.enums.AircraftStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "aircrafts")
public class Aircrafts {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "registration_no", nullable = false, unique = true, length = 50)
    private String registrationNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AircraftStatus status;
    @Column(insertable = false, updatable = false)
    private LocalDateTime created_at;
    @Column(insertable = false, updatable = false)
    private LocalDateTime updated_at;

    @OneToMany(mappedBy = "aircrafts", cascade = CascadeType.ALL)
    private List<AircraftSeats> seats;

    @OneToMany(mappedBy = "aircrafts", cascade = CascadeType.ALL)
    private List<FlightSchedules> schedules;

    public Aircrafts(String id, String registration_no, AircraftStatus status, LocalDateTime created_at,
            LocalDateTime updated_at, List<AircraftSeats> seats) {
        this.id = id;
        this.registrationNo = registration_no;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.seats = seats;
    }
}
