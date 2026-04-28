package com.aidom.api.domain.auth.service;

import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.global.config.AppAuthProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final AuthService authService;
  private final AppAuthProperties appAuthProperties;

  public OAuth2AuthenticationSuccessHandler(
      AuthService authService, AppAuthProperties appAuthProperties) {
    this.authService = authService;
    this.appAuthProperties = appAuthProperties;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    OAuth2User user = (OAuth2User) authentication.getPrincipal();
    String registrationId = request.getRequestURI().contains("kakao") ? "kakao" : "google";

    Provider provider = "kakao".equalsIgnoreCase(registrationId) ? Provider.KAKAO : Provider.GOOGLE;

    OAuthProfile profile = extractProfile(provider, user.getAttributes());
    String code =
        authService.handleOAuthLogin(
            provider, profile.providerId(), profile.email(), profile.name());

    String redirectUri = appAuthProperties.getOAuth2().getDefaultSuccessUri();
    String encodedCode = URLEncoder.encode(code, StandardCharsets.UTF_8);
    getRedirectStrategy().sendRedirect(request, response, redirectUri + "?code=" + encodedCode);
  }

  private OAuthProfile extractProfile(Provider provider, Map<String, Object> attrs) {
    if (provider == Provider.KAKAO) {
      String providerId = String.valueOf(attrs.get("id"));
      String email = null;
      String name = null;

      Object kakaoAccountObj = attrs.get("kakao_account");
      if (kakaoAccountObj instanceof Map<?, ?> kakaoAccount) {
        Object emailObj = kakaoAccount.get("email");
        if (emailObj != null) {
          email = String.valueOf(emailObj);
        }
      }

      Object propertiesObj = attrs.get("properties");
      if (propertiesObj instanceof Map<?, ?> properties) {
        Object nickname = properties.get("nickname");
        if (nickname != null) {
          name = String.valueOf(nickname);
        }
      }

      return new OAuthProfile(providerId, email, name);
    }

    String providerId = String.valueOf(attrs.get("sub"));
    String email = attrs.get("email") == null ? null : String.valueOf(attrs.get("email"));
    String name = attrs.get("name") == null ? null : String.valueOf(attrs.get("name"));
    return new OAuthProfile(providerId, email, name);
  }

  private record OAuthProfile(String providerId, String email, String name) {}
}
