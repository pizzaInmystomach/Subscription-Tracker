package com.example.Subscription.Tracker.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(OAuth2AuthorizedClientKey.class)
public class OAuth2AuthorizedClientEntity {
    @Id
    private String clientRegistrationId;

    @Id
    private String principalName;

    private String accessTokenValue;
    private Instant accessTokenIssuedAt;
    private Instant accessTokenExpiresAt;
    private String accessTokenScopes;
    private String refreshTokenValue;
}
