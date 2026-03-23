package com.example.Subscription.Tracker.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Subscription.Tracker.model.AppUser;
import com.example.Subscription.Tracker.model.AuthEmailVerificationToken;
import com.example.Subscription.Tracker.repository.AppUserRepository;
import com.example.Subscription.Tracker.security.JwtService;
import com.example.Subscription.Tracker.security.UserPrincipal;
import com.example.Subscription.Tracker.service.AuthVerificationService;
import com.example.Subscription.Tracker.service.EmailService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthVerificationService authVerificationService;
    private final EmailService emailService;

    public AuthController(
        AppUserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        AuthVerificationService authVerificationService,
        EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.authVerificationService = authVerificationService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        if (request.email() == null || request.email().isBlank()
            || request.password() == null || request.password().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setVerified(false);
        userRepository.save(user);

        AuthEmailVerificationToken token = authVerificationService.createToken(user);
        emailService.sendAuthVerificationEmail(user.getEmail(), token.getToken());
        return ResponseEntity.ok(Map.of("message", "Verification email sent"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        String email = request.email() == null ? "" : request.email().trim().toLowerCase();
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, request.password())
        );
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        AppUser user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        if (!user.isVerified()) {
            return ResponseEntity.status(403).body(Map.of("error", "Email not verified"));
        }
        String token = jwtService.generateToken(principal);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest request) {
        try {
            authVerificationService.verify(request.token());
            return ResponseEntity.ok(Map.of("message", "Verified"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    public record AuthRequest(String email, String password) {}
    public record VerifyRequest(String token) {}
}
