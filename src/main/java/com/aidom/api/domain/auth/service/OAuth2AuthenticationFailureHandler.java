package com.aidom.api.domain.auth.service;

import com.aidom.api.global.config.AppAuthProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  static final String ERROR_CODE = "oauth2_login_failed";

  private final AppAuthProperties appAuthProperties;

  public OAuth2AuthenticationFailureHandler(AppAuthProperties appAuthProperties) {
    this.appAuthProperties = appAuthProperties;
  }

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException, ServletException {
    log.error("OAuth2 login failed: {}", exception.getMessage(), exception);

    String redirectUri =
        UriComponentsBuilder.fromUriString(appAuthProperties.getOAuth2().getDefaultSuccessUri())
            .queryParam("error", ERROR_CODE)
            .build(true)
            .toUriString();

    getRedirectStrategy().sendRedirect(request, response, redirectUri);
  }
}
