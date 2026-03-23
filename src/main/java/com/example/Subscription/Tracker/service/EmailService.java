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

    @Value("${app.mail.from:no-reply@subtracker.com}")
    private String mailFrom;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // Log mail configuration on startup for debugging purposes
    @PostConstruct
    public void logMailConfig() {
        if (mailUsername == null || mailUsername.isBlank()) {
            log.warn("Mail username is blank. Check MAILTRAP_USERNAME env var.");
        } else {
            log.info("Mail username loaded: {}", mailUsername);
        }
    }

    // Simulate sending email asynchronously
    @Async
    public void sendCancelReminder(String to, String subName, LocalDate billingDate) {
        // In a real application, you would construct a proper email message here
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject("Subscription Reminder: " + subName);
        message.setText(String.format(
            "Hello! Your subscription [%s] will be charged on %s.\nIf you no longer need this service, please remember to cancel in time to avoid unnecessary charges!",
            subName, billingDate.toString()
        ));

        mailSender.send(message);

        // Simulate delay for sending email
        log.info("Sending mails (Thread: {})", Thread.currentThread().getName());
        try { Thread.sleep(2000); } catch (InterruptedException e) {} 
    }

    @Async
    public void sendVerificationEmail(String to, String token) {
        String verifyLink = baseUrl + "/verify.html?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject("Verify your email");
        message.setText(String.format(
            "Please verify your email by clicking the link below:\n%s",
            verifyLink
        ));
        mailSender.send(message);
    }

    @Async
    public void sendAuthVerificationEmail(String to, String token) {
        String verifyLink = baseUrl + "/verify-auth.html?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject("Verify your account");
        message.setText(String.format(
            "Please verify your account by clicking the link below:\n%s",
            verifyLink
        ));
        mailSender.send(message);
    }
}
