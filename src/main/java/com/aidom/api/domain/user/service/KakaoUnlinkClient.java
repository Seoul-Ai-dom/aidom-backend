package com.aidom.api.domain.user.service;

import com.aidom.api.global.config.AppAuthProperties;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class KakaoUnlinkClient {

  private final RestClient restClient;
  private final AppAuthProperties.Kakao kakaoProperties;

  public KakaoUnlinkClient(RestClient.Builder restClientBuilder, AppAuthProperties authProperties) {
    this.kakaoProperties = authProperties.getKakao();

    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(kakaoProperties.getConnectTimeout());
    requestFactory.setReadTimeout(kakaoProperties.getReadTimeout());
    this.restClient = restClientBuilder.requestFactory(requestFactory).build();
  }

  public void unlink(Long kakaoUserId) {
    String adminKey = kakaoProperties.getAdminKey();
    if (adminKey == null || adminKey.isBlank()) {
      throw new CustomException(ErrorCode.KAKAO_UNLINK_FAILED);
    }

    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("target_id_type", "user_id");
    form.add("target_id", String.valueOf(kakaoUserId));

    try {
      restClient
          .post()
          .uri(kakaoProperties.getUnlinkUri())
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .header("Authorization", "KakaoAK " + adminKey.trim())
          .body(form)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientResponseException e) {
      if (isAlreadyUnlinked(e)) {
        return;
      }
      throw new CustomException(ErrorCode.KAKAO_UNLINK_FAILED);
    } catch (RestClientException e) {
      throw new CustomException(ErrorCode.KAKAO_UNLINK_FAILED);
    }
  }

  private boolean isAlreadyUnlinked(RestClientResponseException e) {
    if (e.getStatusCode() != HttpStatus.BAD_REQUEST) {
      return false;
    }
    String body = e.getResponseBodyAsString();
    return body != null
        && body.contains("\"code\":-101")
        && body.contains("NotRegisteredUserException");
  }
}
