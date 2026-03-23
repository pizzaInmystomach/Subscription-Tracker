package com.example.Subscription.Tracker.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.stereotype.Service;

import com.example.Subscription.Tracker.config.ImapProperties;
import com.example.Subscription.Tracker.config.MailDetectionProperties;
import com.example.Subscription.Tracker.model.MailMessageLog;
import com.example.Subscription.Tracker.model.MailMessageLogKey;
import com.example.Subscription.Tracker.model.Subscription;
import com.example.Subscription.Tracker.repository.MailMessageLogRepository;
import com.example.Subscription.Tracker.repository.SubscriptionRepository;

import jakarta.mail.BodyPart;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.ReceivedDateTerm;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ImapScanService {

    private final ImapProperties imapProperties;
    private final MailDetectionProperties detectionProperties;
    private final MailMessageLogRepository mailMessageLogRepository;
    private final SubscriptionRepository subscriptionRepository;

    public ImapScanService(
        ImapProperties imapProperties,
        MailDetectionProperties detectionProperties,
        MailMessageLogRepository mailMessageLogRepository,
        SubscriptionRepository subscriptionRepository
    ) {
        this.imapProperties = imapProperties;
        this.detectionProperties = detectionProperties;
        this.mailMessageLogRepository = mailMessageLogRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public void scanInbox() {
        if (imapProperties.getHost() == null || imapProperties.getHost().isBlank()) {
            log.warn("IMAP host is not configured.");
            return;
        }
        if (imapProperties.getUsername() == null || imapProperties.getUsername().isBlank()) {
            log.warn("IMAP username is not configured.");
            return;
        }
        if (imapProperties.getPassword() == null || imapProperties.getPassword().isBlank()) {
            log.warn("IMAP password is not configured.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.store.protocol", imapProperties.isSsl() ? "imaps" : "imap");
        props.put("mail.imap.ssl.enable", Boolean.toString(imapProperties.isSsl()));

        Session session = Session.getInstance(props);
        try (Store store = session.getStore()) {
            store.connect(
                imapProperties.getHost(),
                imapProperties.getPort(),
                imapProperties.getUsername(),
                imapProperties.getPassword()
            );

            try (Folder folder = store.getFolder(imapProperties.getFolder())) {
                folder.open(Folder.READ_ONLY);

                Date sinceDate = Date.from(
                    LocalDate.now()
                        .minusDays(detectionProperties.getLookbackDays())
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                );
                Message[] messages = folder.search(new ReceivedDateTerm(ComparisonTerm.GE, sinceDate));

                int start = Math.max(0, messages.length - detectionProperties.getMaxMessages());
                for (int i = messages.length - 1; i >= start; i--) {
                    processMessage(messages[i]);
                }
            }
        } catch (Exception ex) {
            log.warn("IMAP scan failed: {}", ex.getMessage());
        }
    }

    private void processMessage(Message message) throws Exception {
        String messageId = extractHeader(message, "Message-ID");
        if (messageId == null || messageId.isBlank()) {
            messageId = fallbackMessageId(message);
        }

        String email = imapProperties.getUsername();
        if (mailMessageLogRepository.existsById(new MailMessageLogKey(email, messageId))) {
            return;
        }

        String subject = safeString(message.getSubject());
        String from = extractHeader(message, "From");
        String body = extractText(message);

        String status = detectStatus(subject, body, from);
        if (status != null) {
            updateSubscriptions(email, subject, body, status);
        }

        mailMessageLogRepository.save(new MailMessageLog(
            email,
            messageId,
            LocalDateTime.now(),
            status == null ? "UNKNOWN" : status
        ));
    }

    private void updateSubscriptions(String email, String subject, String body, String status) {
        List<Subscription> subscriptions = subscriptionRepository.findByEmailIgnoreCase(email);
        String haystack = (subject + " " + body).toLowerCase(Locale.ROOT);

        for (Subscription sub : subscriptions) {
            if (sub.getName() == null || sub.getName().isBlank()) continue;
            if (haystack.contains(sub.getName().toLowerCase(Locale.ROOT))) {
                sub.setStatus(status);
                sub.setLastStatusNote(subject.isBlank() ? body : subject);
                sub.setLastStatusAt(LocalDate.now());
            }
        }
        subscriptionRepository.saveAll(subscriptions);
    }

    private String detectStatus(String subject, String body, String from) {
        String subjectText = safeString(subject).toLowerCase(Locale.ROOT);
        String bodyText = safeString(body).toLowerCase(Locale.ROOT);
        String fromText = safeString(from).toLowerCase(Locale.ROOT);

        int cancelScore = score(subjectText, bodyText, fromText, detectionProperties.getCancelKeywords());
        int failScore = score(subjectText, bodyText, fromText, detectionProperties.getFailKeywords());

        if (failScore >= detectionProperties.getFailThreshold()
            && failScore >= cancelScore) {
            return "PAYMENT_FAILED";
        }
        if (cancelScore >= detectionProperties.getCancelThreshold()) {
            return "CANCELED";
        }
        return null;
    }

    private int score(String subject, String body, String from, String keywordConfig) {
        int total = 0;
        HashMap<String, Integer> keywordWeights = parseWeights(keywordConfig);

        for (Entry<String, Integer> entry : keywordWeights.entrySet()) {
            String phrase = entry.getKey();
            int weight = entry.getValue();
            if (subject.contains(phrase)) {
                total += weight + detectionProperties.getSubjectBoost();
            }
            if (body.contains(phrase)) {
                total += weight;
            }
            if (from.contains(phrase)) {
                total += weight;
            }
        }

        for (String domain : detectionProperties.getTrustedSenderDomains()) {
            if (!domain.isBlank() && from.contains(domain.toLowerCase(Locale.ROOT))) {
                total += detectionProperties.getSenderBoost();
            }
        }

        return total;
    }

    private HashMap<String, Integer> parseWeights(String config) {
        HashMap<String, Integer> weights = new HashMap<>();
        if (config == null || config.isBlank()) return weights;
        String[] entries = config.split(",");
        for (String entry : entries) {
            String trimmed = entry.trim();
            if (trimmed.isBlank()) continue;
            int idx = trimmed.lastIndexOf(':');
            if (idx <= 0) continue;
            String key = trimmed.substring(0, idx).trim().toLowerCase(Locale.ROOT);
            String val = trimmed.substring(idx + 1).trim();
            try {
                weights.put(key, Integer.parseInt(val));
            } catch (NumberFormatException ignored) {
            }
        }
        return weights;
    }

    private String extractText(Message message) {
        try {
            Object content = message.getContent();
            if (content instanceof String) {
                return (String) content;
            }
            if (content instanceof Multipart multipart) {
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart part = multipart.getBodyPart(i);
                    if (part.isMimeType("text/plain")) {
                        return readStream(part.getInputStream());
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private String readStream(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(' ');
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    private String extractHeader(Message message, String headerName) {
        try {
            String[] headers = message.getHeader(headerName);
            if (headers != null && headers.length > 0) {
                return headers[0];
            }
        } catch (MessagingException ignored) {
        }
        return "";
    }

    private String fallbackMessageId(Message message) {
        try {
            Date sent = message.getSentDate();
            return (sent == null ? System.currentTimeMillis() : sent.getTime()) + ":" + safeString(message.getSubject());
        } catch (MessagingException ex) {
            return String.valueOf(System.currentTimeMillis());
        }
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }
}
