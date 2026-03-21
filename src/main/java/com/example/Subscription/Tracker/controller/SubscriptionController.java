package com.example.Subscription.Tracker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Subscription.Tracker.model.Subscription;
import com.example.Subscription.Tracker.service.SubscriptionService;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    @Autowired
    private SubscriptionService service;

    @GetMapping
    public List<Subscription> list() { return service.getAll(); }

    @PostMapping
    public Subscription add(@RequestBody Subscription sub) { return service.create(sub); }

    // Simple home endpoint to verify API is running
    @GetMapping("/")
    public String home() {
        return "🚀 Subscription Tracker API is running! Check /api/subscriptions for data.";
    }
}
