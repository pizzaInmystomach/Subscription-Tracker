package com.example.Subscription.Tracker.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;

import com.example.Subscription.Tracker.model.GmailAccount;
import com.example.Subscription.Tracker.model.GmailMessageLog;
import com.example.Subscription.Tracker.model.GmailMessageLogKey;
import com.example.Subscription.Tracker.model.Subscription;
import com.example.Subscription.Tracker.repository.GmailAccountRepository;
import com.example.Subscription.Tracker.repository.GmailMessageLogRepository;
import com.example.Subscription.Tracker.repository.SubscriptionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GmailScanService {

    private static final String REGISTRATION_ID = "google";

    private final GmailAccountRepository gmailAccountRepository;
    private final GmailMessageLogRepository gmailMessageLogRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final GmailApiClient gmailApiClient;

    public GmailScanService(
        GmailAccountRepository gmailAccountRepository,
        GmailMessageLogRepository gmailMessageLogRepository,
        SubscriptionRepository subscriptionRepository,
        OAuth2AuthorizedClientManager authorizedClientManager,
        GmailApiClient gmailApiClient
    ) {
        this.gmailAccountRepository = gmailAccountRepository;
        this.gmailMessageLogRepository = gmailMessageLogRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.authorizedClientManager = authorizedClientManager;
        this.gmailApiClient = gmailApiClient;
    }

    public void scanAllConnectedAccounts() {
        List<GmailAccount> accounts = gmailAccountRepository.findAll();
        for (GmailAccount account : accounts) {
            try {
                scanAccount(account);
            } catch (Exception ex) {
                log.warn("Gmail scan failed for {}: {}", account.getEmail(), ex.getMessage());
            }
        }
    }

    private void scanAccount(GmailAccount account) {
        if (account.getPrincipalName() == null || account.getPrincipalName().isBlank()) {
            log.warn("Missing principalName for {}", account.getEmail());
            return;
        }
        Authentication principal = new UsernamePasswordAuthenticationToken(account.getPrincipalName(), "N/A");
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
            .withClientRegistrationId(REGISTRATION_ID)
            .principal(principal)
            .build();
        OAuth2AuthorizedClient client = authorizedClientManager.authorize(request);
        if (client == null || client.getAccessToken() == null) {
            log.warn("No OAuth2 client available for {}", account.getEmail());
            return;
        }

        String query = buildQuery();
        List<Map<String, Object>> messages = gmailApiClient.listMessages(
            client.getAccessToken().getTokenValue(),
            query
        );

        if (messages.isEmpty()) {
            account.setLastScanAt(LocalDateTime.now());
            gmailAccountRepository.save(account);
            return;
        }

        List<Subscription> subscriptions = subscriptionRepository.findByEmailIgnoreCase(account.getEmail());

        for (Map<String, Object> message : messages) {
            String messageId = String.valueOf(message.get("id"));
            if (gmailMessageLogRepository.existsById(new GmailMessageLogKey(
                account.getEmail(), messageId))) {
                continue;
            }

            Map<String, Object> detail = gmailApiClient.getMessageMetadata(
                client.getAccessToken().getTokenValue(),
                messageId
            );
            String subject = extractHeader(detail, "Subject");
            String snippet = Optional.ofNullable(detail.get("snippet")).map(Object::toString).orElse("");

            String status = detectStatus(subject, snippet);
            if (status != null) {
                updateSubscriptions(subscriptions, subject, snippet, status);
            }

            gmailMessageLogRepository.save(new GmailMessageLog(
                account.getEmail(),
                messageId,
                LocalDateTime.now(),
                status == null ? "UNKNOWN" : status
            ));
        }

        account.setLastScanAt(LocalDateTime.now());
        gmailAccountRepository.save(account);
    }

    private String buildQuery() {
        return "newer_than:2d (subject:(cancel cancelled cancellation) OR \"payment failed\" OR \"renewal failed\" OR \"card declined\")";
    }

    private void updateSubscriptions(
        List<Subscription> subscriptions,
        String subject,
        String snippet,
        String status
    ) {
        String haystack = (subject + " " + snippet).toLowerCase(Locale.ROOT);
        List<Subscription> updated = new ArrayList<>();

        for (Subscription sub : subscriptions) {
            if (sub.getName() == null || sub.getName().isBlank()) {
                continue;
            }
            if (haystack.contains(sub.getName().toLowerCase(Locale.ROOT))) {
                sub.setStatus(status);
                sub.setLastStatusNote(subject == null || subject.isBlank() ? snippet : subject);
                sub.setLastStatusAt(LocalDate.now());
                updated.add(sub);
            }
        }

        if (!updated.isEmpty()) {
            subscriptionRepository.saveAll(updated);
        }
    }

    private String detectStatus(String subject, String snippet) {
        String text = (subject + " " + snippet).toLowerCase(Locale.ROOT);
        if (text.contains("payment failed") || text.contains("renewal failed") || text.contains("card declined")) {
            return "PAYMENT_FAILED";
        }
        if (text.contains("cancel") || text.contains("cancelled") || text.contains("cancellation")) {
            return "CANCELED";
        }
        return null;
    }

    private String extractHeader(Map<String, Object> message, String headerName) {
        if (message == null) return "";
        Object payloadObj = message.get("payload");
        if (!(payloadObj instanceof Map)) return "";
        Map payload = (Map) payloadObj;
        Object headersObj = payload.get("headers");
        if (!(headersObj instanceof List)) return "";
        List<Map<String, Object>> headers = (List<Map<String, Object>>) headersObj;
        for (Map<String, Object> header : headers) {
            if (headerName.equalsIgnoreCase(String.valueOf(header.get("name")))) {
                return String.valueOf(header.get("value"));
            }
        }
        return "";
    }
}
