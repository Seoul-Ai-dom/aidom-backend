package com.aidom.api.domain.bookmark.entity;

import com.aidom.api.domain.facility.entity.Facility;
import com.aidom.api.domain.user.entity.User;
import com.aidom.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bookmarks")
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

  @Builder
  private Bookmark(User user, Facility facility) {
    this.user = user;
    this.facility = facility;
  }
}
