package com.aidom.api.global.security;

import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.domain.user.enums.Role;
import com.aidom.api.domain.user.enums.UserStatus;
import com.aidom.api.global.config.AppAuthProperties;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  private static final String DEV_FALLBACK_BASE64_SECRET =
      "VGhpc0lzQVRlc3RTZWNyZXRLZXlGb3JBaWRvbUF1dGhUb2tlbg==";

  private final SecretKey key;
  private final AppAuthProperties properties;

  public JwtTokenProvider(AppAuthProperties properties) {
    this.key = Keys.hmacShaKeyFor(resolveSecretBytes(properties.getJwt().getSecret()));
    this.properties = properties;
  }

  public String createAccessToken(User user) {
    return createToken(user, JwtTokenType.ACCESS, properties.getJwt().getAccessTokenValidity());
  }

  public String createRefreshToken(User user) {
    return createToken(user, JwtTokenType.REFRESH, properties.getJwt().getRefreshTokenValidity());
  }

  public long getAccessTokenExpiresInSeconds() {
    return properties.getJwt().getAccessTokenValidity().toSeconds();
  }

  public Claims parseClaims(String token) {
    try {
      return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    } catch (SignatureException | DecodingException e) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    } catch (io.jsonwebtoken.ExpiredJwtException e) {
      throw new CustomException(ErrorCode.EXPIRED_TOKEN);
    } catch (Exception e) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }
  }

  public AuthenticatedUserPrincipal getPrincipalFromAccessToken(String token) {
    Claims claims = parseClaims(token);
    String tokenType = claims.get("tokenType", String.class);
    if (!JwtTokenType.ACCESS.name().equals(tokenType)) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }

    Long userId = claims.get("userId", Long.class);
    Role role = Role.valueOf(claims.get("role", String.class));
    UserStatus status = UserStatus.valueOf(claims.get("status", String.class));
    Provider provider = Provider.valueOf(claims.get("provider", String.class));
    String email = claims.get("email", String.class);

    return new AuthenticatedUserPrincipal(userId, role, status, provider, email);
  }

  public Long getUserIdFromRefreshToken(String token) {
    Claims claims = parseClaims(token);
    String tokenType = claims.get("tokenType", String.class);
    if (!JwtTokenType.REFRESH.name().equals(tokenType)) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }
    return claims.get("userId", Long.class);
  }

  private String createToken(User user, JwtTokenType tokenType, Duration validity) {

    Instant now = Instant.now();
    Instant expiry = now.plus(validity);

    return Jwts.builder()
        .subject(String.valueOf(user.getId()))
        .id(UUID.randomUUID().toString())
        .claim("userId", user.getId())
        .claim("role", user.getRole().name())
        .claim("status", user.getStatus().name())
        .claim("provider", user.getProvider().name())
        .claim("email", user.getEmail())
        .claim("tokenType", tokenType.name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiry))
        .signWith(key)
        .compact();
  }

  private static byte[] resolveSecretBytes(String rawSecret) {
    if (rawSecret == null || rawSecret.isBlank()) {
      log.warn("JWT secret is missing or blank. Falling back to development secret.");
      return Decoders.BASE64.decode(DEV_FALLBACK_BASE64_SECRET);
    }
    try {
      return Decoders.BASE64.decode(rawSecret);
    } catch (Exception ignored) {
      return rawSecret.getBytes(StandardCharsets.UTF_8);
    }
  }
}
