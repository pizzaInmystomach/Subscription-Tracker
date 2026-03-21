package com.example.Subscription.Tracker.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GmailAccount {
    @Id
    private String email;

    private String principalName;
    private LocalDateTime connectedAt;
    private LocalDateTime lastScanAt;
}
