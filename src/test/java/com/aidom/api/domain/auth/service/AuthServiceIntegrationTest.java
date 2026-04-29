package com.aidom.api.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aidom.api.domain.auth.dto.AuthTokenResponse;
import com.aidom.api.domain.auth.entity.AuthCode;
import com.aidom.api.domain.auth.entity.RefreshToken;
import com.aidom.api.domain.auth.repository.AuthCodeRepository;
import com.aidom.api.domain.auth.repository.RefreshTokenRepository;
import com.aidom.api.domain.user.UserRepository;
import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.domain.user.enums.Role;
import com.aidom.api.domain.user.enums.UserStatus;
import com.aidom.api.global.common.entity.Gender;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import com.aidom.api.global.security.JwtTokenProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceIntegrationTest {

  @Autowired private AuthService authService;
  @Autowired private JwtTokenProvider jwtTokenProvider;
  @Autowired private UserRepository userRepository;
  @Autowired private AuthCodeRepository authCodeRepository;
  @Autowired private RefreshTokenRepository refreshTokenRepository;

  @BeforeEach
  void setUp() {
    refreshTokenRepository.deleteAll();
    authCodeRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("auth code를 교환하면 access/refresh token이 발급되고 코드가 사용 처리된다")
  void exchangeCode_success() {
    User user = userRepository.save(createUser("kakao-1", "alpha@aidom.kr"));
    String rawCode = "plain-auth-code";
    authCodeRepository.save(
        AuthCode.builder()
            .user(user)
            .codeHash(hash(rawCode))
            .expiresAt(LocalDateTime.now().plusMinutes(1))
            .build());

    AuthTokenResponse response = authService.exchangeCode(rawCode);

    assertThat(response.accessToken()).isNotBlank();
    assertThat(response.refreshToken()).isNotBlank();
    assertThat(response.tokenType()).isEqualTo("Bearer");
    assertThat(response.userStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(authCodeRepository.findByCodeHash(hash(rawCode)))
        .get()
        .extracting(AuthCode::isUsed)
        .isEqualTo(true);
    assertThat(refreshTokenRepository.findByTokenHash(hash(response.refreshToken()))).isPresent();
  }

  @Test
  @DisplayName("이미 사용된 auth code를 다시 교환하면 INVALID_TOKEN 예외가 발생한다")
  void exchangeCode_usedCode_throwsInvalidToken() {
    User user = userRepository.save(createUser("kakao-2", "beta@aidom.kr"));
    String rawCode = "used-auth-code";
    AuthCode authCode =
        AuthCode.builder()
            .user(user)
            .codeHash(hash(rawCode))
            .expiresAt(LocalDateTime.now().plusMinutes(1))
            .build();
    authCode.markUsed(LocalDateTime.now());
    authCodeRepository.save(authCode);

    assertThatThrownBy(() -> authService.exchangeCode(rawCode))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_TOKEN);
  }

  @Test
  @DisplayName("refresh token 재발급 시 기존 토큰은 revoke되고 새 refresh token이 저장된다")
  void refresh_rotatesRefreshToken() {
    User user = userRepository.save(createUser("kakao-3", "gamma@aidom.kr"));
    String oldRefresh = jwtTokenProvider.createRefreshToken(user);

    refreshTokenRepository.save(
        RefreshToken.builder()
            .user(user)
            .tokenHash(hash(oldRefresh))
            .expiresAt(LocalDateTime.now().plusDays(14))
            .build());

    AuthTokenResponse response = authService.refresh(oldRefresh);

    RefreshToken oldToken = refreshTokenRepository.findByTokenHash(hash(oldRefresh)).orElseThrow();
    RefreshToken newToken =
        refreshTokenRepository.findByTokenHash(hash(response.refreshToken())).orElseThrow();

    assertThat(response.accessToken()).isNotBlank();
    assertThat(oldToken.isRevoked()).isTrue();
    assertThat(oldToken.getReplacedByHash()).isEqualTo(hash(response.refreshToken()));
    assertThat(newToken.isRevoked()).isFalse();
  }

  @Test
  @DisplayName("logout 호출 시 refresh token이 revoke 된다")
  void logout_revokesRefreshToken() {
    User user = userRepository.save(createUser("kakao-4", "delta@aidom.kr"));
    String refresh = jwtTokenProvider.createRefreshToken(user);
    refreshTokenRepository.save(
        RefreshToken.builder()
            .user(user)
            .tokenHash(hash(refresh))
            .expiresAt(LocalDateTime.now().plusDays(14))
            .build());

    authService.logout(refresh);

    RefreshToken revoked = refreshTokenRepository.findByTokenHash(hash(refresh)).orElseThrow();
    assertThat(revoked.isRevoked()).isTrue();
  }

  private User createUser(String providerId, String email) {
    return User.builder()
        .name("테스트유저")
        .email(email)
        .provider(Provider.KAKAO)
        .providerId(providerId)
        .role(Role.USER)
        .status(UserStatus.ACTIVE)
        .gender(Gender.FEMALE)
        .birthDate(LocalDate.of(1992, 2, 2))
        .phone("01011112222")
        .district("서초구")
        .addressDetail("서초대로")
        .build();
  }

  private String hash(String raw) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
