package com.airline.airline_booking_system.booking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airline.airline_booking_system.booking.dto.BookedPassengerResponse;
import com.airline.airline_booking_system.booking.dto.BookingCancelResponse;
import com.airline.airline_booking_system.booking.dto.BookingCreateRequest;
import com.airline.airline_booking_system.booking.dto.BookingDTO;
import com.airline.airline_booking_system.booking.dto.BookingDetail;
import com.airline.airline_booking_system.booking.dto.BookingDetailByCode;
import com.airline.airline_booking_system.booking.dto.BookingHistory;
import com.airline.airline_booking_system.booking.dto.BookingPassengerDTO;
import com.airline.airline_booking_system.booking.dto.BookingResponse;
import com.airline.airline_booking_system.booking.dto.PassengerCreateRequest;
import com.airline.airline_booking_system.booking.entity.BookingPassengers;
import com.airline.airline_booking_system.booking.entity.Bookings;
import com.airline.airline_booking_system.booking.repository.BookingPassengerRepository;
import com.airline.airline_booking_system.booking.repository.BookingRepository;
import com.airline.airline_booking_system.flight.dto.FlightDTO;
import com.airline.airline_booking_system.flight.repository.FlightRepository;
import com.airline.airline_booking_system.flightschedule.entity.FlightSchedules;
import com.airline.airline_booking_system.flightschedule.entity.FlightSeats;
import com.airline.airline_booking_system.flightschedule.repository.FlightScheduleRepository;
import com.airline.airline_booking_system.flightschedule.repository.FlightSeatRepository;
import com.airline.airline_booking_system.user.entity.Users;
import com.airline.airline_booking_system.user.repository.UserRepository;

@Service
@Transactional
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingPassengerRepository bookingPassengerRepository;
    private final FlightScheduleRepository flightScheduleRepository;
    private final FlightSeatRepository flightSeatRepository;
    private final UserRepository userRepository;
    private final FlightRepository flightRepository;

    public BookingService(BookingRepository bookingRepository, BookingPassengerRepository bookingPassengerRepository,
            FlightScheduleRepository flightScheduleRepository, FlightSeatRepository flightSeatRepository,
            UserRepository userRepository, FlightRepository flightRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingPassengerRepository = bookingPassengerRepository;
        this.flightScheduleRepository = flightScheduleRepository;
        this.flightSeatRepository = flightSeatRepository;
        this.userRepository = userRepository;
        this.flightRepository = flightRepository;
    }

    public FlightSchedules getFlightSchedules(String scheduleId) {
        return flightScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find flight schedule " + scheduleId));
    }

    private String generateBookingCode() {
        return "BK" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }

    private Users getActiveUser(String userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy người dùng: " + userId));

        if (user.getStatus() != Users.UserStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Tài khoản không ở trạng thái ACTIVE");
        }

        return user;
    }

    private void validateFlightSchedule(FlightSchedules schedule) {
        if (schedule.getStatus() != FlightSchedules.FlightSchedulesStatus.SCHEDULED) {
            throw new IllegalArgumentException(
                    "Lịch bay hiện không mở bán vé");
        }
    }

    private void validatePassengerRequests(BookingCreateRequest request) {
        if (request.passengers() == null || request.passengers().isEmpty()) {
            throw new IllegalArgumentException(
                    "Danh sách hành khách không được để trống");
        }

        Set<String> seatIds = new HashSet<>();

        for (var passenger : request.passengers()) {
            if (passenger.flightSeatId() == null
                    || passenger.flightSeatId().isBlank()) {
                throw new IllegalArgumentException(
                        "Mỗi hành khách phải chọn một ghế");
            }

            if (!seatIds.add(passenger.flightSeatId())) {
                throw new IllegalArgumentException(
                        "Không được chọn trùng ghế: " + passenger.flightSeatId());
            }
        }
    }

    public BookingResponse createBooking(
            String userId,
            BookingCreateRequest request) {

        // 1. Validate request và user
        validatePassengerRequests(request);
        Users user = getActiveUser(userId);

        // 2. Kiểm tra lịch bay
        FlightSchedules schedule = getFlightSchedules(request.flightScheduleId());

        validateFlightSchedule(schedule);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(15);

        // 3. Tìm các ghế, xác minh thuộc cùng lịch bay
        // LinkedHashMap giữ thứ tự ghế theo request
        Map<String, FlightSeats> selectedSeats = new LinkedHashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (PassengerCreateRequest passenger : request.passengers()) {
            FlightSeats seat = flightSeatRepository
                    .findById(passenger.flightSeatId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy ghế: " + passenger.flightSeatId()));

            if (seat.getFlight_schedules() == null
                    || !schedule.getId().equals(
                            seat.getFlight_schedules().getId())) {
                throw new IllegalArgumentException(
                        "Ghế không thuộc lịch bay đã chọn: " + seat.getId());
            }

            selectedSeats.put(seat.getId(), seat);
            totalAmount = totalAmount.add(seat.getPrice());
        }

        // 4. Tạo booking và lưu trước để có khóa ngoại
        Bookings booking = new Bookings();
        booking.setId(UUID.randomUUID().toString());
        booking.setBookingCode(generateBookingCode());
        booking.setUsers(user);
        booking.setFlight_schedules(schedule);
        booking.setStatus(Bookings.BookingStatus.PENDING);
        booking.setTotal_amount(totalAmount);
        booking.setExpires_at(expiresAt);
        booking.setCreated_at(now);
        booking.setUpdated_at(now);

        bookingRepository.saveAndFlush(booking);

        // 5. Giữ từng ghế bằng câu UPDATE có điều kiện
        for (FlightSeats seat : selectedSeats.values()) {
            int updated = flightSeatRepository.holdSeat(
                    booking.getId(),
                    expiresAt,
                    seat.getId(),
                    schedule.getId(),
                    now);

            if (updated != 1) {
                throw new IllegalArgumentException(
                        "Ghế không còn khả dụng: " + seat.getId());
            }

            // Đồng bộ Entity trong bộ nhớ với dữ liệu vừa cập nhật
            seat.setStatus(FlightSeats.FlightSeatsStatus.HELD);
            seat.setBookings(booking);
            seat.setHeld_until(expiresAt);
        }

        // 6. Tạo thông tin hành khách
        List<BookingPassengers> passengers = new ArrayList<>();
        List<BookedPassengerResponse> passengerResponses = new ArrayList<>();

        for (PassengerCreateRequest passenger : request.passengers()) {
            FlightSeats seat = selectedSeats.get(passenger.flightSeatId());

            if (passenger.fullName() == null
                    || passenger.fullName().isBlank()
                    || passenger.dateOfBirth() == null
                    || passenger.gender() == null
                    || passenger.documentNumber() == null
                    || passenger.documentNumber().isBlank()) {
                throw new IllegalArgumentException(
                        "Thông tin hành khách không được để trống");
            }

            LocalDate dateOfBirth;
            BookingPassengers.Gender gender;

            try {
                dateOfBirth = LocalDate.parse(passenger.dateOfBirth());
                gender = BookingPassengers.Gender.valueOf(
                        passenger.gender().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Ngày sinh hoặc giới tính không hợp lệ", ex);
            }

            BookingPassengers bookingPassenger = new BookingPassengers(
                    UUID.randomUUID().toString(),
                    booking,
                    seat,
                    passenger.fullName(),
                    dateOfBirth,
                    gender,
                    passenger.documentNumber(),
                    now);

            passengers.add(bookingPassenger);

            passengerResponses.add(new BookedPassengerResponse(
                    bookingPassenger.getId(),
                    bookingPassenger.getFull_name(),
                    seat.getSeats().getSeat_number()));
        }

        // 7. Lưu hành khách và cập nhật các quan hệ
        bookingPassengerRepository.saveAll(passengers);
        flightSeatRepository.saveAll(selectedSeats.values());

        booking.setSeats(new ArrayList<>(selectedSeats.values()));
        booking.setPassengers(passengers);

        bookingRepository.save(booking);

        // 8. Trả response
        return new BookingResponse(
                booking.getId(),
                booking.getBookingCode(),
                schedule.getId(),
                booking.getStatus().name(),
                booking.getTotal_amount(),
                booking.getExpires_at(),
                passengerResponses);
    }

    public List<BookingHistory> showHistory(String userId) {
        List<BookingHistory> res = bookingRepository.history(userId);
        return res;
    }

    public BookingDetail getDetail(String bookingId) {
        Bookings booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        FlightDTO flight = flightRepository.getFlight(bookingId);
        List<BookingPassengerDTO> passengers = bookingPassengerRepository.getPassenger(bookingId);
        BookingDetail detail = new BookingDetail(booking.getId(), booking.getBookingCode(),
                booking.getStatus().toString(), flight,
                passengers, booking.getTotal_amount(), booking.getExpires_at());
        return detail;
    }

    public BookingDetailByCode getDetailByCode(String bookingCode) {
        Bookings booking = bookingRepository.findByBookingCode(bookingCode);
        FlightDTO flight = flightRepository.getFlight(booking.getId());
        List<BookingPassengerDTO> passengers = bookingPassengerRepository.getPassenger(booking.getId());
        return new BookingDetailByCode(bookingCode, booking.getStatus().toString(), flight, passengers,
                booking.getTotal_amount(),
                booking.getExpires_at());
    }

    public List<BookingDTO> getAll() {
        return bookingRepository.getAll();
    }

    public BookingCancelResponse cancelBooking(String bookingId, String userId) {
        // 1. Tìm booking
        Bookings booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking"));

        // 2. Kiểm tra quyền sở hữu
        if (booking.getUsers() == null
                || !booking.getUsers().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Bạn không có quyền hủy booking này");
        }

        // 3. Chỉ cho phép hủy booking PENDING
        if (booking.getStatus() != Bookings.BookingStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Chỉ booking PENDING mới được hủy qua API này");
        }

        // 4. Giải phóng ghế đang được booking giữ
        flightSeatRepository.releaseHeldSeats(booking.getId());

        // 5. Cập nhật trạng thái booking
        booking.setStatus(Bookings.BookingStatus.CANCELLED);
        booking.setUpdated_at(LocalDateTime.now());

        bookingRepository.save(booking);

        // 6. Trả response
        return new BookingCancelResponse(
                booking.getId(),
                booking.getStatus().name());
    }
}
