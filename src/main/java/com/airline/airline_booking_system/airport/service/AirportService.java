package com.airline.airline_booking_system.airport.service;

import com.airline.airline_booking_system.airport.dtos.response.AirportResponse;
import com.airline.airline_booking_system.airport.entity.Airports;
import com.airline.airline_booking_system.airport.enums.AirportStatus;
import com.airline.airline_booking_system.airport.repository.AirportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AirportService {
    private final AirportRepository airportRepository;

    @Transactional()
    public List<AirportResponse> getAllAirportsByStatus ()
    {
        List<Airports> airports =
                airportRepository.findByStatus(AirportStatus.ACTIVE);

        return airports.stream()
                .map(airport -> new AirportResponse(
                        airport.getId(),
                        airport.getCode(),
                        airport.getName(),
                        airport.getCity(),
                        airport.getCountry()
                ))
                .toList();

    }


}
