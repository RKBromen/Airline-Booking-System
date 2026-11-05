package com.airline.airline_booking_system.booking.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.airline.airline_booking_system.booking.dto.BookingDTO;
import com.airline.airline_booking_system.booking.entity.Bookings;
import com.airline.airline_booking_system.booking.service.BookingService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("api/v1/admin/bookings")
public class AdminBookingController {
    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<BookingDTO> getAll() {
        return bookingService.getAll();
    }

}
