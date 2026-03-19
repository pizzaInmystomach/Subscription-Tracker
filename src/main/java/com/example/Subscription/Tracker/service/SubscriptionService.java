package com.example.Subscription.Tracker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Subscription.Tracker.model.Subscription;
import com.example.Subscription.Tracker.repository.SubscriptionRepository;

@Service
public class SubscriptionService {
    @Autowired
    private SubscriptionRepository repository;

    public List<Subscription> getAll() { return repository.findAll(); }
    public Subscription create(Subscription sub) { return repository.save(sub); }
}