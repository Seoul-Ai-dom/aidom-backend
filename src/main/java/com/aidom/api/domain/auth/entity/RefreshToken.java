package com.aidom.api.domain.auth.entity;

import com.aidom.api.domain.user.entity.User;
import com.aidom.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
      @Index(name = "idx_refresh_token_hash", columnList = "tokenHash", unique = true),
      @Index(name = "idx_refresh_token_expires_at", columnList = "expiresAt")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "id", column = @Column(name = "refresh_token_id"))
public class RefreshToken extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false, length = 128, unique = true)
  private String tokenHash;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  private LocalDateTime revokedAt;

  @Column(length = 128)
  private String replacedByHash;

  @Builder
  private RefreshToken(User user, String tokenHash, LocalDateTime expiresAt) {
    this.user = user;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
  }

  public boolean isExpired(LocalDateTime now) {
    return expiresAt.isBefore(now);
  }

  public boolean isRevoked() {
    return revokedAt != null;
  }

  public void revoke(LocalDateTime now, String replacedByHash) {
    this.revokedAt = now;
    this.replacedByHash = replacedByHash;
  }
}
