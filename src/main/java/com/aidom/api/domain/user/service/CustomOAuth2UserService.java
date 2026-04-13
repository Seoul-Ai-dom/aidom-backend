package com.aidom.api.domain.user.service;

import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import com.aidom.api.global.security.PrincipalDetails;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

  private final AuthService authService;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = delegate.loadUser(userRequest);
    Provider provider =
        Provider.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
    OAuth2UserInfo userInfo = extractUserInfo(provider, oAuth2User);

    if (userInfo.email() == null || userInfo.email().isBlank()) {
      throw new CustomException(ErrorCode.OAUTH2_EMAIL_NOT_PROVIDED);
    }

    User user =
        authService.socialLogin(userInfo.email(), userInfo.name(), provider, userInfo.providerId());
    log.debug(
        "OAuth2 login success. provider={}, email={}, userId={}",
        provider,
        user.getEmail(),
        user.getId());

    return new PrincipalDetails(user, oAuth2User.getAttributes());
  }

  private OAuth2UserInfo extractUserInfo(Provider provider, OAuth2User oAuth2User) {
    return switch (provider) {
      case GOOGLE ->
          new OAuth2UserInfo(
              oAuth2User.getName(),
              oAuth2User.getAttribute("email"),
              oAuth2User.getAttribute("name"));
      case KAKAO -> extractKakaoUserInfo(oAuth2User);
    };
  }

  @SuppressWarnings("unchecked")
  private OAuth2UserInfo extractKakaoUserInfo(OAuth2User oAuth2User) {
    Map<String, Object> attributes = oAuth2User.getAttributes();
    Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
    Map<String, Object> profile =
        kakaoAccount == null ? null : (Map<String, Object>) kakaoAccount.get("profile");

    String providerId = String.valueOf(attributes.get("id"));
    String email = kakaoAccount == null ? null : (String) kakaoAccount.get("email");
    String nickname = profile == null ? null : (String) profile.get("nickname");
    return new OAuth2UserInfo(providerId, email, nickname);
  }

  private record OAuth2UserInfo(String providerId, String email, String name) {}
}
