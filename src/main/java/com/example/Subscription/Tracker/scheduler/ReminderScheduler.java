package com.example.Subscription.Tracker.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.Subscription.Tracker.model.Subscription;
import com.example.Subscription.Tracker.repository.SubscriptionRepository;
import com.example.Subscription.Tracker.service.EmailService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ReminderScheduler {

    @Autowired
    private SubscriptionRepository repository;

    @Autowired
    private EmailService emailService; 

    @Scheduled(fixedRate = 60000) // for testing
    public void checkAndSendReminders() {
        log.info("Starting scanning...");
        LocalDate today = LocalDate.now();
        List<Subscription> allSubs = repository.findAll();

        for (Subscription sub : allSubs) {
            LocalDate reminderDate = sub.getNextBillingDate().minusDays(sub.getNoticeDays());

            if (reminderDate.equals(today)) {
                // 假設這裡發送給固定信箱，之後可以擴充為 sub.getUser().getEmail()
                emailService.sendCancelReminder("your-test@email.com", sub.getName(), sub.getNextBillingDate());
                log.info("Successfully send reminding mail to [{}].", sub.getName());
            }
        }
    }
}