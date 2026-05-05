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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "children")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "id", column = @Column(name = "child_id"))
@SQLDelete(sql = "UPDATE children SET deleted_at = CURRENT_TIMESTAMP WHERE child_id = ?")
@SQLRestriction("deleted_at IS NULL")
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

  public static Child of(
      String name, LocalDate birthDate, Gender gender, String specialNote, boolean isPrimary) {
    return Child.builder()
        .name(normalizeText(name))
        .birthDate(birthDate)
        .gender(gender)
        .specialNote(normalizeNote(specialNote))
        .isPrimary(isPrimary)
        .build();
  }

  public void updateProfile(String name, LocalDate birthDate, Gender gender, String specialNote) {
    this.name = normalizeText(name);
    this.birthDate = birthDate;
    this.gender = gender;
    this.specialNote = normalizeNote(specialNote);
  }

  public void markPrimary(boolean primary) {
    isPrimary = primary;
  }

  void assignUser(User user) {
    this.user = user;
  }

  private static String normalizeText(String value) {
    return value == null ? null : value.trim();
  }

  private static String normalizeNote(String note) {
    if (note == null || note.isBlank()) {
      return null;
    }
    return note.trim();
  }
}
