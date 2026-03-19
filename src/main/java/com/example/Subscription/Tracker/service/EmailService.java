package com.example.Subscription.Tracker.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@EnableAsync
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @PostConstruct
    public void logMailConfig() {
        if (mailUsername == null || mailUsername.isBlank()) {
            log.warn("Mail username is blank. Check MAILTRAP_USERNAME env var.");
        } else {
            log.info("Mail username loaded: {}", mailUsername);
        }
    }

    @Async
    public void sendCancelReminder(String to, String subName, LocalDate billingDate) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@subtracker.com");
        message.setTo(to);
        message.setSubject("Subscription Reminder: " + subName);
        message.setText(String.format(
            "Hello! Your subscription [%s] will be charged on %s.\nIf you no longer need this service, please remember to cancel in time to avoid unnecessary charges!",
            subName, billingDate.toString()
        ));

        mailSender.send(message);

        log.info("Sending mails (Thread: {})", Thread.currentThread().getName());
        try { Thread.sleep(2000); } catch (InterruptedException e) {} 
    }
}
