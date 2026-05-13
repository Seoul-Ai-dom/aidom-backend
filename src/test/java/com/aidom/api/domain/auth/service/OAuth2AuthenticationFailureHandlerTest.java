package com.aidom.api.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.aidom.api.global.config.AppAuthProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;

class OAuth2AuthenticationFailureHandlerTest {

  @Test
  @DisplayName("OAuth2 로그인 실패 시 고정 에러 코드와 함께 프론트로 리다이렉트한다")
  void onAuthenticationFailure_redirectsWithFixedErrorCode() throws Exception {
    OAuth2AuthenticationFailureHandler handler = createHandler("https://aidom.kr/login/callback");
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.onAuthenticationFailure(
        request, response, new AuthenticationServiceException("provider detail"));

    assertThat(response.getStatus()).isEqualTo(302);
    assertThat(response.getRedirectedUrl())
        .isEqualTo("https://aidom.kr/login/callback?error=oauth2_login_failed");
  }

  @Test
  @DisplayName("예외 메시지가 null 이어도 추가 예외 없이 고정 에러 코드로 리다이렉트한다")
  void onAuthenticationFailure_withNullMessage_redirectsSafely() throws Exception {
    OAuth2AuthenticationFailureHandler handler =
        createHandler("https://aidom.kr/login/callback?source=oauth");
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.onAuthenticationFailure(request, response, new AuthenticationServiceException(null));

    assertThat(response.getStatus()).isEqualTo(302);
    assertThat(response.getRedirectedUrl())
        .isEqualTo("https://aidom.kr/login/callback?source=oauth&error=oauth2_login_failed");
  }

  private OAuth2AuthenticationFailureHandler createHandler(String redirectUri) {
    AppAuthProperties appAuthProperties = new AppAuthProperties();
    appAuthProperties.getOAuth2().setDefaultSuccessUri(redirectUri);
    return new OAuth2AuthenticationFailureHandler(appAuthProperties);
  }
}
