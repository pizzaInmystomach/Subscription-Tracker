package com.example.Subscription.Tracker.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.Subscription.Tracker.service.GmailScanService;

@Component
public class GmailScanScheduler {

    @Autowired
    private GmailScanService gmailScanService;

    @Scheduled(cron = "0 0 9 * * *", zone = "${app.timezone:Asia/Taipei}")
    public void scanGmail() {
        gmailScanService.scanAllConnectedAccounts();
    }
}
