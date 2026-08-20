package com.danny.ewf_service.controller;


import com.danny.ewf_service.configuration.AmazonProperties;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/oauth")
public class OAuthController {

    @Autowired
    private final AmazonProperties amazonProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    public OAuthController(AmazonProperties amazonProperties) {
        this.amazonProperties = amazonProperties;
    }

    // Step 1: Kick off Amazon authorization
    @GetMapping("/login")
    public ResponseEntity<Void> login(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute("amazon_oauth_state", state);

        String amazonAuthUrl = UriComponentsBuilder
                .fromHttpUrl("https://sellercentral.amazon.com/apps/authorize/consent")
                .queryParam("application_id", amazonProperties.getClientId())
                .queryParam("state", state)
                .queryParam("redirect_uri", amazonProperties.getRedirectUri())
                .toUriString();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(amazonAuthUrl))
                .build();
    }

    // Step 2: Amazon redirects back here with the auth code
    @GetMapping("/callback")
    public ResponseEntity<String> callback(
            @RequestParam("spapi_oauth_code") String code,
            @RequestParam("state") String state,
            @RequestParam("selling_partner_id") String sellingPartnerId,
            HttpSession session) {

        String expectedState = (String) session.getAttribute("amazon_oauth_state");
        if (expectedState == null || !expectedState.equals(state)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid state parameter");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", amazonProperties.getClientId());
        body.add("client_secret", amazonProperties.getClientSecret());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                "https://api.amazon.com/auth/o2/token", request, Map.class);

        String refreshToken = (String) tokenResponse.getBody().get("refresh_token");

        // TODO: persist refreshToken + sellingPartnerId securely (encrypted column, secrets manager, etc.)

        return ResponseEntity.ok("Amazon authorization successful for seller: " + sellingPartnerId);
    }

}
