package com.airline.airline_booking_system.common.exceptions;

public class AirportCityAndCountryAlreadyExistException extends RuntimeException {
    public AirportCityAndCountryAlreadyExistException(String city, String country)
    {
        super("Airport city: "+city+" and country: "+country+" already exists");
    }
}