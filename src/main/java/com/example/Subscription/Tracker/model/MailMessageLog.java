package com.example.Subscription.Tracker.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(MailMessageLogKey.class)
public class MailMessageLog {
    @Id
    private String email;

    @Id
    private String messageId;

    private LocalDateTime processedAt;
    private String statusDetected;
}
