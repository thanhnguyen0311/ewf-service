package com.danny.ewf_service.service.amz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.DecryptionFailureException;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private static final String SECRET_NAME = "spapi/lwa-credentials";
    private static final Region REGION = Region.of("us-east-2");
    private static final String LWA_TOKEN_URL = "https://api.amazon.com/auth/o2/token";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Created lazily so a setup failure shows a clear error instead of
    // ExceptionInInitializerError / "Could not initialize class TokenService".
    private static SecretsManagerClient secretsClient;

    private static String cachedToken;
    private static Instant expiresAt = Instant.EPOCH;

    private TokenService() {
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /** Returns a valid LWA access token, refreshing 60s before expiry. */
    public static synchronized String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(expiresAt.minusSeconds(60))) {
            log.debug("Using cached LWA token (expires at {})", expiresAt);
            return cachedToken;
        }

        log.info("Requesting new LWA access token...");
        JsonNode creds = getSecret();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", requireText(creds, "refresh_token"));
        params.put("client_id", requireText(creds, "lwa_client_id"));
        params.put("client_secret", requireText(creds, "lwa_client_secret"));

        HttpResponse<String> response = postToLwa(params);
        JsonNode body = parseJson(response.body(), "LWA response");

        if (response.statusCode() != 200) {
            String error = body.path("error").asText("unknown_error");
            String description = body.path("error_description").asText(response.body());
            log.error("LWA token request failed: HTTP {} | error={} | description={}",
                    response.statusCode(), error, description);
            throw new IllegalStateException("LWA token request failed (" + response.statusCode()
                                            + "): " + error + " - " + description + hintForLwaError(error));
        }

        String token = body.path("access_token").asText(null);
        if (token == null || token.isBlank()) {
            log.error("LWA response has no access_token. Body: {}", response.body());
            throw new IllegalStateException("LWA response has no access_token");
        }

        cachedToken = token;
        expiresAt = Instant.now().plusSeconds(body.path("expires_in").asLong(3600));
        log.info("Got LWA access token {} (expires at {})", mask(token), expiresAt);
        return cachedToken;
    }

    /** Forces a fresh token on the next call (e.g. after SP-API returns 401/403). */
    public static synchronized void invalidateToken() {
        cachedToken = null;
        expiresAt = Instant.EPOCH;
        log.info("LWA token cache cleared");
    }

    // ------------------------------------------------------------------
    // Secrets Manager
    // ------------------------------------------------------------------

    private static SecretsManagerClient secrets() {
        if (secretsClient == null) {
            try {
                secretsClient = SecretsManagerClient.builder()
                        .region(REGION)
                        .httpClient(UrlConnectionHttpClient.create())
                        .build();
            } catch (Throwable t) {
                log.error("Failed to create SecretsManagerClient: {}", t.toString(), t);
                throw new IllegalStateException("Cannot create SecretsManagerClient: " + t, t);
            }
        }
        return secretsClient;
    }

    /** Reads the secret from Secrets Manager (KMS decrypt happens automatically). */
    private static JsonNode getSecret() {
        try {
            GetSecretValueResponse response = secrets().getSecretValue(
                    GetSecretValueRequest.builder().secretId(SECRET_NAME).build());

            String secretString = response.secretString();
            if (secretString == null || secretString.isBlank()) {
                throw new IllegalStateException("Secret '" + SECRET_NAME
                                                + "' has no SecretString (was it stored as binary?)");
            }
            JsonNode node = parseJson(secretString, "secret '" + SECRET_NAME + "'");
            log.debug("Loaded secret '{}' with keys {}", SECRET_NAME, fieldNames(node));
            return node;

        } catch (ResourceNotFoundException e) {
            log.error("Secret '{}' not found in region {}. Check the name and region.", SECRET_NAME, REGION);
            throw new IllegalStateException("Secret not found: " + SECRET_NAME + " in " + REGION, e);

        } catch (DecryptionFailureException e) {
            log.error("KMS could not decrypt secret '{}'. Check kms:Decrypt permission and the key policy. {}",
                    SECRET_NAME, awsError(e));
            throw new IllegalStateException("KMS decryption failed for " + SECRET_NAME, e);

        } catch (SecretsManagerException e) {
            // Covers AccessDeniedException and other service-side errors
            log.error("Secrets Manager error for '{}': {}", SECRET_NAME, awsError(e));
            throw new IllegalStateException("Secrets Manager error: " + awsError(e), e);

        } catch (SdkClientException e) {
            // Usually: no AWS credentials found locally, or a network problem
            log.error("AWS client error (credentials or network?): {}", e.getMessage());
            throw new IllegalStateException("AWS client error: " + e.getMessage()
                                            + " | Hint: run 'aws configure' or set AWS_PROFILE / AWS_ACCESS_KEY_ID", e);
        }
    }

    // ------------------------------------------------------------------
    // LWA HTTP call
    // ------------------------------------------------------------------

    private static HttpResponse<String> postToLwa(Map<String, String> params) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LWA_TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(formEncode(params)))
                .build();
        try {
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("LWA responded with HTTP {}", response.statusCode());
            return response;
        } catch (HttpTimeoutException e) {
            log.error("LWA token request timed out: {}", e.getMessage());
            throw new IllegalStateException("LWA token request timed out", e);
        } catch (IOException e) {
            log.error("Network error calling LWA: {}", e.toString());
            throw new IllegalStateException("Network error calling LWA: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while calling LWA", e);
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static String requireText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            List<String> keys = fieldNames(node);
            log.error("Secret '{}' is missing key '{}'. Available keys: {}", SECRET_NAME, field, keys);
            throw new IllegalStateException("Secret '" + SECRET_NAME + "' is missing key '"
                                            + field + "'. Available keys: " + keys);
        }
        return value.asText().trim();
    }

    private static JsonNode parseJson(String text, String what) {
        try {
            return MAPPER.readTree(text == null ? "" : text);
        } catch (IOException e) {
            log.error("Could not parse {} as JSON: {}", what, e.getMessage());
            throw new IllegalStateException("Invalid JSON in " + what, e);
        }
    }

    private static List<String> fieldNames(JsonNode node) {
        List<String> names = new ArrayList<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }

    private static String awsError(SecretsManagerException e) {
        String code = e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "?";
        String msg = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
        return "[" + e.statusCode() + " " + code + "] " + msg;
    }

    private static String hintForLwaError(String error) {
        return switch (error) {
            case "invalid_grant" -> " | Hint: refresh_token is wrong, expired, or the app was de-authorized.";
            case "invalid_client" -> " | Hint: client_id or client_secret is wrong.";
            case "unauthorized_client" -> " | Hint: this client is not allowed to use this grant type.";
            default -> "";
        };
    }

    private static String mask(String token) {
        return token.length() <= 10 ? "****" : token.substring(0, 10) + "...";
    }

    private static String formEncode(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                          + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }
}