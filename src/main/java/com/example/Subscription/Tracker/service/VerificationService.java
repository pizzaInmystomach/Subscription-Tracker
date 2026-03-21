package com.example.Subscription.Tracker.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Subscription.Tracker.model.EmailVerification;
import com.example.Subscription.Tracker.model.Subscription;
import com.example.Subscription.Tracker.repository.EmailVerificationRepository;
import com.example.Subscription.Tracker.repository.SubscriptionRepository;

@Service
public class VerificationService {

    @Autowired
    private EmailVerificationRepository verificationRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    // Create a new verification request and return the entity (including token)
    public EmailVerification requestVerification(String email, List<String> platforms) {
        String token = UUID.randomUUID().toString();
        EmailVerification verification = new EmailVerification();
        verification.setEmail(normalizeEmail(email));
        verification.setToken(token);
        verification.setPlatformsCsv(String.join(",", normalizePlatforms(platforms)));
        verification.setCreatedAt(LocalDateTime.now());
        verification.setVerifiedAt(null);

        return verificationRepository.save(verification);
    }

    // Confirm the verification token and create subscriptions if valid
    public List<Subscription> confirm(String token) {
        EmailVerification verification = verificationRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (verification.getVerifiedAt() != null) {
            return List.of();
        }

        verification.setVerifiedAt(LocalDateTime.now());
        verificationRepository.save(verification);

        List<String> platforms = splitPlatforms(verification.getPlatformsCsv());
        List<Subscription> created = new ArrayList<>();
        // For each platform, create a subscription if it doesn't already exist for this email
        for (String name : platforms) {
            if (name.isBlank()) continue;
            if (subscriptionRepository.existsByEmailIgnoreCaseAndNameIgnoreCase(
                verification.getEmail(), name)) {
                continue;
            }
            Subscription sub = new Subscription();
            sub.setName(name);
            sub.setEmail(verification.getEmail());
            sub.setStatus("PENDING_DETAILS");
            sub.setLastStatusNote("Awaiting billing details.");
            sub.setLastStatusAt(LocalDateTime.now().toLocalDate());
            created.add(subscriptionRepository.save(sub));
        }
        return created;
    }

    // Helper methods for normalization and parsing
    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    // Normalize platform names by trimming and removing empty entries
    private List<String> normalizePlatforms(List<String> platforms) {
        if (platforms == null) return List.of();
        List<String> cleaned = new ArrayList<>();
        for (String p : platforms) {
            if (p == null) continue;
            String name = p.trim();
            if (!name.isEmpty()) cleaned.add(name);
        }
        return cleaned;
    }

    // Split the CSV string of platforms back into a list, trimming whitespace
    private List<String> splitPlatforms(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        String[] parts = csv.split(",");
        List<String> items = new ArrayList<>();
        for (String part : parts) {
            String name = part.trim();
            if (!name.isEmpty()) items.add(name);
        }
        return items;
    }
}
