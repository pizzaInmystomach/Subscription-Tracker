package com.example.Subscription.Tracker.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2AuthorizedClientKey implements Serializable {
    private String clientRegistrationId;
    private String principalName;
}
