package com.airline.airline_booking_system.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airline.airline_booking_system.booking.entity.BookingPassengers;
import com.airline.airline_booking_system.booking.entity.Bookings;
import com.airline.airline_booking_system.booking.repository.BookingRepository;
import com.airline.airline_booking_system.flightschedule.entity.FlightSeats;
import com.airline.airline_booking_system.flightschedule.enums.FlightSeatStatus;
import com.airline.airline_booking_system.flightschedule.repository.FlightSeatRepository;
import com.airline.airline_booking_system.payment.dto.VnPayIPNResponse;
import com.airline.airline_booking_system.payment.entity.Payments;
import com.airline.airline_booking_system.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VnPayIPNService {
    private final VnPaySignatureService signatureService;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final FlightSeatRepository flightSeatRepository;

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Transactional
    public VnPayIPNResponse validateIpn(Map<String, String> params) {

        // 1. Verify signature
        if (!signatureService.verifySignature(params)) {
            return response("97", "Invalid signature");
        }

        // 2. Verify merchant
        String receivedTmnCode = params.get("vnp_TmnCode");

        if (receivedTmnCode == null
                || !tmnCode.equals(receivedTmnCode)) {
            return response("97", "Invalid merchant");
        }

        // 3. Validate required parameters
        String transactionId = params.get("vnp_TxnRef");
        String receivedAmount = params.get("vnp_Amount");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");

        if (transactionId == null || transactionId.isBlank()) {
            return response("01", "Transaction not found");
        }

        if (receivedAmount == null || receivedAmount.isBlank()) {
            return response("04", "Invalid amount");
        }

        if (responseCode == null || transactionStatus == null) {
            return response("99", "Missing transaction information");
        }

        // 4. Find payment without lock to obtain booking ID
        Payments initialPayment = paymentRepository
                .findByTransactionId(transactionId)
                .orElse(null);

        if (initialPayment == null) {
            return response("01", "Transaction not found");
        }

        String bookingId = initialPayment.getBookings().getId();

        // 5. Lock booking first
        Bookings booking = bookingRepository
                .findByIdForUpdate(bookingId)
                .orElse(null);

        if (booking == null) {
            return response("01", "Booking not found");
        }

        // 6. Lock payment and verify relationship
        Payments payment = paymentRepository
                .findByTransactionIdForUpdate(transactionId)
                .orElse(null);

        if (payment == null
                || !payment.getBookings().getId().equals(bookingId)) {
            return response("01", "Transaction not found");
        }

        // 7. Verify amount
        try {
            BigDecimal actualAmount = new BigDecimal(receivedAmount);

            BigDecimal expectedAmount = payment.getAmount()
                    .movePointRight(2);

            if (actualAmount.compareTo(expectedAmount) != 0) {
                return response("04", "Invalid amount");
            }

        } catch (NumberFormatException e) {
            return response("04", "Invalid amount");
        }

        // 8. Check payment method
        if (payment.getPayment_method() != Payments.PaymentMethod.VNPAY) {
            return response("05", "Invalid payment method");
        }

        // 9. Check transaction result
        boolean success = "00".equals(responseCode)
                && "00".equals(transactionStatus);

        // 10. Idempotency
        if (payment.getStatus() == Payments.PaymentStatus.SUCCESS) {
            return response("02", "Transaction already confirmed");
        }

        if (payment.getStatus() == Payments.PaymentStatus.FAILED) {
            return response("02", "Transaction already processed");
        }

        if (payment.getStatus() != Payments.PaymentStatus.PENDING) {
            return response("05", "Invalid payment status");
        }

        LocalDateTime now = LocalDateTime.now(VN_ZONE);

        // 11. Failed transaction
        if (!success) {
            payment.setStatus(Payments.PaymentStatus.FAILED);
            paymentRepository.save(payment);

            return response("00", "Confirm Success");
        }

        // 12. Record successful payment
        // Even a late payment must be recorded as SUCCESS.
        payment.setStatus(Payments.PaymentStatus.SUCCESS);
        payment.setPaid_at(now);

        paymentRepository.save(payment);

        // 13. Handle expired or cancelled booking
        boolean expired = booking.getExpires_at() == null
                || !booking.getExpires_at().isAfter(now);

        if (booking.getStatus() != Bookings.BookingStatus.PENDING
                || expired) {

            if (booking.getStatus() == Bookings.BookingStatus.PENDING
                    && expired) {

                booking.setStatus(Bookings.BookingStatus.EXPIRED);
                booking.setUpdated_at(now);

                flightSeatRepository.releaseHeldSeats(bookingId);

                bookingRepository.save(booking);
            }

            log.warn(
                    "Late/invalid booking payment. BookingId={}, PaymentId={}. Manual refund reconciliation required.",
                    bookingId, payment.getId());

            return response("00", "Confirm Success");
        }

        // 14. Lock all seats belonging to this booking
        List<FlightSeats> seats = flightSeatRepository
                .findAllByBookingIdForUpdate(bookingId);

        // 15. Validate passengers and seat assignments
        List<BookingPassengers> passengers = booking.getPassengers();

        boolean validPassengerCount = passengers != null
                && !passengers.isEmpty()
                && seats.size() == passengers.size();

        boolean validSeatAssignments = validPassengerCount
                && passengers.stream().allMatch(passenger -> passenger.getFlightSeats() != null
                        && seats.stream().anyMatch(seat -> seat.getId().equals(
                                passenger.getFlightSeats().getId())));

        // 16. Validate held seats
        boolean validSeats = validPassengerCount
                && validSeatAssignments
                && seats.stream().allMatch(seat -> seat.getStatus() == FlightSeatStatus.HELD
                        && seat.getHeld_until() != null
                        && seat.getHeld_until().isAfter(now));

        // 17. Confirm booking
        booking.setStatus(Bookings.BookingStatus.CONFIRMED);
        booking.setUpdated_at(now);

        bookingRepository.save(booking);

        log.info(
                "VNPay payment confirmed. BookingId={}, PaymentId={}",
                bookingId, payment.getId());

        return response("00", "Confirm Success");
    }

    private VnPayIPNResponse response(String code, String message) {
        return new VnPayIPNResponse(code, message);
    }
}
