package com.example.Subscription.Tracker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Subscription.Tracker.model.AppUser;
import com.example.Subscription.Tracker.model.Subscription;
import com.example.Subscription.Tracker.repository.AppUserRepository;
import com.example.Subscription.Tracker.security.UserPrincipal;
import com.example.Subscription.Tracker.service.SubscriptionService;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    @Autowired
    private SubscriptionService service;

    @Autowired
    private AppUserRepository userRepository;

    @GetMapping
    public List<Subscription> list(Authentication authentication) {
        AppUser user = getUser(authentication);
        return service.getAllByUser(user);
    }

    @PostMapping
    public Subscription add(@RequestBody Subscription sub, Authentication authentication) {
        AppUser user = getUser(authentication);
        return service.createForUser(user, sub);
    }

    // Simple home endpoint to verify API is running
    @GetMapping("/")
    public String home() {
        return "🚀 Subscription Tracker API is running! Check /api/subscriptions for data.";
    }

    private AppUser getUser(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(principal.getId())
            .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
