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

    public Subscription updateForUser(AppUser user, Long id, Subscription incoming) {
        Subscription existing = repository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Subscription not found"));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Subscription not found");
        }

        if (incoming.getName() != null) existing.setName(incoming.getName());
        if (incoming.getPrice() != null) existing.setPrice(incoming.getPrice());
        if (incoming.getCurrency() != null) existing.setCurrency(incoming.getCurrency());
        if (incoming.getNextBillingDate() != null) existing.setNextBillingDate(incoming.getNextBillingDate());
        if (incoming.getPeriod() != null) existing.setPeriod(incoming.getPeriod());
        if (incoming.getNoticeDays() != null) existing.setNoticeDays(incoming.getNoticeDays());
        if (incoming.getStatus() != null) existing.setStatus(incoming.getStatus());
        if (incoming.getLastStatusNote() != null) existing.setLastStatusNote(incoming.getLastStatusNote());
        if (incoming.getLastStatusAt() != null) existing.setLastStatusAt(incoming.getLastStatusAt());

        existing.setEmail(user.getEmail());
        if (existing.getStatus() == null || existing.getStatus().isBlank()) {
            existing.setStatus("ACTIVE");
        }
        if (existing.getLastStatusAt() == null) {
            existing.setLastStatusAt(LocalDate.now());
        }
        return repository.save(existing);
    }

    public void deleteForUser(AppUser user, Long id) {
        Subscription existing = repository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Subscription not found"));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Subscription not found");
        }
        repository.delete(existing);
    }
}
