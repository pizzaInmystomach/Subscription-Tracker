package com.example.Subscription.Tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Subscription.Tracker.model.GmailMessageLog;
import com.example.Subscription.Tracker.model.GmailMessageLogKey;

public interface GmailMessageLogRepository extends JpaRepository<GmailMessageLog, GmailMessageLogKey> {}
