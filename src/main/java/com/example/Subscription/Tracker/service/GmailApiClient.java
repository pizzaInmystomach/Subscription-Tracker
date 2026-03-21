package com.example.Subscription.Tracker.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GmailApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Map<String, Object>> listMessages(String accessToken, String query) {
        String url = UriComponentsBuilder
            .fromHttpUrl("https://gmail.googleapis.com/gmail/v1/users/me/messages")
            .queryParam("q", query)
            .build()
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class
        );

        Map body = response.getBody();
        if (body == null || !body.containsKey("messages")) {
            return List.of();
        }
        return (List<Map<String, Object>>) body.get("messages");
    }

    public Map<String, Object> getMessageMetadata(String accessToken, String messageId) {
        String url = UriComponentsBuilder
            .fromHttpUrl("https://gmail.googleapis.com/gmail/v1/users/me/messages/" + messageId)
            .queryParam("format", "metadata")
            .queryParam("metadataHeaders", "Subject")
            .queryParam("metadataHeaders", "From")
            .build()
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class
        );

        return response.getBody();
    }
}
