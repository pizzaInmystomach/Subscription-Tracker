package com.example.Subscription.Tracker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import com.example.Subscription.Tracker.model.AppUser;
import com.example.Subscription.Tracker.model.Subscription;
import com.example.Subscription.Tracker.repository.SubscriptionRepository;

@Service
public class SubscriptionService {
    @Autowired
    private SubscriptionRepository repository;

    public List<Subscription> getAllByUser(AppUser user) {
        return repository.findByUserId(user.getId());
    }

    public Subscription createForUser(AppUser user, Subscription sub) {
        sub.setUser(user);
        sub.setEmail(user.getEmail());
        if (sub.getStatus() == null || sub.getStatus().isBlank()) {
            sub.setStatus("ACTIVE");
        }
        if (sub.getLastStatusAt() == null) {
            sub.setLastStatusAt(LocalDate.now());
        }
        return repository.save(sub);
    }
}
