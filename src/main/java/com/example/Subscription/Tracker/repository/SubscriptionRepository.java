package com.example.Subscription.Tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.Subscription.Tracker.model.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
  @Query("SELECT s FROM Subscription s")
  List<Subscription> findAllSubscriptions();

  boolean existsByEmailIgnoreCaseAndNameIgnoreCase(String email, String name);

  List<Subscription> findByEmailIgnoreCase(String email);
}
