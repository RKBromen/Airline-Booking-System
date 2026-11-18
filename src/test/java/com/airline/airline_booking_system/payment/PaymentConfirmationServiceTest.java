package com.airline.airline_booking_system.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.airline.airline_booking_system.booking.entity.Bookings;
import com.airline.airline_booking_system.booking.entity.Bookings.BookingStatus;
import com.airline.airline_booking_system.booking.repository.BookingRepository;
import com.airline.airline_booking_system.payment.dto.PaymentCreateRequest;
import com.airline.airline_booking_system.payment.dto.PendingPaymentData;
import com.airline.airline_booking_system.payment.entity.Payments;
import com.airline.airline_booking_system.payment.entity.Payments.PaymentStatus;
import com.airline.airline_booking_system.payment.repository.PaymentRepository;
import com.airline.airline_booking_system.payment.service.PaymentConfirmationService;
import com.airline.airline_booking_system.user.entity.Users;

@ExtendWith(MockitoExtension.class)
public class PaymentConfirmationServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentConfirmationService paymentConfirmationService;

    private Bookings booking;
    private Users user;

    private final String bookingId = "booking-001";
    private final String userId = "user-001";

    @BeforeEach
    void setUp() {
        user = new Users();
        user.setId(userId);

        booking = new Bookings();
        booking.setId(bookingId);
        booking.setUsers(user);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotal_amount(new BigDecimal("1500000"));
        booking.setExpires_at(LocalDateTime.now().plusMinutes(15));
    }

    @Test
    void preparePayment_shouldCreatePayment_whenBookingIsValid() {
        PaymentCreateRequest request = new PaymentCreateRequest("VNPAY");

        when(bookingRepository.findByIdForUpdate(bookingId)).thenReturn(Optional.of(booking));

        when(paymentRepository.findByBookings_IdAndStatus(bookingId, PaymentStatus.PENDING))
                .thenReturn(Optional.empty());

        when(paymentRepository.existsByBookings_IdAndStatus(bookingId, PaymentStatus.SUCCESS)).thenReturn(false);

        when(paymentRepository.save(any(Payments.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PendingPaymentData result = paymentConfirmationService.createPendingPayments(bookingId, userId, request);

        assertNotNull(result);
        assertEquals(bookingId, result.bookingId());
        assertEquals("1500000", result.amount().toPlainString());
        assertEquals("VNPAY", result.paymentMethod());
        assertEquals("PENDING", result.status());
        assertNotNull(result.paymentId());
        assertNotNull(result.transactionId());

        verify(paymentRepository, times(1)).save(any(Payments.class));
    }

    @Test
    void preparePayment_shouldReuseExistingPayment_whenPendingPaymentExists() {
        PaymentCreateRequest request = new PaymentCreateRequest("VNPAY");

        Payments existingPayment = new Payments("payment-001", booking, Payments.PaymentMethod.VNPAY,
                new BigDecimal("1500000"), Payments.PaymentStatus.PENDING, "transaction-001", LocalDateTime.now(), null,
                "https://example.com/checkout");

        when(bookingRepository.findByIdForUpdate(bookingId))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findByBookings_IdAndStatus(
                bookingId, Payments.PaymentStatus.PENDING))
                .thenReturn(Optional.of(existingPayment));

        // Act
        PendingPaymentData result = paymentConfirmationService.createPendingPayments(
                bookingId, userId, request);

        // Assert
        assertEquals("payment-001", result.paymentId());
        assertEquals("transaction-001", result.transactionId());
        assertEquals("https://example.com/checkout", result.paymentUrl());

        verify(paymentRepository, never())
                .save(any(Payments.class));
    }

    @Test
    void preparePayment_shouldThrowException_whenBookingNotFound() {
        when(bookingRepository.findByIdForUpdate(bookingId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentConfirmationService.createPendingPayments(bookingId, userId,
                new PaymentCreateRequest("VNPAY")));

        verify(paymentRepository, never()).save(any(Payments.class));
    }

    @Test
    void preparePayment_shouldThrowAccessDeniedException_whenUserIsNotOwner() {
        when(bookingRepository.findByIdForUpdate(bookingId))
                .thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class, () -> paymentConfirmationService.createPendingPayments(
                bookingId,
                "another-user-id",
                new PaymentCreateRequest("VNPAY")));

        verify(paymentRepository, never()).save(any(Payments.class));
    }

    @Test
    void preparePayment_shouldThrowException_whenBookingIsNotPending() {
        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findByIdForUpdate(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class, () -> paymentConfirmationService.createPendingPayments(bookingId,
                userId, new PaymentCreateRequest("VNPAY")));

        verify(paymentRepository, never()).save(any(Payments.class));
    }

    @Test
    void preparePayment_shouldCreatePayment_whenBookingHasExpired() {
        booking.setExpires_at(LocalDateTime.now().minusMinutes(1));

        when(bookingRepository.findByIdForUpdate(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class, () -> paymentConfirmationService.createPendingPayments(bookingId,
                userId, new PaymentCreateRequest("VNPAY")));

        verify(paymentRepository, never()).save(any(Payments.class));
    }

    @Test
    void preparePayment_shouldThrowException_whenPaymentAlreadySucceeded() {
        when(bookingRepository.findByIdForUpdate(bookingId))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findByBookings_IdAndStatus(
                bookingId, Payments.PaymentStatus.PENDING))
                .thenReturn(Optional.empty());

        when(paymentRepository.existsByBookings_IdAndStatus(
                bookingId, Payments.PaymentStatus.SUCCESS))
                .thenReturn(true);

        assertThrows(IllegalStateException.class, () -> paymentConfirmationService.createPendingPayments(
                bookingId,
                userId,
                new PaymentCreateRequest("VNPAY")));

        verify(paymentRepository, never()).save(any(Payments.class));
    }

    @Test
    void preparePayment_shouldThrowException_whenPendingPaymentHasDifferentMethod() {
        Payments existingPayment = new Payments(
                "payment-001",
                booking,
                Payments.PaymentMethod.VNPAY,
                new BigDecimal("1500000"),
                Payments.PaymentStatus.PENDING,
                "transaction-001",
                LocalDateTime.now(),
                null,
                null);

        when(bookingRepository.findByIdForUpdate(bookingId))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findByBookings_IdAndStatus(
                bookingId, Payments.PaymentStatus.PENDING))
                .thenReturn(Optional.of(existingPayment));

        assertThrows(IllegalStateException.class, () -> paymentConfirmationService.createPendingPayments(
                bookingId,
                userId,
                new PaymentCreateRequest("MOMO")));

        verify(paymentRepository, never()).save(any(Payments.class));
    }

    @Test
    void savePaymentUrl_shouldSaveUrl_whenPaymentIsPending() {
        Payments payment = new Payments(
                "payment-001",
                booking,
                Payments.PaymentMethod.VNPAY,
                new BigDecimal("1500000"),
                Payments.PaymentStatus.PENDING,
                "transaction-001",
                LocalDateTime.now(),
                null,
                null);

        String paymentUrl = "https://example.com/checkout";

        when(paymentRepository.findByIdForUpdate("payment-001"))
                .thenReturn(Optional.of(payment));

        paymentConfirmationService.savePaymentUrl("payment-001", paymentUrl);

        assertEquals(paymentUrl, payment.getPayment_url());

        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void savePaymentUrl_shouldThrowException_whenPaymentIsNotPending() {
        Payments payment = new Payments(
                "payment-001",
                booking,
                Payments.PaymentMethod.VNPAY,
                new BigDecimal("1500000"),
                Payments.PaymentStatus.SUCCESS,
                "transaction-001",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null);

        when(paymentRepository.findByIdForUpdate("payment-001"))
                .thenReturn(Optional.of(payment));

        assertThrows(IllegalStateException.class, () -> paymentConfirmationService.savePaymentUrl(
                "payment-001",
                "https://example.com/checkout"));

        verify(paymentRepository, never()).save(any(Payments.class));
    }

    @Test
    void savePaymentUrl_shouldThrowException_whenPaymentNotFound() {
        when(paymentRepository.findByIdForUpdate("payment-001"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentConfirmationService.savePaymentUrl(
                "payment-001",
                "https://example.com/checkout"));

        verify(paymentRepository, never()).save(any(Payments.class));
    }
}
