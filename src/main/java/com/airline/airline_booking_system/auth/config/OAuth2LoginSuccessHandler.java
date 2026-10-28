package com.airline.airline_booking_system.auth.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.airline.airline_booking_system.auth.enums.AuthProvider;
import com.airline.airline_booking_system.auth.service.OAuth2Service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final OAuth2Service oauth2Service;

    public OAuth2LoginSuccessHandler(OAuth2Service oauth2Service) { this.oauth2Service = oauth2Service; }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
            AuthProvider provider = AuthProvider.valueOf(oauth.getAuthorizedClientRegistrationId().toUpperCase());
            String code = oauth2Service.issueHandoff(oauth2Service.loginOrRegister(provider,
                    (OAuth2User) oauth.getPrincipal()));
            String redirect = UriComponentsBuilder.fromUriString(oauth2Service.getFrontendRedirectUri())
                    .queryParam("code", code).build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, redirect);
        } catch (RuntimeException exception) {
            String redirect = UriComponentsBuilder.fromUriString(oauth2Service.getFrontendRedirectUri())
                    .queryParam("error", "oauth_login_failed").build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, redirect);
        }
    }
}
