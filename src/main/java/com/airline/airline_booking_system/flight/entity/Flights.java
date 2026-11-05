package com.airline.airline_booking_system.flight.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.airline.airline_booking_system.airport.entity.Airports;
import com.airline.airline_booking_system.flight.enums.FlightStatus;
import com.airline.airline_booking_system.flightschedule.entity.FlightSchedules;
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

@Entity
@Table(name = "flights")
@Getter
@Setter
@NoArgsConstructor
public class Flights {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "flight_number", nullable = false, unique = true, length = 20)
    private String flight_number;

    @ManyToOne(optional = false)
    @JoinColumn(name = "departure_airport_id", nullable = false)
    private Airports departure_airport_id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "arrival_airport_id", nullable = false)
    private Airports arrival_airport_id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updated_at;

    @OneToMany(mappedBy = "flights")
    private List<FlightSchedules> schedules;

    public Flights(String id, String flight_number, Airports departure_airport_id, Airports arrival_airport_id,
            FlightStatus status) {
        this.id = id;
        this.flight_number = flight_number;
        this.departure_airport_id = departure_airport_id;
        this.arrival_airport_id = arrival_airport_id;
        this.status = status;
    }
}
