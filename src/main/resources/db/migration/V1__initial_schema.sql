CREATE DATABASE IF NOT EXISTS airline_booking
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE airline_booking;

CREATE TABLE roles (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,

    CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE TABLE users (
    id CHAR(36) PRIMARY KEY,

    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    full_name VARCHAR(150) NOT NULL,
    
	status ENUM('ACTIVE', 'BLOCKED', 'DELETED') NOT NULL DEFAULT 'ACTIVE',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE user_roles (
    user_id CHAR(36) NOT NULL,
    role_id CHAR(36) NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_users
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_roles
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE
);

CREATE TABLE airports (
    id CHAR(36) PRIMARY KEY,

    code VARCHAR(3) NOT NULL,
    name VARCHAR(150) NOT NULL,

    city VARCHAR(100),
    country VARCHAR(100),

    status ENUM('ACTIVE', 'BLOCKED', 'DELETED') NOT NULL DEFAULT 'ACTIVE',


    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_airports_code UNIQUE (code)
);

CREATE TABLE aircrafts (
    id CHAR(36) PRIMARY KEY,

    registration_no VARCHAR(50) NOT NULL,

    status ENUM('ACTIVE', 'BLOCKED', 'DELETED') NOT NULL DEFAULT 'ACTIVE',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_aircrafts_registration
        UNIQUE (registration_no)

);

CREATE TABLE aircraft_seats (
    id CHAR(36) PRIMARY KEY,

    aircraft_id CHAR(36) NOT NULL,

    seat_number VARCHAR(10) NOT NULL,
	
	seat_class ENUM(
	        'ECONOMY',
	        'PREMIUM_ECONOMY',
	        'BUSINESS',
	        'FIRST'
	    ) NOT NULL ,

    CONSTRAINT fk_aircraft_seats_aircrafts
        FOREIGN KEY (aircraft_id)
        REFERENCES aircrafts(id),

    CONSTRAINT uk_aircraft_seat
        UNIQUE (aircraft_id, seat_number)
);

CREATE TABLE flights (
    id CHAR(36) PRIMARY KEY,

    flight_number VARCHAR(20) NOT NULL,

    departure_airport_id CHAR(36) NOT NULL,
    arrival_airport_id CHAR(36) NOT NULL,

    status ENUM(
        'ACTIVE',
        'INACTIVE'
    ) NOT NULL DEFAULT 'ACTIVE',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_flights_departure_airports
        FOREIGN KEY (departure_airport_id)
        REFERENCES airports(id),

    CONSTRAINT fk_flights_arrival_airports
        FOREIGN KEY (arrival_airport_id)
        REFERENCES airports(id),

    CONSTRAINT chk_flights_different_airports
        CHECK (departure_airport_id <> arrival_airport_id),
    
    CONSTRAINT uk_flights_flight_number 
	    UNIQUE (flight_number),
	    
	CONSTRAINT uk_flights_route 
		UNIQUE (departure_airport_id, arrival_airport_id)
);


CREATE TABLE flight_schedules (
    id CHAR(36) PRIMARY KEY,

    flight_id CHAR(36) NOT NULL,
    aircraft_id CHAR(36) NOT NULL,

    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,

    base_price DECIMAL(15,2) NOT NULL,

    status ENUM(
        'SCHEDULED',
        'BOARDING',
        'DEPARTED',
        'COMPLETED',
        'CANCELLED'
    ) NOT NULL DEFAULT 'SCHEDULED',
    
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_flight_schedules_flight
        FOREIGN KEY (flight_id)
        REFERENCES flights(id),

    CONSTRAINT fk_flight_schedules_aircraft
        FOREIGN KEY (aircraft_id)
        REFERENCES aircrafts(id),

    CONSTRAINT chk_flight_schedule_time
        CHECK (arrival_time > departure_time),

    CONSTRAINT chk_flight_schedule_price
        CHECK (base_price >= 0)

);

CREATE TABLE bookings (
    id CHAR(36) PRIMARY KEY,

    booking_code VARCHAR(30) NOT NULL,

    user_id CHAR(36) NOT NULL,
    flight_schedule_id CHAR(36) NOT NULL,

    status ENUM(
        'PENDING',
        'CONFIRMED',
        'CANCELLED',
        'EXPIRED'
    ) NOT NULL DEFAULT 'PENDING',

    total_amount DECIMAL(15,2) NOT NULL DEFAULT 0,

    expires_at DATETIME,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_bookings_code
        UNIQUE (booking_code),

    CONSTRAINT fk_bookings_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_bookings_schedule
        FOREIGN KEY (flight_schedule_id)
        REFERENCES flight_schedules(id),

    CONSTRAINT chk_booking_amount
        CHECK (total_amount >= 0)

);

CREATE TABLE flight_seats (
    id CHAR(36) PRIMARY KEY,

    flight_schedule_id CHAR(36) NOT NULL,
    aircraft_seat_id CHAR(36) NOT NULL,

    status ENUM(
        'AVAILABLE',
        'HELD',
        'BOOKED'
    ) NOT NULL DEFAULT 'AVAILABLE',

    price DECIMAL(15,2) NOT NULL,

    held_by_booking_id CHAR(36),
    held_until DATETIME,

    CONSTRAINT fk_flight_seats_schedule
        FOREIGN KEY (flight_schedule_id)
        REFERENCES flight_schedules(id),

    CONSTRAINT fk_flight_seats_aircraft_seat
        FOREIGN KEY (aircraft_seat_id)
        REFERENCES aircraft_seats(id),

    CONSTRAINT fk_flight_seats_booking
        FOREIGN KEY (held_by_booking_id)
        REFERENCES bookings(id),

    CONSTRAINT uk_flight_schedule_seat
        UNIQUE (
            flight_schedule_id,
            aircraft_seat_id
        ),

    CONSTRAINT chk_flight_seat_price
        CHECK (price >= 0)

);


CREATE TABLE booking_passengers (
    id CHAR(36) PRIMARY KEY,

    booking_id CHAR(36) NOT NULL,
    flight_seat_id CHAR(36),

    full_name VARCHAR(150) NOT NULL,

    date_of_birth DATE,
    
    gender ENUM(
        'MALE',
        'FEMALE',
        'OTHER'
    ),

    document_number VARCHAR(100),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_booking_passengers_booking
        FOREIGN KEY (booking_id)
        REFERENCES bookings(id),

    CONSTRAINT fk_booking_passengers_seat
        FOREIGN KEY (flight_seat_id)
        REFERENCES flight_seats(id),

    CONSTRAINT uk_passenger_flight_seat
        UNIQUE (flight_seat_id)

);

CREATE TABLE payments (
    id CHAR(36) PRIMARY KEY,

    booking_id CHAR(36) NOT NULL,

    payment_method ENUM(
        'VNPAY',
        'MOMO',
        'CREDIT_CARD',
        'BANK_TRANSFER'
    ) NOT NULL,

    amount DECIMAL(15,2) NOT NULL,

    status ENUM(
        'PENDING',
        'SUCCESS',
        'FAILED',
        'REFUNDED'
    ) NOT NULL DEFAULT 'PENDING',

    transaction_id VARCHAR(150),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at DATETIME,

    CONSTRAINT fk_payments_booking
        FOREIGN KEY (booking_id)
        REFERENCES bookings(id),

    CONSTRAINT chk_payment_amount
        CHECK (amount >= 0)
);


-- =========================================================
-- BASIC INDEXES
-- Chỉ tạo những index rõ ràng phục vụ FK/query phổ biến trước
-- =========================================================

CREATE INDEX idx_user_roles_role_id
ON user_roles(role_id);

CREATE INDEX idx_aircraft_seats_aircraft_id
ON aircraft_seats(aircraft_id);

CREATE INDEX idx_flight_schedules_flight_departure
ON flight_schedules(
    flight_id,
    departure_time
);

CREATE INDEX idx_bookings_user_created
ON bookings(
    user_id,
    created_at
);

CREATE INDEX idx_flight_seats_schedule_status
ON flight_seats(
    flight_schedule_id,
    status
);

CREATE INDEX idx_payments_booking_created
ON payments(
    booking_id,
    created_at
);
