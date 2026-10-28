package com.airline.airline_booking_system.airport.service;

import com.airline.airline_booking_system.airport.dtos.request.AirportRequest;
import com.airline.airline_booking_system.airport.dtos.response.AirportResponse;
import com.airline.airline_booking_system.airport.entity.Airports;
import com.airline.airline_booking_system.airport.enums.AirportStatus;
import com.airline.airline_booking_system.airport.repository.AirportRepository;
import com.airline.airline_booking_system.common.exceptions.AirportCityAndCountryAlreadyExistException;
import com.airline.airline_booking_system.common.exceptions.AirportCodeAlreadyExistException;
import com.airline.airline_booking_system.common.exceptions.AirportNameAlreadyExistException;
import com.airline.airline_booking_system.common.exceptions.AirportNotFoundException;
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

    @Transactional()
    public AirportResponse getAirportByIdAndStatus(UUID id)
    {
        Airports airport = airportRepository
                .findByIdAndStatus(id, AirportStatus.ACTIVE)
                .orElseThrow(()-> new AirportNotFoundException(id));

        return  new AirportResponse(
                airport.getId(),
                airport.getCode(),
                airport.getName(),
                airport.getCity(),
                airport.getCountry()
        );
    }
    public AirportResponse createAirport(AirportRequest request)
    {
        String code = request.code().trim();
        String city = request.city().trim();
        String country = request.country().trim();
        String name = request.name().trim();
        if(airportRepository.existsByCode((code)))
            throw new AirportCodeAlreadyExistException(code);

        if(airportRepository.existsByCityAndCountry(city, country ))
            throw new AirportCityAndCountryAlreadyExistException(city, country);

        if(airportRepository.existsByName(name))
            throw new AirportNameAlreadyExistException(name);

        Airports airport = new Airports();

        airport.setCode(code);
        airport.setName(name);
        airport.setCity(city);
        airport.setCountry(country);
        airport.setStatus(AirportStatus.ACTIVE);

        Airports savedAirport = airportRepository.saveAndFlush(airport);

        return new AirportResponse(
                airport.getId(),
                airport.getCode(),
                airport.getName(),
                airport.getCity(),
                airport.getCountry()
        );

    }
}
