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
import com.airline.airline_booking_system.payment.dto.PaymentCreateRequest;
import com.airline.airline_booking_system.payment.dto.PaymentHistoryDTO;
import com.airline.airline_booking_system.payment.dto.PaymentResponse;
import com.airline.airline_booking_system.payment.service.PaymentInitializationService;
import com.airline.airline_booking_system.payment.service.PaymentService;
import com.airline.airline_booking_system.user.service.CurrentUserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("api/v1/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final PaymentInitializationService paymentInitializationService;
    private final CurrentUserService currentUserService;

    public BookingController(BookingService bookingService, PaymentService paymentService,
            PaymentInitializationService paymentInitializationService, CurrentUserService currentUserService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.paymentInitializationService = paymentInitializationService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody BookingCreateRequest request) {

        // Tạm thời dùng userId để kiểm thử.
        // Khi tích hợp Spring Security/JWT, lấy ID từ người dùng đã đăng nhập.
        // String userId = currentUserService.getCurrentUserId();

        BookingResponse response = bookingService.createBooking(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public List<BookingHistory> showHistory() {
        return bookingService.showHistory();
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
            @PathVariable String id) {

        return ResponseEntity.ok(
                bookingService.cancelBooking(id));
    }

    @GetMapping("/{bookingId}/payments")
    public List<PaymentHistoryDTO> getPaymentByBookingId(@PathVariable("bookingId") String bookingId) {
        return paymentService.findPaymentByBookingId(bookingId);
    }

    @PostMapping("/{bookingId}/payments")
    public ResponseEntity<PaymentResponse> createPayment(@PathVariable("bookingId") String bookingId,
            @Valid @RequestBody PaymentCreateRequest request, HttpServletRequest httpRequest) {
        String userId = currentUserService.getCurrentUserId();

        String clientIp = getClientIp(httpRequest);

        PaymentResponse response = paymentInitializationService.initializePayment(
                bookingId,
                userId,
                request,
                clientIp);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
