package com.example.Subscription.Tracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Subscription.Tracker.model.AuthEmailVerificationToken;

public interface AuthEmailVerificationTokenRepository extends JpaRepository<AuthEmailVerificationToken, Long> {
    Optional<AuthEmailVerificationToken> findByToken(String token);
}
