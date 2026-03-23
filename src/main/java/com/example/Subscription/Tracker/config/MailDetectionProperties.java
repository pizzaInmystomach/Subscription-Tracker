package com.example.Subscription.Tracker.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail.detect")
public class MailDetectionProperties {
    private int lookbackDays = 2;
    private int maxMessages = 200;
    private List<String> trustedSenderDomains = List.of();
    private String cancelKeywords = "cancel:3,cancelled:3,cancellation:4,subscription cancelled:6";
    private String failKeywords = "payment failed:5,renewal failed:5,card declined:4";
    private int cancelThreshold = 5;
    private int failThreshold = 5;
    private int subjectBoost = 2;
    private int senderBoost = 2;

    public int getLookbackDays() { return lookbackDays; }
    public void setLookbackDays(int lookbackDays) { this.lookbackDays = lookbackDays; }

    public int getMaxMessages() { return maxMessages; }
    public void setMaxMessages(int maxMessages) { this.maxMessages = maxMessages; }

    public List<String> getTrustedSenderDomains() { return trustedSenderDomains; }
    public void setTrustedSenderDomains(List<String> trustedSenderDomains) { this.trustedSenderDomains = trustedSenderDomains; }

    public String getCancelKeywords() { return cancelKeywords; }
    public void setCancelKeywords(String cancelKeywords) { this.cancelKeywords = cancelKeywords; }

    public String getFailKeywords() { return failKeywords; }
    public void setFailKeywords(String failKeywords) { this.failKeywords = failKeywords; }


    public int getCancelThreshold() { return cancelThreshold; }
    public void setCancelThreshold(int cancelThreshold) { this.cancelThreshold = cancelThreshold; }

    public int getFailThreshold() { return failThreshold; }
    public void setFailThreshold(int failThreshold) { this.failThreshold = failThreshold; }

    public int getSubjectBoost() { return subjectBoost; }
    public void setSubjectBoost(int subjectBoost) { this.subjectBoost = subjectBoost; }

    public int getSenderBoost() { return senderBoost; }
    public void setSenderBoost(int senderBoost) { this.senderBoost = senderBoost; }
}
