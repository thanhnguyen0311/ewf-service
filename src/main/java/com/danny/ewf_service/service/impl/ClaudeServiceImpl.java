package com.danny.ewf_service.service.impl;


import com.danny.ewf_service.configuration.WmsDatasourceProperties;
import com.danny.ewf_service.payload.request.product.ProductMetaDto;
import com.danny.ewf_service.payload.response.product.ProductMetaResponseDto;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.ClaudeService;
import com.danny.ewf_service.utils.CsvWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ClaudeServiceImpl implements ClaudeService {
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION = "2023-06-01";

    private static final String SYSTEM_PROMPT = """
            You are an e-commerce SEO expert. Given a product, write:
            - metaTitle: max 60 characters, includes the main keyword, compelling.
            - metaDescription: 140-160 characters, benefit-focused, ends with a soft call to action.
            Respond ONLY with a JSON object in this exact shape, with no markdown or extra text:
            {"metaTitle": "...", "metaDescription": "..."}
            """;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private final WmsDatasourceProperties wmsDatasourceProperties;

    @Override
    public ProductMetaResponseDto getProductMeta(ProductMetaDto productMetaDto) throws IOException, InterruptedException {
        String userPrompt = """
                Product title: %s
                SKU: %s
                Product description: %s
                """.formatted(productMetaDto.getTitle(), productMetaDto.getSku(), productMetaDto.getDescription());

        Map<String, Object> body = Map.of(
                "model", "claude-sonnet-5",
                "max_tokens", 2048,
                "system", SYSTEM_PROMPT,
                "messages", List.of(Map.of("role", "user", "content", userPrompt))
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("x-api-key", wmsDatasourceProperties.getAnthropicApiKey())
                .header("anthropic-version", API_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Claude API returned " + response.statusCode() + ": " + response.body());
        }
        // Collect all text blocks from the response
        JsonNode root = mapper.readTree(response.body());

        StringBuilder text = new StringBuilder();
        System.out.println("Claude raw text: " + text);
        System.out.println("stop_reason: " + root.path("stop_reason").asText());
        System.out.println("SKU " + productMetaDto.getSku() + " metaTitle: " + root.path("content").get(0).path("text").asText());
        for (JsonNode block : root.path("content")) {
            if ("text".equals(block.path("type").asText())) {
                text.append(block.path("text").asText());
            }
        }

        // Strip accidental ```json fences before parsing
        String clean = text.toString().replaceAll("```json|```", "").trim();
        JsonNode meta = mapper.readTree(clean);

        return new ProductMetaResponseDto(
                productMetaDto.getSku(),
                meta.path("metaTitle").asText(),
                meta.path("metaDescription").asText()
        );
    }
}
