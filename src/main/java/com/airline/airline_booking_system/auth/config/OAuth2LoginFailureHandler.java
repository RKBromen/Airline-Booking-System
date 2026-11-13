package com.airline.airline_booking_system.auth.config;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.airline.airline_booking_system.auth.service.OAuth2Service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final OAuth2Service oauth2Service;

    public OAuth2LoginFailureHandler(OAuth2Service oauth2Service) {
        this.oauth2Service = oauth2Service;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        boolean linkAttempt = session != null
                && session.getAttribute(OAuth2Service.LINK_FLOW_SESSION_KEY) instanceof OAuth2Service.LinkFlow;
        if (session != null) session.removeAttribute(OAuth2Service.LINK_FLOW_SESSION_KEY);
        String error = linkAttempt ? "oauth_link_failed" : "oauth_login_failed";
        String redirect = UriComponentsBuilder.fromUriString(oauth2Service.getFrontendRedirectUri())
                .queryParam("error", error).build().toUriString();
        getRedirectStrategy().sendRedirect(request, response, redirect);
    }
}
