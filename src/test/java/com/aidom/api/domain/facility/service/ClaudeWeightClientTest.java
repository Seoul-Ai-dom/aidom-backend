package com.aidom.api.domain.facility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.aidom.api.domain.facility.dto.ScoringWeights;
import com.aidom.api.domain.facility.dto.UserRecommendationContext;
import com.aidom.api.global.config.ClaudeProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class ClaudeWeightClientTest {

  @Mock private RestClient.Builder restClientBuilder;
  @Mock private RestClient restClient;
  @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
  @Mock private RestClient.RequestBodySpec requestBodySpec;
  @Mock private RestClient.ResponseSpec responseSpec;

  private ClaudeProperties properties;
  private ObjectMapper objectMapper;
  private ClaudeWeightClient client;

  @BeforeEach
  void setUp() {
    properties = new ClaudeProperties();
    objectMapper = new ObjectMapper();

    given(restClientBuilder.requestFactory(any())).willReturn(restClientBuilder);
    given(restClientBuilder.build()).willReturn(restClient);

    client = new ClaudeWeightClient(restClientBuilder, properties, objectMapper, Runnable::run);
  }

  @Test
  @DisplayName("apiKey가 빈 문자열이면 DEFAULT를 반환하고 RestClient를 호출하지 않는다")
  void apiKeyBlank_returnsDefault() {
    properties.setApiKey("");

    ScoringWeights result = client.calculateWeights(createContext());

    assertThat(result).isEqualTo(ScoringWeights.DEFAULT);
    verify(restClient, never()).post();
  }

  @Test
  @DisplayName("apiKey가 null이면 DEFAULT를 반환하고 RestClient를 호출하지 않는다")
  void apiKeyNull_returnsDefault() {
    properties.setApiKey(null);

    ScoringWeights result = client.calculateWeights(createContext());

    assertThat(result).isEqualTo(ScoringWeights.DEFAULT);
    verify(restClient, never()).post();
  }

  @Test
  @DisplayName("정상 Claude JSON 응답이면 비동기 fetch 후 캐시되어 다음 호출에서 파싱된 가중치를 반환한다")
  void validResponse_returnsParsedWeights() {
    properties.setApiKey("test-api-key");
    String response =
        claudeResponse(
            "{\"ratingFactor\":2.0,\"districtMatchWeight\":5.0,"
                + "\"serviceTypeWeight\":4.0,\"freeWeight\":3.0,\"distanceDecay\":0.8}");
    stubApiCall(response);

    UserRecommendationContext context = createContext();

    // 1차: cache miss → 비동기 fetch → DEFAULT 반환
    ScoringWeights first = client.calculateWeights(context);
    assertThat(first).isEqualTo(ScoringWeights.DEFAULT);

    // 2차: cache hit → 파싱된 가중치 반환
    ScoringWeights second = client.calculateWeights(context);
    assertThat(second.ratingFactor()).isEqualTo(2.0);
    assertThat(second.districtMatchWeight()).isEqualTo(5.0);
    assertThat(second.serviceTypeWeight()).isEqualTo(4.0);
    assertThat(second.freeWeight()).isEqualTo(3.0);
    assertThat(second.distanceDecay()).isEqualTo(0.8);
  }

  @Test
  @DisplayName("범위 초과 가중치가 반환되면 DEFAULT를 반환한다")
  void outOfRangeWeights_returnsDefault() {
    properties.setApiKey("test-api-key");
    String response =
        claudeResponse(
            "{\"ratingFactor\":99.0,\"districtMatchWeight\":5.0,"
                + "\"serviceTypeWeight\":4.0,\"freeWeight\":3.0,\"distanceDecay\":0.8}");
    stubApiCall(response);

    ScoringWeights result = client.calculateWeights(createContext());

    assertThat(result).isEqualTo(ScoringWeights.DEFAULT);
  }

  @Test
  @DisplayName("content 배열이 비어있으면 DEFAULT를 반환한다")
  void emptyContentArray_returnsDefault() {
    properties.setApiKey("test-api-key");
    stubApiCall("{\"content\":[]}");

    ScoringWeights result = client.calculateWeights(createContext());

    assertThat(result).isEqualTo(ScoringWeights.DEFAULT);
  }

  @Test
  @DisplayName("content.text가 유효하지 않은 JSON이면 DEFAULT를 반환한다")
  void malformedJson_returnsDefault() {
    properties.setApiKey("test-api-key");
    stubApiCall(claudeResponse("not-a-json"));

    ScoringWeights result = client.calculateWeights(createContext());

    assertThat(result).isEqualTo(ScoringWeights.DEFAULT);
  }

  @Test
  @DisplayName("RestClient가 예외를 던지면 DEFAULT를 반환한다")
  void restClientException_returnsDefault() {
    properties.setApiKey("test-api-key");
    stubApiCallException(new RestClientException("Connection refused"));

    ScoringWeights result = client.calculateWeights(createContext());

    assertThat(result).isEqualTo(ScoringWeights.DEFAULT);
  }

  @Test
  @DisplayName("RestClient가 null body를 반환하면 DEFAULT를 반환한다")
  void nullResponseBody_returnsDefault() {
    properties.setApiKey("test-api-key");
    stubApiCall(null);

    ScoringWeights result = client.calculateWeights(createContext());

    assertThat(result).isEqualTo(ScoringWeights.DEFAULT);
  }

  @Test
  @DisplayName("동일 context로 반복 호출하면 API는 한 번만 호출되고 캐시된 결과를 반환한다")
  void cachedResult_returnsWithoutApiCall() {
    properties.setApiKey("test-api-key");
    String response =
        claudeResponse(
            "{\"ratingFactor\":2.0,\"districtMatchWeight\":5.0,"
                + "\"serviceTypeWeight\":4.0,\"freeWeight\":3.0,\"distanceDecay\":0.8}");
    stubApiCall(response);

    UserRecommendationContext context = createContext();

    // 1차: cache miss → 비동기 fetch → DEFAULT
    client.calculateWeights(context);

    // 2차, 3차: cache hit → API 추가 호출 없이 캐시 반환
    ScoringWeights second = client.calculateWeights(context);
    ScoringWeights third = client.calculateWeights(context);

    assertThat(second).isEqualTo(third);
    assertThat(second.ratingFactor()).isEqualTo(2.0);
    verify(restClient, times(1)).post();
  }

  @Test
  @DisplayName("DEFAULT 결과는 캐시되지 않아 다음 호출에서 커스텀 가중치를 반환할 수 있다")
  void defaultResult_isNotCached() {
    properties.setApiKey("test-api-key");

    String response =
        claudeResponse(
            "{\"ratingFactor\":2.0,\"districtMatchWeight\":5.0,"
                + "\"serviceTypeWeight\":4.0,\"freeWeight\":3.0,\"distanceDecay\":0.8}");

    // retrieve()를 연속 스텁: 1차 예외, 2차 정상
    given(restClient.post()).willReturn(requestBodyUriSpec);
    given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
    given(requestBodySpec.contentType(any(MediaType.class))).willReturn(requestBodySpec);
    given(requestBodySpec.header(anyString(), any(String[].class))).willReturn(requestBodySpec);
    given(requestBodySpec.body((Object) any())).willReturn(requestBodySpec);
    given(requestBodySpec.retrieve())
        .willThrow(new RestClientException("Connection refused"))
        .willReturn(responseSpec);
    given(responseSpec.body(String.class)).willReturn(response);

    UserRecommendationContext context = createContext();

    // 1차: 비동기 fetch 실패 → DEFAULT, 캐시 안 됨
    ScoringWeights first = client.calculateWeights(context);
    assertThat(first).isEqualTo(ScoringWeights.DEFAULT);

    // 2차: 비동기 fetch 성공 → DEFAULT 반환, 캐시 적재
    ScoringWeights second = client.calculateWeights(context);
    assertThat(second).isEqualTo(ScoringWeights.DEFAULT);

    // 3차: cache hit → 커스텀 가중치
    ScoringWeights third = client.calculateWeights(context);
    assertThat(third.ratingFactor()).isEqualTo(2.0);
    assertThat(third).isNotEqualTo(ScoringWeights.DEFAULT);
    verify(restClient, times(2)).post();
  }

  private void stubApiCall(String responseBody) {
    given(restClient.post()).willReturn(requestBodyUriSpec);
    given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
    given(requestBodySpec.contentType(any(MediaType.class))).willReturn(requestBodySpec);
    given(requestBodySpec.header(anyString(), any(String[].class))).willReturn(requestBodySpec);
    given(requestBodySpec.body((Object) any())).willReturn(requestBodySpec);
    given(requestBodySpec.retrieve()).willReturn(responseSpec);
    given(responseSpec.body(String.class)).willReturn(responseBody);
  }

  private void stubApiCallException(RuntimeException ex) {
    given(restClient.post()).willReturn(requestBodyUriSpec);
    given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
    given(requestBodySpec.contentType(any(MediaType.class))).willReturn(requestBodySpec);
    given(requestBodySpec.header(anyString(), any(String[].class))).willReturn(requestBodySpec);
    given(requestBodySpec.body((Object) any())).willReturn(requestBodySpec);
    given(requestBodySpec.retrieve()).willThrow(ex);
  }

  private String claudeResponse(String textContent) {
    String escaped = textContent.replace("\\", "\\\\").replace("\"", "\\\"");
    return "{\"content\":[{\"type\":\"text\",\"text\":\"" + escaped + "\"}]}";
  }

  private UserRecommendationContext createContext() {
    return new UserRecommendationContext(
        5, "강남구", 3, Set.of("우리동네키움센터"), 2, true, "morning", "spring");
  }
}
