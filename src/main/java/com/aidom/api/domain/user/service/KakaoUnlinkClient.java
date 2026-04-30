package com.aidom.api.domain.user.service;

import com.aidom.api.global.config.AppAuthProperties;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KakaoUnlinkClient {

  private final RestClient restClient;
  private final AppAuthProperties authProperties;

  public KakaoUnlinkClient(RestClient.Builder restClientBuilder, AppAuthProperties authProperties) {
    this.restClient = restClientBuilder.build();
    this.authProperties = authProperties;
  }

  public void unlink(Long kakaoUserId) {
    String adminKey = authProperties.getKakao().getAdminKey();
    if (adminKey == null || adminKey.isBlank()) {
      throw new CustomException(ErrorCode.KAKAO_UNLINK_FAILED);
    }

    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("target_id_type", "user_id");
    form.add("target_id", String.valueOf(kakaoUserId));

    try {
      restClient
          .post()
          .uri(authProperties.getKakao().getUnlinkUri())
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .header("Authorization", "KakaoAK " + adminKey.trim())
          .body(form)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      throw new CustomException(ErrorCode.KAKAO_UNLINK_FAILED);
    }
  }
}
