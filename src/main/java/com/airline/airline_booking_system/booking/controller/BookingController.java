package com.airline.airline_booking_system.booking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.airline.airline_booking_system.booking.dto.BookingCancelResponse;
import com.airline.airline_booking_system.booking.dto.BookingCreateRequest;
import com.airline.airline_booking_system.booking.dto.BookingDetail;
import com.airline.airline_booking_system.booking.dto.BookingDetailByCode;
import com.airline.airline_booking_system.booking.dto.BookingHistory;
import com.airline.airline_booking_system.booking.dto.BookingResponse;
import com.airline.airline_booking_system.booking.service.BookingService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("api/v1/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody BookingCreateRequest request) {

        // Tạm thời dùng userId để kiểm thử.
        // Khi tích hợp Spring Security/JWT, lấy ID từ người dùng đã đăng nhập.
        String userId = requestUserIdForTesting();

        BookingResponse response = bookingService.createBooking(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String requestUserIdForTesting() {
        // Thay giá trị này bằng ID của một user ACTIVE có thật trong database.
        return "00000002-0000-4000-8000-000000000002";
    }

    @GetMapping("/me")
    public List<BookingHistory> showHistory() {
        String userId = requestUserIdForTesting();
        return bookingService.showHistory(userId);
    }

    @GetMapping("/{id}")
    public BookingDetail getDetail(@PathVariable("id") String bookingId) {
        return bookingService.getDetail(bookingId);
    }

    @GetMapping("/code/{bookingCode}")
    public BookingDetailByCode getDetailByCode(@PathVariable("bookingCode") String code) {
        return bookingService.getDetailByCode(code);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingCancelResponse> cancelBooking(
            @PathVariable String id,
            @RequestParam String userId) {

        return ResponseEntity.ok(
                bookingService.cancelBooking(id, userId));
    }
}
