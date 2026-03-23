package com.example.Subscription.Tracker.security;

import java.io.IOException;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.Subscription.Tracker.model.AppUser;
import com.example.Subscription.Tracker.repository.AppUserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public OAuth2LoginSuccessHandler(
        AppUserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            response.sendRedirect("/?auth=failed");
            return;
        }

        OAuth2User oauthUser = oauthToken.getPrincipal();
        String email = oauthUser.getAttribute("email");
        if (email == null || email.isBlank()) {
            response.sendRedirect("/?auth=failed");
            return;
        }

        AppUser user = userRepository.findByEmail(email)
            .orElseGet(() -> {
                AppUser created = new AppUser();
                created.setEmail(email.toLowerCase());
                created.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                created.setVerified(true);
                return userRepository.save(created);
            });

        if (!user.isVerified()) {
            user.setVerified(true);
            userRepository.save(user);
        }

        UserPrincipal principal = new UserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash());
        String token = jwtService.generateToken(principal);
        response.sendRedirect("/?token=" + token);
    }
}
