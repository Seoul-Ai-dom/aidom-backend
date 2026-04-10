package com.aidom.api.domain.facility.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "facility_external_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FacilityExternalInfo {

  @Id
  @Column(name = "facility_id", length = 20)
  private String facilityId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "facility_id")
  private Facility facility;

  @Column(length = 20)
  private String phone;

  @Column(length = 500)
  private String website;

  @Column(columnDefinition = "TEXT")
  private String naverHours;

  @Column(length = 20)
  private String businessStatus;

  /** null이면 Facility.address로 fallback * */
  @Column(length = 300)
  private String naverAddress;

  /** null이면 Facility의 fee/monthly_fee로 fallback. */
  @Column(columnDefinition = "TEXT")
  private String feeText;

  @Column(length = 500)
  private String feeImageUrl;

  @Column(length = 500)
  private String thumbnailUrl;

  /** 네이버 마지막 동기화 시각. */
  @Column(nullable = false)
  private LocalDateTime syncedAt;

  @Builder
  private void FacilityNaverInfo(
      Facility facility,
      String phone,
      String website,
      String naverHours,
      String businessStatus,
      String naverAddress,
      String feeText,
      String feeImageUrl,
      String thumbnailUrl,
      LocalDateTime syncedAt) {
    this.facility = facility;
    this.phone = phone;
    this.website = website;
    this.naverHours = naverHours;
    this.businessStatus = businessStatus;
    this.naverAddress = naverAddress;
    this.feeText = feeText;
    this.feeImageUrl = feeImageUrl;
    this.thumbnailUrl = thumbnailUrl;
    this.syncedAt = syncedAt;
  }

  /** 네이버 배치가 재동기화할 때 호출. */
  public void applyNaverSync(
      String phone,
      String website,
      String naverHours,
      String businessStatus,
      String naverAddress,
      String feeText,
      String feeImageUrl,
      String thumbnailUrl) {
    this.phone = phone;
    this.website = website;
    this.naverHours = naverHours;
    this.businessStatus = businessStatus;
    this.naverAddress = naverAddress;
    this.feeText = feeText;
    this.feeImageUrl = feeImageUrl;
    this.thumbnailUrl = thumbnailUrl;
    this.syncedAt = LocalDateTime.now();
  }
}
