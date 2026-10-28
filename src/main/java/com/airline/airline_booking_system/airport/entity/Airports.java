package com.airline.airline_booking_system.airport.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.airline.airline_booking_system.airport.enums.AirportStatus;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@NoArgsConstructor
@Entity
@Table(name = "airports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Airports {
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private UUID id;

    @Column(nullable = false, length = 3, unique = true, updatable = false)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @OneToMany(mappedBy = "departure_airport_id", cascade = CascadeType.ALL)
    private List<Flights> departureFlights;

    @OneToMany(mappedBy = "arrival_airport_id", cascade = CascadeType.ALL)
    private List<Flights> arrivalFlights;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AirportStatus status;

    @Column(name = "created_at",insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at",insertable = false, updatable = false)
    private LocalDateTime updatedAt;

}
