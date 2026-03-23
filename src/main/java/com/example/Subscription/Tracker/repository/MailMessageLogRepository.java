package com.example.Subscription.Tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Subscription.Tracker.model.MailMessageLog;
import com.example.Subscription.Tracker.model.MailMessageLogKey;

public interface MailMessageLogRepository extends JpaRepository<MailMessageLog, MailMessageLogKey> {}
