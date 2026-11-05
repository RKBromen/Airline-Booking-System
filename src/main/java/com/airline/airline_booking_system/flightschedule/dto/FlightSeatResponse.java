package com.airline.airline_booking_system.flightschedule.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class FlightSeatResponse {
    private String id;
    private String seatNumber;
    private String seatClass;
    private BigDecimal price;
    private String status;

    public FlightSeatResponse(String id, String seatNumber, String seatClass, BigDecimal price, String status) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.seatClass = seatClass;
        this.price = price;
        this.status = status;
    }
}
