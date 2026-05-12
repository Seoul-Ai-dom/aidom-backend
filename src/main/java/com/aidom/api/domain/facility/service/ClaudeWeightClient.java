package com.aidom.api.domain.facility.service;

import com.aidom.api.domain.facility.dto.ScoringWeights;
import com.aidom.api.domain.facility.dto.UserRecommendationContext;
import com.aidom.api.global.config.ClaudeProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.claude.enabled", havingValue = "true")
public class ClaudeWeightClient {

  private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
  private static final String ANTHROPIC_VERSION = "2023-06-01";

  private static final String SYSTEM_PROMPT =
      """
      You are a scoring weight calculator for a childcare facility recommendation system.
      Given user context, return JSON: {"ratingFactor":N,"districtMatchWeight":N,"serviceTypeWeight":N,"freeWeight":N,"distanceDecay":N}
      Rules:
      - ratingFactor: 0.1~5.0
      - districtMatchWeight: 0.0~10.0
      - serviceTypeWeight: 0.0~10.0
      - freeWeight: 0.0~10.0
      - distanceDecay: 0.1~1.0
      - More bookmarks -> stronger serviceTypeWeight
      - Young children (0-3) -> higher freeWeight
      - Has district -> boost districtMatchWeight
      - No location -> distanceDecay = 0.5
      - Respond ONLY with JSON, no explanation""";

  private final RestClient restClient;
  private final ClaudeProperties properties;
  private final ObjectMapper objectMapper;

  public ClaudeWeightClient(
      RestClient.Builder restClientBuilder,
      ClaudeProperties properties,
      ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;

    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(properties.getConnectTimeout());
    requestFactory.setReadTimeout(properties.getReadTimeout());
    this.restClient = restClientBuilder.requestFactory(requestFactory).build();
  }

  public ScoringWeights calculateWeights(UserRecommendationContext context) {
    String apiKey = properties.getApiKey();
    if (apiKey == null || apiKey.isBlank()) {
      log.debug("Claude API key not configured, using default weights");
      return ScoringWeights.DEFAULT;
    }

    try {
      String userMessage = objectMapper.writeValueAsString(context);

      String requestBody =
          objectMapper.writeValueAsString(
              new ClaudeRequest(
                  properties.getModel(),
                  256,
                  SYSTEM_PROMPT,
                  new Message[] {new Message("user", userMessage)}));

      String responseBody =
          restClient
              .post()
              .uri(ANTHROPIC_API_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .header("x-api-key", apiKey.trim())
              .header("anthropic-version", ANTHROPIC_VERSION)
              .body(requestBody)
              .retrieve()
              .body(String.class);

      return parseResponse(responseBody);
    } catch (Exception e) {
      log.warn("Claude API call failed, using default weights: {}", e.getMessage());
      return ScoringWeights.DEFAULT;
    }
  }

  private ScoringWeights parseResponse(String responseBody) {
    try {
      JsonNode root = objectMapper.readTree(responseBody);
      JsonNode content = root.path("content");
      if (!content.isArray() || content.isEmpty()) {
        log.warn("Claude response has no content, using default weights");
        return ScoringWeights.DEFAULT;
      }

      String text = content.get(0).path("text").asText();
      JsonNode weights = objectMapper.readTree(text);

      ScoringWeights result =
          new ScoringWeights(
              weights.path("ratingFactor").asDouble(),
              weights.path("districtMatchWeight").asDouble(),
              weights.path("serviceTypeWeight").asDouble(),
              weights.path("freeWeight").asDouble(),
              weights.path("distanceDecay").asDouble());

      if (!result.isValid()) {
        log.warn("Claude returned out-of-range weights, using default weights: {}", text);
        return ScoringWeights.DEFAULT;
      }

      return result;
    } catch (Exception e) {
      log.warn("Failed to parse Claude response, using default weights: {}", e.getMessage());
      return ScoringWeights.DEFAULT;
    }
  }

  private record ClaudeRequest(String model, int max_tokens, String system, Message[] messages) {}

  private record Message(String role, String content) {}
}
