package com.example.Subscription.Tracker.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.Subscription.Tracker.model.AppUser;
import com.example.Subscription.Tracker.model.AuthEmailVerificationToken;
import com.example.Subscription.Tracker.repository.AppUserRepository;
import com.example.Subscription.Tracker.repository.AuthEmailVerificationTokenRepository;

@Service
public class AuthVerificationService {

    private final AuthEmailVerificationTokenRepository tokenRepository;
    private final AppUserRepository userRepository;

    public AuthVerificationService(
        AuthEmailVerificationTokenRepository tokenRepository,
        AppUserRepository userRepository
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    public AuthEmailVerificationToken createToken(AppUser user) {
        AuthEmailVerificationToken token = new AuthEmailVerificationToken();
        token.setUserId(user.getId());
        token.setToken(UUID.randomUUID().toString());
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        return tokenRepository.save(token);
    }

    public boolean verify(String tokenValue) {
        AuthEmailVerificationToken token = tokenRepository.findByToken(tokenValue)
            .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (token.getVerifiedAt() != null) {
            return true;
        }
        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired");
        }
        token.setVerifiedAt(LocalDateTime.now());
        tokenRepository.save(token);

        AppUser user = userRepository.findById(token.getUserId())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        user.setVerified(true);
        userRepository.save(user);
        return true;
    }
}
