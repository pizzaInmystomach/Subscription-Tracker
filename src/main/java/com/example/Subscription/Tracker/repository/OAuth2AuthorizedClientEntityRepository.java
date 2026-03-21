package com.example.Subscription.Tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Subscription.Tracker.model.OAuth2AuthorizedClientEntity;
import com.example.Subscription.Tracker.model.OAuth2AuthorizedClientKey;

public interface OAuth2AuthorizedClientEntityRepository
    extends JpaRepository<OAuth2AuthorizedClientEntity, OAuth2AuthorizedClientKey> {}
