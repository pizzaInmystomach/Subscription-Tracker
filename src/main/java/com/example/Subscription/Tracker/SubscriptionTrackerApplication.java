package com.example.Subscription.Tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.Subscription.Tracker.config.ImapProperties;
import com.example.Subscription.Tracker.config.MailDetectionProperties;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties({MailDetectionProperties.class, ImapProperties.class})
public class SubscriptionTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubscriptionTrackerApplication.class, args);
	}

}
