package com.aidom.api.domain.visit.service;

import com.aidom.api.domain.facility.entity.Facility;
import com.aidom.api.domain.facility.repository.FacilityRepository;
import com.aidom.api.domain.user.entity.Child;
import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.repository.ChildRepository;
import com.aidom.api.domain.user.repository.UserRepository;
import com.aidom.api.domain.visit.dto.VisitConfirmRequest;
import com.aidom.api.domain.visit.dto.VisitCreateRequest;
import com.aidom.api.domain.visit.dto.VisitResponse;
import com.aidom.api.domain.visit.dto.VisitSummaryResponse;
import com.aidom.api.domain.visit.dto.VisitUpdateRequest;
import com.aidom.api.domain.visit.entity.VisitHistory;
import com.aidom.api.domain.visit.enums.VisitStatus;
import com.aidom.api.domain.visit.repository.VisitHistoryRepository;
import com.aidom.api.global.common.dto.SliceResponse;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitService {

  private final VisitHistoryRepository visitHistoryRepository;
  private final UserRepository userRepository;
  private final ChildRepository childRepository;
  private final FacilityRepository facilityRepository;

  public SliceResponse<VisitResponse> getMyVisits(
      Long userId, int page, int size, VisitStatus status, YearMonth yearMonth) {
    Pageable pageable = PageRequest.of(page, size);
    Slice<VisitHistory> slice;

    if (yearMonth != null && status != null) {
      LocalDate startDate = yearMonth.atDay(1);
      LocalDate endDate = yearMonth.atEndOfMonth();
      slice =
          visitHistoryRepository.findByUserIdAndVisitDateBetweenAndStatusWithDetails(
              userId, startDate, endDate, status, pageable);
    } else if (yearMonth != null) {
      LocalDate startDate = yearMonth.atDay(1);
      LocalDate endDate = yearMonth.atEndOfMonth();
      slice =
          visitHistoryRepository.findByUserIdAndVisitDateBetweenAndStatusNotWithDetails(
              userId, startDate, endDate, VisitStatus.CANCELLED, pageable);
    } else if (status != null) {
      slice = visitHistoryRepository.findByUserIdAndStatusWithDetails(userId, status, pageable);
    } else {
      slice =
          visitHistoryRepository.findByUserIdAndStatusNotWithDetails(
              userId, VisitStatus.CANCELLED, pageable);
    }

    return SliceResponse.from(slice.map(this::toVisitResponse));
  }

  @Transactional
  public VisitResponse createVisit(Long userId, VisitCreateRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    Child child =
        childRepository
            .findById(request.childId())
            .orElseThrow(() -> new CustomException(ErrorCode.CHILD_NOT_FOUND));
    if (!child.getUser().getId().equals(userId)) {
      throw new CustomException(ErrorCode.ACCESS_DENIED);
    }
    Facility facility =
        facilityRepository
            .findById(request.facilityId())
            .orElseThrow(() -> new CustomException(ErrorCode.FACILITY_NOT_FOUND));

    if (request.startTime() != null
        && request.endTime() != null
        && request.endTime().isBefore(request.startTime())) {
      throw new CustomException(ErrorCode.INVALID_VISIT_TIME_RANGE);
    }

    VisitHistory visitHistory =
        VisitHistory.builder()
            .user(user)
            .child(child)
            .facility(facility)
            .status(VisitStatus.PLANNED)
            .source(request.source())
            .visitDate(request.visitDate())
            .startTime(request.startTime())
            .endTime(request.endTime())
            .build();

    return toVisitResponse(visitHistoryRepository.save(visitHistory));
  }

  public VisitResponse getVisit(Long userId, Long visitId) {
    VisitHistory visit = getVisitWithOwnerCheck(userId, visitId);
    return toVisitResponse(visit);
  }

  @Transactional
  public VisitResponse updateVisit(Long userId, Long visitId, VisitUpdateRequest request) {
    VisitHistory visit = getVisitWithOwnerCheck(userId, visitId);
    try {
      visit.updateSchedule(request.visitDate(), request.startTime(), request.endTime());
    } catch (IllegalArgumentException e) {
      throw new CustomException(ErrorCode.INVALID_VISIT_TIME_RANGE);
    } catch (IllegalStateException e) {
      throw new CustomException(ErrorCode.VISIT_NOT_EDITABLE);
    }
    return toVisitResponse(visit);
  }

  @Transactional
  public VisitResponse cancelVisit(Long userId, Long visitId) {
    VisitHistory visit = getVisitWithOwnerCheck(userId, visitId);
    try {
      visit.cancel();
    } catch (IllegalStateException e) {
      throw new CustomException(ErrorCode.VISIT_ALREADY_CANCELLED);
    }
    return toVisitResponse(visit);
  }

  @Transactional
  public VisitResponse confirmVisit(Long userId, Long visitId, VisitConfirmRequest request) {
    VisitHistory visit = getVisitWithOwnerCheck(userId, visitId);

    if (visit.getStatus() == VisitStatus.CONFIRMED) {
      throw new CustomException(ErrorCode.VISIT_ALREADY_CONFIRMED);
    }
    if (visit.getStatus() == VisitStatus.CANCELLED) {
      throw new CustomException(ErrorCode.VISIT_ALREADY_CANCELLED);
    }

    try {
      visit.confirm(request.visitDate(), request.startTime(), request.endTime());
    } catch (IllegalArgumentException e) {
      throw new CustomException(ErrorCode.INVALID_VISIT_TIME_RANGE);
    }
    return toVisitResponse(visit);
  }

  public List<VisitSummaryResponse> getRecentVisits(Long userId, int limit) {
    Pageable pageable = PageRequest.of(0, limit);
    List<VisitStatus> statuses = List.of(VisitStatus.CONFIRMED, VisitStatus.PLANNED);
    Slice<VisitHistory> slice =
        visitHistoryRepository.findByUserIdAndStatusInWithDetails(userId, statuses, pageable);
    return slice.getContent().stream().map(this::toVisitSummaryResponse).toList();
  }

  private VisitHistory getVisitWithOwnerCheck(Long userId, Long visitId) {
    VisitHistory visit =
        visitHistoryRepository
            .findById(visitId)
            .orElseThrow(() -> new CustomException(ErrorCode.VISIT_NOT_FOUND));

    if (!visit.getUser().getId().equals(userId)) {
      throw new CustomException(ErrorCode.ACCESS_DENIED);
    }

    return visit;
  }

  private VisitResponse toVisitResponse(VisitHistory visit) {
    return new VisitResponse(
        visit.getId(),
        visit.getFacility().getId(),
        visit.getFacility().getFacilityName(),
        visit.getChild().getId(),
        visit.getChild().getName(),
        visit.getStatus(),
        visit.getSource(),
        visit.getVisitDate(),
        visit.getStartTime(),
        visit.getEndTime(),
        calculateDurationMinutes(visit.getStartTime(), visit.getEndTime()),
        visit.getConfirmedAt(),
        visit.getCancelledAt(),
        visit.getCreatedAt());
  }

  private Long calculateDurationMinutes(LocalTime startTime, LocalTime endTime) {
    if (startTime == null || endTime == null || endTime.isBefore(startTime)) {
      return null;
    }
    return ChronoUnit.MINUTES.between(startTime, endTime);
  }

  private VisitSummaryResponse toVisitSummaryResponse(VisitHistory visit) {
    return new VisitSummaryResponse(
        visit.getId(),
        visit.getFacility().getId(),
        visit.getFacility().getFacilityName(),
        visit.getFacility().getServiceType().getDescription(),
        visit.getVisitDate(),
        visit.getStatus());
  }
}
