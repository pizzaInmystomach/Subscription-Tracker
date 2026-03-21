package com.example.Subscription.Tracker.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Service;

import com.example.Subscription.Tracker.model.OAuth2AuthorizedClientEntity;
import com.example.Subscription.Tracker.model.OAuth2AuthorizedClientKey;
import com.example.Subscription.Tracker.repository.OAuth2AuthorizedClientEntityRepository;

@Service
public class JpaOAuth2AuthorizedClientService implements OAuth2AuthorizedClientService {

    private final OAuth2AuthorizedClientEntityRepository repository;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public JpaOAuth2AuthorizedClientService(
        OAuth2AuthorizedClientEntityRepository repository,
        ClientRegistrationRepository clientRegistrationRepository
    ) {
        this.repository = repository;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(
        String clientRegistrationId,
        String principalName
    ) {
        OAuth2AuthorizedClientEntity entity = repository
            .findById(new OAuth2AuthorizedClientKey(clientRegistrationId, principalName))
            .orElse(null);
        if (entity == null) {
            return null;
        }

        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
        if (registration == null) {
            return null;
        }

        Set<String> scopes = parseScopes(entity.getAccessTokenScopes());
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            entity.getAccessTokenValue(),
            entity.getAccessTokenIssuedAt(),
            entity.getAccessTokenExpiresAt(),
            scopes
        );

        OAuth2RefreshToken refreshToken = null;
        if (entity.getRefreshTokenValue() != null) {
            refreshToken = new OAuth2RefreshToken(entity.getRefreshTokenValue(), Instant.now());
        }

        @SuppressWarnings("unchecked")
        T client = (T) new OAuth2AuthorizedClient(registration, principalName, accessToken, refreshToken);
        return client;
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();

        OAuth2AuthorizedClientEntity entity = new OAuth2AuthorizedClientEntity();
        entity.setClientRegistrationId(authorizedClient.getClientRegistration().getRegistrationId());
        entity.setPrincipalName(principal.getName());
        entity.setAccessTokenValue(accessToken.getTokenValue());
        entity.setAccessTokenIssuedAt(accessToken.getIssuedAt());
        entity.setAccessTokenExpiresAt(accessToken.getExpiresAt());
        entity.setAccessTokenScopes(String.join(",", accessToken.getScopes()));
        entity.setRefreshTokenValue(refreshToken == null ? null : refreshToken.getTokenValue());

        repository.save(entity);
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
        repository.deleteById(new OAuth2AuthorizedClientKey(clientRegistrationId, principalName));
    }

    private Set<String> parseScopes(String scopesCsv) {
        if (scopesCsv == null || scopesCsv.isBlank()) {
            return Set.of();
        }
        return new HashSet<>(Arrays.asList(scopesCsv.split(",")));
    }
}
