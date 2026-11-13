package com.airline.airline_booking_system.auth.controller;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.airline.airline_booking_system.auth.dto.ProviderListResponse;
import com.airline.airline_booking_system.auth.enums.AuthProvider;
import com.airline.airline_booking_system.auth.repository.UserAuthProviderRepository;
import com.airline.airline_booking_system.auth.service.OAuth2Service;
import com.airline.airline_booking_system.user.repository.UserRepository;

@RestController
@RequestMapping("/api/v1/auth/providers")
public class AuthProviderController {
    private final UserAuthProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final OAuth2Service oauth2Service;

    public AuthProviderController(UserAuthProviderRepository providerRepository, UserRepository userRepository,
            OAuth2Service oauth2Service) {
        this.providerRepository = providerRepository;
        this.userRepository = userRepository;
        this.oauth2Service = oauth2Service;
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

    @PostMapping("/{provider}/link")
    public LinkAuthorizationResponse link(@PathVariable AuthProvider provider, Authentication authentication,
            HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        session.setAttribute(OAuth2Service.LINK_FLOW_SESSION_KEY,
                new OAuth2Service.LinkFlow(authentication.getName(), provider, Instant.now()));
        String authorizationUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/oauth2/authorization/{provider}").buildAndExpand(provider.name().toLowerCase()).toUriString();
        return new LinkAuthorizationResponse(authorizationUrl);
    }

    @DeleteMapping("/{provider}")
    public ResponseEntity<Void> unlink(@PathVariable AuthProvider provider, Authentication authentication) {
        oauth2Service.unlink(authentication.getName(), provider);
        return ResponseEntity.noContent().build();
    }

    public record LinkAuthorizationResponse(String authorizationUrl) { }
}
