package com.aidom.api.domain.user.entity;

import com.aidom.api.domain.user.enums.ParentRelation;
import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.domain.user.enums.Role;
import com.aidom.api.domain.user.enums.UserStatus;
import com.aidom.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
@SQLDelete(
    sql = "UPDATE users SET status = 'DELETED', deleted_at = CURRENT_TIMESTAMP WHERE user_id = ?")
@SQLRestriction("status in ('ACTIVE','ONBOARDING') and deleted_at IS NULL")
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
  @Column(length = 20)
  private ParentRelation relation;

  @Column private LocalDate birthDate;

  @Column(length = 20)
  private String phone;

  @Column(length = 200)
  private String address;

  @Column(length = 50)
  private String city;

  @Column(length = 50)
  private String district;

  @Column(length = 200)
  private String addressDetail;

  @Column(precision = 10, scale = 7)
  private BigDecimal addressLat;

  @Column(precision = 10, scale = 7)
  private BigDecimal addressLng;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("isPrimary DESC, id ASC")
  private List<Child> children = new ArrayList<>();

  private LocalDateTime deletedAt;

  @Builder
  private User(
      String name,
      String email,
      Provider provider,
      String providerId,
      Role role,
      UserStatus status,
      ParentRelation relation,
      LocalDate birthDate,
      String phone,
      String address,
      String city,
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
    this.relation = relation;
    this.birthDate = birthDate;
    this.phone = phone;
    this.address = address;
    this.city = city;
    this.district = district;
    this.addressDetail = addressDetail;
    this.addressLat = addressLat;
    this.addressLng = addressLng;
  }

  public void updateSocialProfile(String name, String email) {
    if (name != null && !name.isBlank()) {
      this.name = name.trim();
    }
    if (email != null && !email.isBlank()) {
      this.email = email.trim();
    }
  }

  public void syncSocialIdentity(Provider provider, String providerId) {
    this.provider = provider;
    this.providerId = providerId;
  }

  public void completeOnboarding(
      String name,
      LocalDate birthDate,
      ParentRelation relation,
      String city,
      String district,
      String phone) {
    this.name = normalizeText(name);
    this.birthDate = birthDate;
    this.relation = relation;
    this.city = normalizeText(city);
    this.district = normalizeText(district);
    this.phone = normalizeText(phone);
    this.status = UserStatus.ACTIVE;
  }

  public void addChild(Child child) {
    children.add(child);
    child.assignUser(this);
  }

  public void removeChild(Child child) {
    children.remove(child);
    child.assignUser(null);
  }

  public void clearChildren() {
    Iterator<Child> iterator = children.iterator();
    while (iterator.hasNext()) {
      Child child = iterator.next();
      iterator.remove();
      child.assignUser(null);
    }
  }

  public void updateProfile(
      String name,
      ParentRelation relation,
      LocalDate birthDate,
      String phone,
      String address,
      String detailAddress) {
    this.name = normalizeText(name);
    this.relation = relation;
    this.birthDate = birthDate;
    this.phone = normalizeText(phone);
    this.address = normalizeText(address);
    this.addressDetail = normalizeText(detailAddress);
  }

  public boolean isWithdrawn() {
    return status == UserStatus.DELETED || status == UserStatus.WITHDRAW;
  }

  public void reactivateForRejoin() {
    this.status = UserStatus.ONBOARDING;
    this.deletedAt = null;
  }

  private String normalizeText(String value) {
    return value == null ? null : value.trim();
  }
}
