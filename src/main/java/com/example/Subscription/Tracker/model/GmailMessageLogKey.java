package com.example.Subscription.Tracker.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GmailMessageLogKey implements Serializable {
    private String email;
    private String messageId;
}
