package com.aidom.api.domain.visit.entity;

import com.aidom.api.domain.facility.entity.Facility;
import com.aidom.api.domain.user.entity.Child;
import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.visit.enums.VisitSource;
import com.aidom.api.domain.visit.enums.VisitStatus;
import com.aidom.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "visit_histories")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
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

  /** 로직 확인 후 수정 예정 * */
  @Column(nullable = false)
  private LocalDate visitDate;

  private LocalTime startTime;

  private LocalTime endTime;

  private LocalDateTime confirmedAt;

  private LocalDateTime cancelledAt;
}
