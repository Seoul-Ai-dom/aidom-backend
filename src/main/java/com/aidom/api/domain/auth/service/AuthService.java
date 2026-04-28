package com.aidom.api.domain.auth.service;

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
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import com.aidom.api.global.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final AuthCodeRepository authCodeRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final Clock clock;
  private final com.aidom.api.global.config.AppAuthProperties authProperties;

  public AuthService(
      UserRepository userRepository,
      AuthCodeRepository authCodeRepository,
      RefreshTokenRepository refreshTokenRepository,
      JwtTokenProvider jwtTokenProvider,
      Clock clock,
      com.aidom.api.global.config.AppAuthProperties authProperties) {
    this.userRepository = userRepository;
    this.authCodeRepository = authCodeRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.jwtTokenProvider = jwtTokenProvider;
    this.clock = clock;
    this.authProperties = authProperties;
  }

  @Transactional
  public String handleOAuthLogin(Provider provider, String providerId, String email, String name) {
    User user =
        userRepository
            .findByProviderAndProviderId(provider, providerId)
            .map(
                existing -> {
                  existing.updateSocialProfile(name, email);
                  return existing;
                })
            .orElseGet(
                () ->
                    userRepository.save(
                        User.builder()
                            .provider(provider)
                            .providerId(providerId)
                            .email(email)
                            .name(name)
                            .role(Role.USER)
                            .status(UserStatus.ONBOARDING)
                            .build()));

    String rawCode = UUID.randomUUID().toString().replace("-", "");
    AuthCode authCode =
        AuthCode.builder()
            .user(user)
            .codeHash(hash(rawCode))
            .expiresAt(now().plus(authProperties.getJwt().getAuthCodeValidity()))
            .build();
    authCodeRepository.save(authCode);
    return rawCode;
  }

  @Transactional
  public AuthTokenResponse exchangeCode(String code) {
    AuthCode authCode =
        authCodeRepository
            .findByCodeHash(hash(code))
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

    if (authCode.isUsed() || authCode.isExpired(now())) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }
    authCode.markUsed(now());

    User user = authCode.getUser();

    String accessToken = jwtTokenProvider.createAccessToken(user);
    String refreshToken = jwtTokenProvider.createRefreshToken(user);

    refreshTokenRepository.save(
        RefreshToken.builder()
            .user(user)
            .tokenHash(hash(refreshToken))
            .expiresAt(now().plus(authProperties.getJwt().getRefreshTokenValidity()))
            .build());

    return new AuthTokenResponse(
        accessToken,
        refreshToken,
        jwtTokenProvider.getAccessTokenExpiresInSeconds(),
        "Bearer",
        user.getStatus());
  }

  @Transactional
  public AuthTokenResponse refresh(String refreshTokenRaw) {
    Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshTokenRaw);
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

    String currentHash = hash(refreshTokenRaw);
    RefreshToken token =
        refreshTokenRepository
            .findByTokenHash(currentHash)
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

    if (token.isRevoked() || token.isExpired(now())) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }

    String newAccessToken = jwtTokenProvider.createAccessToken(user);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(user);
    String newRefreshHash = hash(newRefreshToken);

    token.revoke(now(), newRefreshHash);
    refreshTokenRepository.save(
        RefreshToken.builder()
            .user(user)
            .tokenHash(newRefreshHash)
            .expiresAt(now().plus(authProperties.getJwt().getRefreshTokenValidity()))
            .build());

    return new AuthTokenResponse(
        newAccessToken,
        newRefreshToken,
        jwtTokenProvider.getAccessTokenExpiresInSeconds(),
        "Bearer",
        user.getStatus());
  }

  @Transactional
  public void logout(String refreshTokenRaw) {
    String tokenHash = hash(refreshTokenRaw);
    Optional<RefreshToken> optional = refreshTokenRepository.findByTokenHash(tokenHash);
    if (optional.isEmpty()) {
      return;
    }
    RefreshToken refreshToken = optional.get();
    if (!refreshToken.isRevoked()) {
      refreshToken.revoke(now(), null);
    }
  }

  private String hash(String raw) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(bytes);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to hash token", e);
    }
  }

  private LocalDateTime now() {
    return LocalDateTime.now(clock);
  }
}
