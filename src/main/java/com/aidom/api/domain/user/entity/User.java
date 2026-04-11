package com.aidom.api.domain.user.entity;

import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.domain.user.enums.Role;
import com.aidom.api.domain.user.enums.UserStatus;
import com.aidom.api.global.common.entity.BaseEntity;
import com.aidom.api.global.common.entity.Gender;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity {

  @Column(nullable = false, length = 50)
  private String name;

  @Column(nullable = false)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Provider provider;

  @Column(nullable = false)
  private String providerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserStatus status;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Gender gender;

  @Column(nullable = false)
  private LocalDate birthDate;

  @Column(nullable = false, length = 20)
  private String phone;

  @Column(nullable = false, length = 50)
  private String district;

  @Column(length = 200)
  private String addressDetail;

  @Column(precision = 10, scale = 7)
  private BigDecimal addressLat;

  @Column(precision = 10, scale = 7)
  private BigDecimal addressLng;

  private LocalDateTime deletedAt;

  @Builder
  private User(
      String name,
      String email,
      Provider provider,
      String providerId,
      Role role,
      UserStatus status,
      Gender gender,
      LocalDate birthDate,
      String phone,
      String district,
      String addressDetail,
      BigDecimal addressLat,
      BigDecimal addressLng) {
    this.name = name;
    this.email = email;
    this.provider = provider;
    this.providerId = providerId;
    this.role = role;
    this.status = status;
    this.gender = gender;
    this.birthDate = birthDate;
    this.phone = phone;
    this.district = district;
    this.addressDetail = addressDetail;
    this.addressLat = addressLat;
    this.addressLng = addressLng;
  }
}
