package com.generalservicesportal.joborder.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MicrosoftAuthService {

    @Value("${azure.activedirectory.client-id}")
    private String clientId;

    @Value("${azure.activedirectory.tenant-id}")
    private String tenantId;

    public String validateTokenAndGetEmail(String accessToken) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        // Use the access token to call the Microsoft Graph API
        String graphUrl = "https://graph.microsoft.com/v1.0/me";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<String> response = restTemplate.exchange(graphUrl, HttpMethod.GET, entity, String.class);

        // Parse the response to get the email
        JsonNode userInfo = new ObjectMapper().readTree(response.getBody());
        JsonNode emailNode = userInfo.get("mail");
        if (emailNode == null) {
            throw new RuntimeException("Email not found in user info: " + response.getBody());
        }

        return emailNode.asText();
    }
}