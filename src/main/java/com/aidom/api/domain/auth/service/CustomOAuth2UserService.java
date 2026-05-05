package com.aidom.api.domain.auth.service;

import java.util.Map;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User user = delegate.loadUser(userRequest);
    Map<String, Object> attrs = user.getAttributes();
    String userNameAttributeName =
        userRequest
            .getClientRegistration()
            .getProviderDetails()
            .getUserInfoEndpoint()
            .getUserNameAttributeName();
    if (userNameAttributeName == null || userNameAttributeName.isBlank()) {
      throw new IllegalStateException(
          "OAuth2 user-name-attribute is not configured for registration: "
              + userRequest.getClientRegistration().getRegistrationId());
    }
    return new DefaultOAuth2User(user.getAuthorities(), attrs, userNameAttributeName);
  }
}
