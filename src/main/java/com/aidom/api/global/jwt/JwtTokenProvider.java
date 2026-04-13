package com.aidom.api.global.jwt;

import com.aidom.api.domain.user.entity.User;
import com.aidom.api.global.security.AuthProperties;
import com.aidom.api.global.security.PrincipalDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  private final AuthProperties authProperties;
  private final PrincipalDetailsService principalDetailsService;

  private SecretKey key;

  @PostConstruct
  protected void init() {
    String secretKeyPlain = authProperties.getJwt().getSecret();
    if (secretKeyPlain == null || secretKeyPlain.isBlank()) {
      throw new IllegalStateException("app.auth.jwt.secret 프로퍼티가 설정되지 않았습니다.");
    }

    byte[] keyBytes = Decoders.BASE64.decode(secretKeyPlain);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  public String createAccessToken(User user) {
    Instant now = Instant.now();
    Instant expiry = now.plus(authProperties.getJwt().getAccessTokenValidity());

    return Jwts.builder()
        .subject(String.valueOf(user.getId()))
        .claim("email", user.getEmail())
        .claim("role", user.getRole().name())
        .claim("status", user.getStatus().name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiry))
        .signWith(key)
        .compact();
  }

  public Authentication getAuthentication(String token) {
    UserDetails userDetails = principalDetailsService.loadUserByUsername(getUserId(token));
    return new UsernamePasswordAuthenticationToken(
        userDetails, token, userDetails.getAuthorities());
  }

  public String getUserId(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      log.warn("Invalid JWT signature");
    } catch (ExpiredJwtException e) {
      log.warn("Expired JWT token");
    } catch (UnsupportedJwtException e) {
      log.warn("Unsupported JWT token");
    } catch (IllegalArgumentException e) {
      log.warn("JWT token is blank or malformed");
    }
    return false;
  }
}
