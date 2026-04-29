package com.aidom.api.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.domain.user.enums.Role;
import com.aidom.api.domain.user.enums.UserStatus;
import com.aidom.api.global.common.entity.Gender;
import com.aidom.api.global.config.AppAuthProperties;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {

  private JwtTokenProvider jwtTokenProvider;

  @BeforeEach
  void setUp() {
    AppAuthProperties properties = new AppAuthProperties();
    properties.getJwt().setSecret("VGhpc0lzQVRlc3RTZWNyZXRLZXlGb3JBaWRvbUF1dGhUb2tlbg==");
    jwtTokenProvider = new JwtTokenProvider(properties);
  }

  @Test
  @DisplayName("access token에서 principal을 추출할 수 있다")
  void getPrincipalFromAccessToken() {
    User user = createUser();
    String accessToken = jwtTokenProvider.createAccessToken(user);

    AuthenticatedUserPrincipal principal =
        jwtTokenProvider.getPrincipalFromAccessToken(accessToken);

    assertThat(principal.userId()).isEqualTo(1L);
    assertThat(principal.role()).isEqualTo(Role.USER);
    assertThat(principal.status()).isEqualTo(UserStatus.ACTIVE);
    assertThat(principal.provider()).isEqualTo(Provider.KAKAO);
    assertThat(principal.email()).isEqualTo("user@aidom.kr");
  }

  @Test
  @DisplayName("refresh token을 access token 파서로 읽으면 INVALID_TOKEN 예외가 발생한다")
  void getPrincipalFromAccessToken_withRefreshToken_throwsInvalidToken() {
    User user = createUser();
    String refreshToken = jwtTokenProvider.createRefreshToken(user);

    assertThatThrownBy(() -> jwtTokenProvider.getPrincipalFromAccessToken(refreshToken))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_TOKEN);
  }

  private User createUser() {
    User user =
        User.builder()
            .name("홍길동")
            .email("user@aidom.kr")
            .provider(Provider.KAKAO)
            .providerId("kakao-123")
            .role(Role.USER)
            .status(UserStatus.ACTIVE)
            .gender(Gender.MALE)
            .birthDate(LocalDate.of(1990, 1, 1))
            .phone("01012345678")
            .district("강남구")
            .addressDetail("테헤란로")
            .build();
    ReflectionTestUtils.setField(user, "id", 1L);
    return user;
  }
}
