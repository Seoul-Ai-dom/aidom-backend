package com.aidom.api.domain.facility.entity;

import com.aidom.api.domain.facility.enums.ServiceType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "facilities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Facility {

  @Id
  @Column(name = "facility_id", length = 20)
  private String id;

  @Column(name = "facility_name", nullable = false)
  private String facilityName;

  @Column(name = "service_type_code", nullable = false, length = 30)
  private String serviceTypeCode;

  @Column(name = "service_type", nullable = false)
  private ServiceType serviceType;

  @Column(name = "district_code", nullable = false, length = 10)
  private String districtCode;

  @Column(name = "district_name", nullable = false, length = 50)
  private String districtName;

  @Column(columnDefinition = "TEXT")
  private String address;

  @Column(precision = 10, scale = 7)
  private BigDecimal lat;

  @Column(precision = 10, scale = 7)
  private BigDecimal lng;

  @Column(name = "age_group", nullable = false, length = 50)
  private String ageGroup;

  @Column(name = "age_min", nullable = false)
  private int ageMin;

  @Column(name = "age_max", nullable = false)
  private int ageMax;

  @Column(name = "booking_required", nullable = false)
  private boolean bookingRequired;

  @Column(name = "is_free", nullable = false)
  private boolean isFree;

  private Integer fee;

  @Column(name = "monthly_fee")
  private Integer monthlyFee;

  @Column(name = "capacity_regular")
  private Integer capacityRegular;

  @Column(name = "capacity_temporary")
  private Integer capacityTemporary;

  @Column(name = "area_sqm", precision = 10, scale = 2)
  private BigDecimal areaSqm;

  @Column(name = "operating_days", columnDefinition = "TEXT")
  private String operatingDays;

  @Column(name = "closed_days", columnDefinition = "TEXT")
  private String closedDays;

  @Column(name = "has_regular_program", nullable = false)
  private boolean hasRegularProgram;

  @Column(name = "has_regular_care", nullable = false)
  private boolean hasRegularCare;

  @Column(name = "has_temporary_care", nullable = false)
  private boolean hasTemporaryCare;

  @OneToOne(mappedBy = "facility", fetch = FetchType.LAZY)
  private FacilityExternalInfo externalInfo;

  @OneToOne(mappedBy = "facility", fetch = FetchType.LAZY)
  private FacilityStats stats;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate private LocalDateTime updatedAt;

  @Builder
  private Facility(
      String id,
      String facilityName,
      String serviceTypeCode,
      ServiceType serviceType,
      String districtCode,
      String districtName,
      String address,
      BigDecimal lat,
      BigDecimal lng,
      String ageGroup,
      int ageMin,
      int ageMax,
      boolean bookingRequired,
      boolean isFree,
      Integer fee,
      Integer monthlyFee,
      Integer capacityRegular,
      Integer capacityTemporary,
      BigDecimal areaSqm,
      String operatingDays,
      String closedDays,
      boolean hasRegularProgram,
      boolean hasRegularCare,
      boolean hasTemporaryCare) {
    this.id = id;
    this.facilityName = facilityName;
    this.serviceTypeCode = serviceTypeCode;
    this.serviceType = serviceType;
    this.districtCode = districtCode;
    this.districtName = districtName;
    this.address = address;
    this.lat = lat;
    this.lng = lng;
    this.ageGroup = ageGroup;
    this.ageMin = ageMin;
    this.ageMax = ageMax;
    this.bookingRequired = bookingRequired;
    this.isFree = isFree;
    this.fee = fee;
    this.monthlyFee = monthlyFee;
    this.capacityRegular = capacityRegular;
    this.capacityTemporary = capacityTemporary;
    this.areaSqm = areaSqm;
    this.operatingDays = operatingDays;
    this.closedDays = closedDays;
    this.hasRegularProgram = hasRegularProgram;
    this.hasRegularCare = hasRegularCare;
    this.hasTemporaryCare = hasTemporaryCare;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Facility facility)) return false;
    return id != null && id.equals(facility.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
