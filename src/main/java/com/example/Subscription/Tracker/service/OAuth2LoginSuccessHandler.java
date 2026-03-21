package com.example.Subscription.Tracker.service;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.Subscription.Tracker.model.GmailAccount;
import com.example.Subscription.Tracker.repository.GmailAccountRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final GmailAccountRepository gmailAccountRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public OAuth2LoginSuccessHandler(
        GmailAccountRepository gmailAccountRepository,
        OAuth2AuthorizedClientService authorizedClientService
    ) {
        this.gmailAccountRepository = gmailAccountRepository;
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            String email = oauthToken.getPrincipal().getAttribute("email");
            if (email != null) {
                gmailAccountRepository.save(new GmailAccount(
                    email,
                    oauthToken.getName(),
                    LocalDateTime.now(),
                    null
                ));
            }
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName()
            );
            if (client != null && client.getRefreshToken() != null) {
                // Refresh token persisted by authorized client service.
            }
        }
        response.sendRedirect("/?gmail=connected");
    }
}
