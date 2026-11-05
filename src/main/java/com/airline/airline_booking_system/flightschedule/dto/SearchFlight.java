package com.airline.airline_booking_system.flightschedule.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SearchFlight {
    private String id;
    private String flight_id;
    private String departure_id;
    private String arrival_id;
    private LocalDateTime departure_time;
    private LocalDateTime arrival_time;
    private BigDecimal base_price;
    private Long seatAvailable;

    public SearchFlight(String id, String flight_id, String departure_id, String arrival_id,
            LocalDateTime departure_time, LocalDateTime arrival_time, BigDecimal base_price, Long seatAvailable) {
        this.id = id;
        this.flight_id = flight_id;
        this.departure_id = departure_id;
        this.arrival_id = arrival_id;
        this.departure_time = departure_time;
        this.arrival_time = arrival_time;
        this.base_price = base_price;
        this.seatAvailable = seatAvailable;
    }
}
