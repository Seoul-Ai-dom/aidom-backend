package com.aidom.api.domain.visit.entity;

import com.aidom.api.domain.facility.entity.Facility;
import com.aidom.api.domain.user.entity.Child;
import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.visit.enums.VisitSource;
import com.aidom.api.domain.visit.enums.VisitStatus;
import com.aidom.api.global.common.entity.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "visit_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "id", column = @Column(name = "visit_id"))
public class VisitHistory extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "child_id", nullable = false)
  private Child child;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facility_id", nullable = false)
  private Facility facility;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private VisitStatus status;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private VisitSource source;

  @Column(name = "visit_date")
  private LocalDate visitDate;

  private LocalTime startTime;

  private LocalTime endTime;

  private LocalDateTime confirmedAt;

  private LocalDateTime cancelledAt;

  @Builder
  private VisitHistory(
      User user,
      Child child,
      Facility facility,
      VisitStatus status,
      VisitSource source,
      LocalDate visitDate,
      LocalTime startTime,
      LocalTime endTime) {
    this.user = user;
    this.child = child;
    this.facility = facility;
    this.status = status;
    this.source = source;
    this.visitDate = visitDate;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public void updateSchedule(LocalDate visitDate, LocalTime startTime, LocalTime endTime) {
    if (this.status != VisitStatus.PLANNED) {
      throw new IllegalStateException("PLANNED 상태의 이용 내역만 수정할 수 있습니다.");
    }

    LocalTime nextStartTime = startTime != null ? startTime : this.startTime;
    LocalTime nextEndTime = endTime != null ? endTime : this.endTime;
    validateTimeRange(nextStartTime, nextEndTime);

    if (visitDate != null) {
      this.visitDate = visitDate;
    }
    if (startTime != null) {
      this.startTime = startTime;
    }
    if (endTime != null) {
      this.endTime = endTime;
    }
  }

  public void confirm(LocalDate visitDate, LocalTime startTime, LocalTime endTime) {
    if (this.status == VisitStatus.CONFIRMED) {
      throw new IllegalStateException("이미 확정된 이용 내역입니다.");
    }
    if (this.status == VisitStatus.CANCELLED) {
      throw new IllegalStateException("취소된 이용 내역은 확정할 수 없습니다.");
    }

    validateTimeRange(startTime, endTime);

    this.status = VisitStatus.CONFIRMED;
    this.visitDate = visitDate;
    this.startTime = startTime;
    this.endTime = endTime;
    this.confirmedAt = LocalDateTime.now();
  }

  public void cancel() {
    if (this.status == VisitStatus.CANCELLED) {
      throw new IllegalStateException("이미 취소된 이용 내역입니다.");
    }
    this.status = VisitStatus.CANCELLED;
    this.cancelledAt = LocalDateTime.now();
  }

  private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
    if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
      throw new IllegalArgumentException("종료 시간은 시작 시간보다 빠를 수 없습니다.");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VisitHistory that)) return false;
    return getId() != null && getId().equals(that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }
}
