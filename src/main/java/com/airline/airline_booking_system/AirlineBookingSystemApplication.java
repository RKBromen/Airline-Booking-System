package com.airline.airline_booking_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AirlineBookingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(AirlineBookingSystemApplication.class, args);
	}

}
