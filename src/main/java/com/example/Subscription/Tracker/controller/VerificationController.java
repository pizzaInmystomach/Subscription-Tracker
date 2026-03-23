package com.example.Subscription.Tracker.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Subscription.Tracker.model.AppUser;
import com.example.Subscription.Tracker.model.EmailVerification;
import com.example.Subscription.Tracker.repository.AppUserRepository;
import com.example.Subscription.Tracker.security.UserPrincipal;
import com.example.Subscription.Tracker.service.EmailService;
import com.example.Subscription.Tracker.service.VerificationService;

@RestController
@RequestMapping("/api/verify")
public class VerificationController {

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AppUserRepository userRepository;

    @PostMapping("/request")
    public ResponseEntity<?> request(@RequestBody VerificationRequest body, Authentication authentication) {
        if (body.email() == null || body.email().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        if (body.platforms() == null || body.platforms().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Platforms required"));
        }
        AppUser user = getUser(authentication);
        if (!user.getEmail().equalsIgnoreCase(body.email().trim())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email must match the logged-in user"));
        }
        EmailVerification verification = verificationService.requestVerification(user, body.email(), body.platforms());
        emailService.sendVerificationEmail(verification.getEmail(), verification.getToken());
        return ResponseEntity.ok(Map.of("message", "Verification sent"));
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestParam String token) {
        verificationService.confirm(token);
        return ResponseEntity.ok(Map.of("message", "Verified"));
    }

    public record VerificationRequest(String email, List<String> platforms) {}

    private AppUser getUser(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(principal.getId())
            .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
