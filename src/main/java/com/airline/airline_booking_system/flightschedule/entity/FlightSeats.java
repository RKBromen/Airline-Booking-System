package com.airline.airline_booking_system.flightschedule.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.airline.airline_booking_system.aircraft.entity.AircraftSeats;
import com.airline.airline_booking_system.booking.entity.BookingPassengers;
import com.airline.airline_booking_system.booking.entity.Bookings;
import com.airline.airline_booking_system.flightschedule.enums.FlightSeatStatus;
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
@Table(name = "flight_seats")
@Getter
@Setter
@NoArgsConstructor
public class FlightSeats {
    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "flight_schedule_id", nullable = false)
    private FlightSchedules flight_schedules;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aircraft_seat_id", nullable = false)
    private AircraftSeats seats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightSeatStatus status;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "held_by_booking_id")
    private Bookings bookings;

    @Column(name = "held_until")
    private LocalDateTime held_until;

    @OneToMany(mappedBy = "flightSeats")
    private List<BookingPassengers> passengers;

    public FlightSeats(String id, FlightSchedules flight_schedules, AircraftSeats seats, FlightSeatStatus status,
            BigDecimal price) {
        this.id = id;
        this.flight_schedules = flight_schedules;
        this.seats = seats;
        this.status = status;
        this.price = price;
    }
}
