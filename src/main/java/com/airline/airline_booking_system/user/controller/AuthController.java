package com.airline.airline_booking_system.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.airline.airline_booking_system.user.dto.RegisterRequest;
import com.airline.airline_booking_system.user.dto.RegisteredUserResponse;
import com.airline.airline_booking_system.user.dto.LoginRequest;
import com.airline.airline_booking_system.user.dto.LoginResponse;
import com.airline.airline_booking_system.user.service.AuthenticationService;
import com.airline.airline_booking_system.user.service.RegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;

    public AuthController(RegistrationService registrationService, AuthenticationService authenticationService) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisteredUserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }
}
