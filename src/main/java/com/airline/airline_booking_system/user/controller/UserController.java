package com.airline.airline_booking_system.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.airline.airline_booking_system.user.dto.ChangePasswordRequest;
import com.airline.airline_booking_system.user.dto.PageResponse;
import com.airline.airline_booking_system.user.dto.UpdateProfileRequest;
import com.airline.airline_booking_system.user.dto.UserCreateRequest;
import com.airline.airline_booking_system.user.dto.UserResponse;
import com.airline.airline_booking_system.user.dto.UserStatusRequest;
import com.airline.airline_booking_system.user.dto.UserUpdateRequest;
import com.airline.airline_booking_system.user.entity.Users;
import com.airline.airline_booking_system.user.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping
    public PageResponse<UserResponse> list(@RequestParam(required = false) Users.UserStatus status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort) {
        return userService.list(status, page, size, sort);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable String id) { return userService.get(id); }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PatchMapping("/{id}")
    public UserResponse update(@PathVariable String id, @Valid @RequestBody UserUpdateRequest request) {
        return userService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public UserResponse updateStatus(@PathVariable String id, @Valid @RequestBody UserStatusRequest request) {
        return userService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me")
    public UserResponse updateMe(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateOwnProfile(authentication.getName(), request);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changeOwnPassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
