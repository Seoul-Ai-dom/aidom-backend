package com.aidom.api.global.security.handler;

import com.aidom.api.global.jwt.JwtTokenProvider;
import com.aidom.api.global.security.AuthProperties;
import com.aidom.api.global.security.HttpCookieOAuth2AuthorRequestRepo;
import com.aidom.api.global.security.PrincipalDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final HttpCookieOAuth2AuthorRequestRepo authorizationRequestRepository;
  private final AuthProperties authProperties;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
    String accessToken = jwtTokenProvider.createAccessToken(principalDetails.getUser());
    String targetUrl = determineTargetUrl(request, accessToken, principalDetails);

    authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    clearAuthenticationAttributes(request);
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  private String determineTargetUrl(
      HttpServletRequest request, String accessToken, PrincipalDetails principalDetails) {
    String redirectUri =
        getRedirectUriFromCookie(request)
            .filter(this::isAuthorizedRedirectUri)
            .orElse(authProperties.getOauth2().getDefaultSuccessUri());

    return UriComponentsBuilder.fromUriString(redirectUri)
        .queryParam("accessToken", accessToken)
        .queryParam("onboardingRequired", principalDetails.getUser().requiresOnboarding())
        .queryParam("role", principalDetails.getUser().getRole().name())
        .build()
        .toUriString();
  }

  private Optional<String> getRedirectUriFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }

    for (Cookie cookie : cookies) {
      if (HttpCookieOAuth2AuthorRequestRepo.REDIRECT_URI_PARAM_COOKIE_NAME.equals(
          cookie.getName())) {
        return Optional.ofNullable(cookie.getValue());
      }
    }
    return Optional.empty();
  }

  private boolean isAuthorizedRedirectUri(String uri) {
    return authProperties.getOauth2().getAuthorizedRedirectUris().stream()
        .anyMatch(uri::startsWith);
  }
}
