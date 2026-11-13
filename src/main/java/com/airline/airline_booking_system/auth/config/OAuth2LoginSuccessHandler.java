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
import jakarta.servlet.http.HttpSession;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final OAuth2Service oauth2Service;

    public OAuth2LoginSuccessHandler(OAuth2Service oauth2Service) { this.oauth2Service = oauth2Service; }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        boolean linkAttempt = false;
        try {
            OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
            AuthProvider provider = AuthProvider.valueOf(oauth.getAuthorizedClientRegistrationId().toUpperCase());
            HttpSession session = request.getSession(false);
            Object pending = session == null ? null : session.getAttribute(OAuth2Service.LINK_FLOW_SESSION_KEY);
            linkAttempt = pending instanceof OAuth2Service.LinkFlow;
            if (session != null) session.removeAttribute(OAuth2Service.LINK_FLOW_SESSION_KEY);
            if (pending instanceof OAuth2Service.LinkFlow flow) {
                if (!oauth2Service.isValidLinkFlow(flow, provider)) {
                    throw new IllegalStateException("OAuth account-link request expired");
                }
                oauth2Service.link(flow.userId(), provider, (OAuth2User) oauth.getPrincipal());
                String redirect = UriComponentsBuilder.fromUriString(oauth2Service.getFrontendRedirectUri())
                        .queryParam("linked", provider.name()).build().toUriString();
                getRedirectStrategy().sendRedirect(request, response, redirect);
                return;
            }
            String code = oauth2Service.issueHandoff(oauth2Service.loginOrRegister(provider,
                    (OAuth2User) oauth.getPrincipal()));
            String redirect = UriComponentsBuilder.fromUriString(oauth2Service.getFrontendRedirectUri())
                    .queryParam("code", code).build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, redirect);
        } catch (RuntimeException exception) {
            String redirect = UriComponentsBuilder.fromUriString(oauth2Service.getFrontendRedirectUri())
                    .queryParam("error", linkAttempt ? "oauth_link_failed" : "oauth_login_failed").build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, redirect);
        }
    }
}
