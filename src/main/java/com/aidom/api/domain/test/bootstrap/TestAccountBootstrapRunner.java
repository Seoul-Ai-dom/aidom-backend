package com.aidom.api.domain.test.bootstrap;

import com.aidom.api.domain.auth.entity.RefreshToken;
import com.aidom.api.domain.auth.repository.RefreshTokenRepository;
import com.aidom.api.domain.test.bootstrap.TestAccountBootstrapProperties.ChildSpec;
import com.aidom.api.domain.user.entity.Child;
import com.aidom.api.domain.user.entity.ProvisionedProfile;
import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.repository.UserRepository;
import com.aidom.api.global.config.AppAuthProperties;
import com.aidom.api.global.security.JwtTokenProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.ops.test-account", name = "enabled", havingValue = "true")
public class TestAccountBootstrapRunner implements ApplicationRunner {

  private final TestAccountBootstrapProperties properties;
  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final AppAuthProperties authProperties;
  private final Clock clock;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    User user = upsertUser();

    String accessToken = jwtTokenProvider.createAccessToken(user);
    String refreshToken = jwtTokenProvider.createRefreshToken(user);

    refreshTokenRepository.save(
        RefreshToken.builder()
            .user(user)
            .tokenHash(hash(refreshToken))
            .expiresAt(now().plus(authProperties.getJwt().getRefreshTokenValidity()))
            .build());

    logBootstrapResult(user, accessToken, refreshToken);
  }

  private User upsertUser() {
    User user =
        userRepository
            .findByEmailIncludingDeleted(properties.getEmail())
            .orElseGet(
                () ->
                    User.builder()
                        .email(properties.getEmail())
                        .name(properties.getName())
                        .provider(properties.getProvider())
                        .providerId(properties.getProviderId())
                        .role(properties.getRole())
                        .status(properties.getStatus())
                        .build());

    user.applyProvisionedProfile(
        ProvisionedProfile.builder()
            .name(properties.getName())
            .email(properties.getEmail())
            .provider(properties.getProvider())
            .providerId(properties.getProviderId())
            .role(properties.getRole())
            .status(properties.getStatus())
            .relation(properties.getRelation())
            .birthDate(properties.getBirthDate())
            .phone(properties.getPhone())
            .address(properties.getAddress())
            .city(properties.getCity())
            .district(properties.getDistrict())
            .addressDetail(properties.getAddressDetail())
            .addressLat(properties.getAddressLat())
            .addressLng(properties.getAddressLng())
            .build());

    user.clearChildren();
    for (ChildSpec childSpec : normalizedChildren(properties.getChildren())) {
      user.addChild(
          Child.of(
              childSpec.getName(),
              childSpec.getBirthDate(),
              childSpec.getGender(),
              childSpec.getSpecialNote(),
              childSpec.isPrimary()));
    }

    return userRepository.saveAndFlush(user);
  }

  private List<ChildSpec> normalizedChildren(List<ChildSpec> children) {
    boolean hasPrimary = children.stream().anyMatch(ChildSpec::isPrimary);

    if (hasPrimary) {
      return children;
    }

    return children.stream()
        .map(
            child ->
                new ChildSpec(
                    child.getName(),
                    child.getBirthDate(),
                    child.getGender(),
                    child.getSpecialNote(),
                    false))
        .collect(Collectors.collectingAndThen(Collectors.toList(), list -> setPrimary(list)));
  }

  private List<ChildSpec> setPrimary(List<ChildSpec> children) {
    if (!children.isEmpty()) {
      children.get(0).setPrimary(true);
    }
    return children;
  }

  private void logBootstrapResult(User user, String accessToken, String refreshToken) {
    String childSummary =
        user.getChildren().stream()
            .map(
                child ->
                    child.getId() + ":" + child.getName() + "(primary=" + child.isPrimary() + ")")
            .collect(Collectors.joining(", "));

    log.warn("============================================================");
    log.warn("PRODUCTION TEST ACCOUNT BOOTSTRAP COMPLETED");
    log.warn("requestId={}", properties.getRequestId());
    log.warn(
        "userId={}, email={}, provider={}, providerId={}, status={}",
        user.getId(),
        user.getEmail(),
        user.getProvider(),
        user.getProviderId(),
        user.getStatus());
    log.warn("childIds={}", childSummary);
    if (properties.isLogAccessToken()) {
      log.warn("accessToken={}", accessToken);
      log.warn("authorizationHeader=Bearer {}", accessToken);
    } else {
      log.warn(
          "accessToken={}...{}",
          accessToken.substring(0, Math.min(8, accessToken.length())),
          "(masked)");
    }
    log.warn("accessTokenExpiresInSeconds={}", jwtTokenProvider.getAccessTokenExpiresInSeconds());
    if (properties.isLogRefreshToken()) {
      log.warn("refreshToken={}", refreshToken);
    }
    log.warn("Disable APP_OPS_TEST_ACCOUNT_ENABLED after collecting the token.");
    log.warn("============================================================");
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
