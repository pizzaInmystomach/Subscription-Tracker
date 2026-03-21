package com.example.Subscription.Tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Subscription.Tracker.model.GmailAccount;

public interface GmailAccountRepository extends JpaRepository<GmailAccount, String> {}
