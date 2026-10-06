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

import com.airline.airline_booking_system.flight.entity.*;

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

    @OneToMany(mappedBy = "airport", cascade = CascadeType.ALL)
    private List<Flights> flights;

    public enum AirportsStatus {
        ACTIVE, BLOCKED, DELETED
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AirportsStatus status;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public Airports() {
    }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public AirportsStatus getStatus() {
        return status;
    }

    public void setStatus(AirportsStatus status) {
        this.status = status;
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
