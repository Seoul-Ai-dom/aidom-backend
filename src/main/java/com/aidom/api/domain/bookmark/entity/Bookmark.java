package com.aidom.api.domain.bookmark.entity;

import com.aidom.api.domain.facility.entity.Facility;
import com.aidom.api.domain.user.entity.User;
import com.aidom.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "bookmarks",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_bookmark_user_facility",
            columnNames = {"user_id", "facility_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "id", column = @Column(name = "bookmark_id"))
public class Bookmark extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facility_id", nullable = false)
  private Facility facility;

  private Bookmark(User user, Facility facility) {
    this.user = user;
    this.facility = facility;
  }

  public static Bookmark of(User user, Facility facility) {
    return new Bookmark(user, facility);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Bookmark bookmark)) return false;
    return getId() != null && getId().equals(bookmark.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }
}
