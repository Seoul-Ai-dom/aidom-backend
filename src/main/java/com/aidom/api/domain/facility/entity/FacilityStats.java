package com.aidom.api.domain.facility.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "facility_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FacilityStats {

  @Id
  @Column(name = "facility_id", length = 20)
  private String facilityId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "facility_id")
  private Facility facility;

  @Column(name = "avg_rating", nullable = false, precision = 2, scale = 1)
  private BigDecimal avgRating;

  @Column(name = "avg_rating_safety", nullable = false, precision = 2, scale = 1)
  private BigDecimal avgRatingSafety;

  @Column(name = "avg_rating_cleanliness", nullable = false, precision = 2, scale = 1)
  private BigDecimal avgRatingCleanliness;

  @Column(name = "avg_rating_management", nullable = false, precision = 2, scale = 1)
  private BigDecimal avgRatingManagement;

  @Column(name = "avg_rating_kindness", nullable = false, precision = 2, scale = 1)
  private BigDecimal avgRatingKindness;

  @Column(name = "review_count", nullable = false)
  private int reviewCount;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Builder
  private FacilityStats(
      Facility facility,
      BigDecimal avgRating,
      BigDecimal avgRatingSafety,
      BigDecimal avgRatingCleanliness,
      BigDecimal avgRatingManagement,
      BigDecimal avgRatingKindness,
      int reviewCount) {
    this.facility = facility;
    this.avgRating = avgRating;
    this.avgRatingSafety = avgRatingSafety;
    this.avgRatingCleanliness = avgRatingCleanliness;
    this.avgRatingManagement = avgRatingManagement;
    this.avgRatingKindness = avgRatingKindness;
    this.reviewCount = reviewCount;
    this.updatedAt = LocalDateTime.now();
  }

  /** 리뷰 집계 결과로 통계를 재계산한다. */
  public void recalculate(
      BigDecimal avgRating,
      BigDecimal avgRatingSafety,
      BigDecimal avgRatingCleanliness,
      BigDecimal avgRatingManagement,
      BigDecimal avgRatingKindness,
      int reviewCount) {
    this.avgRating = avgRating.setScale(1, RoundingMode.HALF_UP);
    this.avgRatingSafety = avgRatingSafety.setScale(1, RoundingMode.HALF_UP);
    this.avgRatingCleanliness = avgRatingCleanliness.setScale(1, RoundingMode.HALF_UP);
    this.avgRatingManagement = avgRatingManagement.setScale(1, RoundingMode.HALF_UP);
    this.avgRatingKindness = avgRatingKindness.setScale(1, RoundingMode.HALF_UP);
    this.reviewCount = reviewCount;
    this.updatedAt = LocalDateTime.now();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FacilityStats that)) return false;
    return facilityId != null && facilityId.equals(that.facilityId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(facilityId);
  }
}
