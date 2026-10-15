package com.airline.airline_booking_system.flight.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.airline.airline_booking_system.airport.entity.Airports;

import com.airline.airline_booking_system.flightschedule.entity.FlightSchedules;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "flights")
public class Flights {
    @Id
    private String id;
    @Column(nullable = false, unique = true)
    private String flight_number;
    @ManyToOne
    @JoinColumn(name = "departure_airport_id", nullable = false, unique = true)
    private Airports departure_airport;
    @ManyToOne
    @JoinColumn(name = "arrival_airport_id", nullable = false, unique = true)
    private Airports arrival_airport;

    public enum FlightStatus {
        ACTIVE, INACTIVE
    }

    private FlightStatus status;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    @OneToMany(mappedBy = "flights", cascade = CascadeType.ALL)
    private List<FlightSchedules> schedules;

    public Flights(String id, String flight_number, Airports departure_airport, Airports arrival_airport,
            FlightStatus status, LocalDateTime created_at, LocalDateTime updated_at, List<FlightSchedules> schedules) {
        this.id = id;
        this.flight_number = flight_number;
        this.departure_airport = departure_airport;
        this.arrival_airport = arrival_airport;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.schedules = schedules;
    }
}
