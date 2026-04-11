package com.aidom.api.domain.user.entity;

import com.aidom.api.global.common.entity.BaseEntity;
import com.aidom.api.global.common.entity.Gender;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "children")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "id", column = @Column(name = "child_id"))
public class Child extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(nullable = false)
  private LocalDate birthDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Gender gender;

  @Column(columnDefinition = "TEXT")
  private String specialNote;

  @Column(nullable = false)
  private boolean isPrimary = false;

  private LocalDateTime deletedAt;

  @Builder
  private Child(
      User user,
      String name,
      LocalDate birthDate,
      Gender gender,
      String specialNote,
      boolean isPrimary) {
    this.user = user;
    this.name = name;
    this.birthDate = birthDate;
    this.gender = gender;
    this.specialNote = specialNote;
    this.isPrimary = isPrimary;
  }
}
