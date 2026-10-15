package com.airline.airline_booking_system.airport.entity;

import java.time.LocalDateTime;
import java.util.List;

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

import com.airline.airline_booking_system.flight.entity.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "airports")
public class Airports {
    @Id
    private String id;
    @Column(nullable = false)
    private String code;
    @Column(nullable = false)
    private String name;
    private String city;
    private String country;

    @OneToMany(mappedBy = "departure_airport", cascade = CascadeType.ALL)
    private List<Flights> departure_airport;
    @OneToMany(mappedBy = "arrival_airport", cascade = CascadeType.ALL)
    private List<Flights> arrival_airport;

    public enum AirportsStatus {
        ACTIVE, BLOCKED, DELETED
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AirportsStatus status;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public Airports(String id, String code, String name, String city, String country, AirportsStatus status,
            LocalDateTime created_at, LocalDateTime updated_at) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.city = city;
        this.country = country;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }
}
