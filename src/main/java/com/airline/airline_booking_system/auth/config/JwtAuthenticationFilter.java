package com.airline.airline_booking_system.auth.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.airline.airline_booking_system.user.entity.Users;
import com.airline.airline_booking_system.user.repository.UserRepository;
import com.airline.airline_booking_system.user.service.JwtTokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, UserRepository userRepository) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            JwtTokenService.TokenClaims claims = jwtTokenService.verify(header.substring(7));
            if (claims != null) {
                Users user = userRepository.findById(claims.userId()).orElse(null);
                if (user != null && user.getStatus() == Users.UserStatus.ACTIVE) {
                    List<SimpleGrantedAuthority> authorities = claims.roles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
                    var authentication = new UsernamePasswordAuthenticationToken(user.getId(), null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
