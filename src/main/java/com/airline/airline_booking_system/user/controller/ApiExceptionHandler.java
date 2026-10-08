package com.airline.airline_booking_system.user.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.airline.airline_booking_system.user.service.EmailAlreadyRegisteredException;
import com.airline.airline_booking_system.user.service.InvalidCredentialsException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmail(EmailAlreadyRegisteredException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", exception.getMessage()));
    }
}
