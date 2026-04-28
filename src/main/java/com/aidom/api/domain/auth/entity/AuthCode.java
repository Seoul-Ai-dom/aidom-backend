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
    name = "auth_codes",
    indexes = {
      @Index(name = "idx_auth_code_hash", columnList = "codeHash", unique = true),
      @Index(name = "idx_auth_code_expires_at", columnList = "expiresAt")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "id", column = @Column(name = "auth_code_id"))
public class AuthCode extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false, length = 128, unique = true)
  private String codeHash;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  private LocalDateTime usedAt;

  @Builder
  private AuthCode(User user, String codeHash, LocalDateTime expiresAt) {
    this.user = user;
    this.codeHash = codeHash;
    this.expiresAt = expiresAt;
  }

  public boolean isExpired(LocalDateTime now) {
    return expiresAt.isBefore(now);
  }

  public boolean isUsed() {
    return usedAt != null;
  }

  public void markUsed(LocalDateTime now) {
    this.usedAt = now;
  }
}
