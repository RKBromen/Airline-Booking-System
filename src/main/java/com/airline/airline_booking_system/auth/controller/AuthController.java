package com.airline.airline_booking_system.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.airline.airline_booking_system.auth.config.RefreshCookieFactory;
import com.airline.airline_booking_system.auth.dto.AuthSession;
import com.airline.airline_booking_system.auth.dto.CurrentUserResponse;
import com.airline.airline_booking_system.auth.dto.OAuthExchangeRequest;
import com.airline.airline_booking_system.auth.service.OAuth2Service;
import com.airline.airline_booking_system.user.dto.LoginRequest;
import com.airline.airline_booking_system.user.dto.RegisterRequest;
import com.airline.airline_booking_system.user.dto.RegisteredUserResponse;
import com.airline.airline_booking_system.user.entity.Users;
import com.airline.airline_booking_system.user.repository.UserRepository;
import com.airline.airline_booking_system.user.repository.UserRoleRepository;
import com.airline.airline_booking_system.user.service.AuthenticationService;
import com.airline.airline_booking_system.user.service.RegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;
    private final OAuth2Service oauth2Service;
    private final RefreshCookieFactory cookieFactory;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public AuthController(RegistrationService registrationService, AuthenticationService authenticationService,
            OAuth2Service oauth2Service, RefreshCookieFactory cookieFactory, UserRepository userRepository,
            UserRoleRepository userRoleRepository) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
        this.oauth2Service = oauth2Service;
        this.cookieFactory = cookieFactory;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisteredUserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, CsrfToken csrfToken) {
        csrfToken.getToken();
        return withRefreshCookie(authenticationService.login(request));
    }

    @GetMapping("/oauth2/google")
    public RedirectView googleLogin() { return new RedirectView("/oauth2/authorization/google"); }

    @GetMapping("/oauth2/facebook")
    public RedirectView facebookLogin() { return new RedirectView("/oauth2/authorization/facebook"); }

    @PostMapping("/oauth2/exchange")
    public ResponseEntity<?> exchangeOAuthCode(@Valid @RequestBody OAuthExchangeRequest request, CsrfToken csrfToken) {
        csrfToken.getToken();
        return withRefreshCookie(oauth2Service.exchange(request.code()));
    }

    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        Users user = currentUser(authentication);
        return new CurrentUserResponse(user.getId(), user.getEmail(), user.getFull_name(), user.getStatus().name(),
                userRoleRepository.findRoleNamesByUserId(user.getId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = RefreshCookieFactory.COOKIE_NAME, required = false) String token) {
        authenticationService.logout(token);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookieFactory.clear().toString()).build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refresh(@CookieValue(name = RefreshCookieFactory.COOKIE_NAME, required = false) String token) {
        return withRefreshCookie(authenticationService.refresh(token));
    }

    private ResponseEntity<?> withRefreshCookie(AuthSession session) {
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookieFactory.issue(session.refreshToken()).toString())
                .body(session.response());
    }

    private Users currentUser(Authentication authentication) {
        return userRepository.findById(authentication.getName())
                .filter(user -> user.getStatus() == Users.UserStatus.ACTIVE)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Account is not active"));
    }
}
