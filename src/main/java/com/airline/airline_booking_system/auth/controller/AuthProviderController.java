package com.airline.airline_booking_system.auth.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.airline.airline_booking_system.auth.dto.ProviderListResponse;
import com.airline.airline_booking_system.auth.enums.AuthProvider;
import com.airline.airline_booking_system.auth.repository.UserAuthProviderRepository;
import com.airline.airline_booking_system.user.repository.UserRepository;

@RestController
@RequestMapping("/api/v1/auth/providers")
public class AuthProviderController {
    private final UserAuthProviderRepository providerRepository;
    private final UserRepository userRepository;

    public AuthProviderController(UserAuthProviderRepository providerRepository, UserRepository userRepository) {
        this.providerRepository = providerRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ProviderListResponse list(Authentication authentication) {
        String userId = authentication.getName();
        Set<AuthProvider> linked = providerRepository.findAllByUser_Id(userId).stream()
                .map(provider -> provider.getProvider()).collect(Collectors.toSet());
        boolean hasPassword = userRepository.findById(userId).map(user -> user.getPassword() != null).orElse(false);
        List<ProviderListResponse.ProviderItem> providers = List.of(AuthProvider.values()).stream()
                .map(provider -> new ProviderListResponse.ProviderItem(provider.name(), linked.contains(provider))).toList();
        return new ProviderListResponse(providers, hasPassword);
    }
}
