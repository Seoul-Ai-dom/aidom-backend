package com.aidom.api.domain.test.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aidom.api.domain.auth.entity.RefreshToken;
import com.aidom.api.domain.auth.repository.RefreshTokenRepository;
import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.domain.user.enums.Role;
import com.aidom.api.domain.user.enums.UserStatus;
import com.aidom.api.domain.user.repository.UserRepository;
import com.aidom.api.global.config.AppAuthProperties;
import com.aidom.api.global.security.JwtTokenProvider;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

@ExtendWith(MockitoExtension.class)
class TestAccountBootstrapRunnerTest {

  private static final Clock FIXED_CLOCK =
      Clock.fixed(Instant.parse("2026-05-06T00:00:00Z"), ZoneId.of("Asia/Seoul"));

  @Mock private UserRepository userRepository;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private JwtTokenProvider jwtTokenProvider;

  @Captor private ArgumentCaptor<User> userCaptor;
  @Captor private ArgumentCaptor<RefreshToken> refreshTokenCaptor;

  @Test
  @DisplayName("활성화되면 테스트 부모/자녀를 저장하고 실제 토큰을 발급한다")
  void run_bootstrapsUserChildrenAndRefreshToken() throws Exception {
    TestAccountBootstrapProperties properties = new TestAccountBootstrapProperties();
    AppAuthProperties authProperties = new AppAuthProperties();
    authProperties.getJwt().setRefreshTokenValidity(Duration.ofDays(14));

    when(userRepository.findByEmailIncludingDeleted(properties.getEmail()))
        .thenReturn(Optional.empty());
    when(userRepository.saveAndFlush(any(User.class)))
        .thenAnswer(
            invocation -> {
              User saved = invocation.getArgument(0);
              return saved;
            });
    when(jwtTokenProvider.createAccessToken(any(User.class))).thenReturn("access-token");
    when(jwtTokenProvider.createRefreshToken(any(User.class))).thenReturn("refresh-token");

    TestAccountBootstrapRunner runner =
        new TestAccountBootstrapRunner(
            properties,
            userRepository,
            refreshTokenRepository,
            jwtTokenProvider,
            authProperties,
            FIXED_CLOCK);

    runner.run(new DefaultApplicationArguments(new String[0]));

    verify(userRepository).saveAndFlush(userCaptor.capture());
    verify(refreshTokenRepository).save(refreshTokenCaptor.capture());

    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getEmail()).isEqualTo(properties.getEmail());
    assertThat(savedUser.getProvider()).isEqualTo(Provider.KAKAO);
    assertThat(savedUser.getRole()).isEqualTo(Role.USER);
    assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(savedUser.getChildren()).hasSize(2);
    assertThat(savedUser.getChildren().get(0).isPrimary()).isTrue();

    RefreshToken refreshToken = refreshTokenCaptor.getValue();
    assertThat(refreshToken.getUser()).isSameAs(savedUser);
    assertThat(refreshToken.getTokenHash()).hasSize(64);
  }
}
