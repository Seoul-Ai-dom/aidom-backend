package com.aidom.api.global.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class HttpCookieOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private static final String COOKIE_NAME = "oauth2_auth_request";
  private static final int COOKIE_EXPIRE_SECONDS = 180;

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    return findCookie(request).map(Cookie::getValue).map(this::deserialize).orElse(null);
  }

  @Override
  public void saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response) {
    if (authorizationRequest == null) {
      deleteCookie(response);
      return;
    }
    Cookie cookie = new Cookie(COOKIE_NAME, serialize(authorizationRequest));
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
    response.addCookie(cookie);
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
      HttpServletRequest request, HttpServletResponse response) {
    OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
    deleteCookie(response);
    return authorizationRequest;
  }

  private Optional<Cookie> findCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }
    return Arrays.stream(cookies).filter(c -> COOKIE_NAME.equals(c.getName())).findFirst();
  }

  private void deleteCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie(COOKIE_NAME, "");
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }

  private String serialize(OAuth2AuthorizationRequest request) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(request);
      return Base64.getUrlEncoder().encodeToString(bos.toByteArray());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to serialize OAuth2AuthorizationRequest", e);
    }
  }

  private OAuth2AuthorizationRequest deserialize(String value) {
    try (ObjectInputStream ois =
        new ObjectInputStream(new ByteArrayInputStream(Base64.getUrlDecoder().decode(value)))) {
      return (OAuth2AuthorizationRequest) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalStateException("Failed to deserialize OAuth2AuthorizationRequest", e);
    }
  }
}
