package com.aidom.api.domain.visit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.aidom.api.domain.facility.entity.Facility;
import com.aidom.api.domain.facility.enums.ServiceType;
import com.aidom.api.domain.facility.repository.FacilityRepository;
import com.aidom.api.domain.user.entity.Child;
import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.domain.user.enums.Role;
import com.aidom.api.domain.user.enums.UserStatus;
import com.aidom.api.domain.user.repository.ChildRepository;
import com.aidom.api.domain.user.repository.UserRepository;
import com.aidom.api.domain.visit.dto.VisitConfirmRequest;
import com.aidom.api.domain.visit.dto.VisitCreateRequest;
import com.aidom.api.domain.visit.dto.VisitResponse;
import com.aidom.api.domain.visit.dto.VisitUpdateRequest;
import com.aidom.api.domain.visit.entity.VisitHistory;
import com.aidom.api.domain.visit.enums.VisitSource;
import com.aidom.api.domain.visit.enums.VisitStatus;
import com.aidom.api.domain.visit.repository.VisitHistoryRepository;
import com.aidom.api.global.common.entity.Gender;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

  @Mock private VisitHistoryRepository visitHistoryRepository;
  @Mock private UserRepository userRepository;
  @Mock private ChildRepository childRepository;
  @Mock private FacilityRepository facilityRepository;

  @InjectMocks private VisitService visitService;

  @Test
  @DisplayName("이용내역 등록 - 성공")
  void createVisit_success() {
    Long userId = 1L;
    User user = createUser();
    Child child = createChild(user);
    Facility facility = createFacility("FAC001");
    VisitCreateRequest request =
        new VisitCreateRequest("FAC001", 1L, VisitSource.MANUAL, null, null, null);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(childRepository.findById(1L)).willReturn(Optional.of(child));
    given(facilityRepository.findById("FAC001")).willReturn(Optional.of(facility));
    given(visitHistoryRepository.save(any(VisitHistory.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    VisitResponse result = visitService.createVisit(userId, request);

    assertThat(result.status()).isEqualTo(VisitStatus.PLANNED);
    assertThat(result.facilityId()).isEqualTo("FAC001");
  }

  @Test
  @DisplayName("이용내역 등록 - 다른 사용자의 아이 ID로 등록 시 ACCESS_DENIED")
  void createVisit_otherUsersChild_throwsAccessDenied() {
    Long userId = 1L;
    User user = createUser();
    User otherUser = createOtherUser();
    Child otherChild = createChild(otherUser);
    VisitCreateRequest request =
        new VisitCreateRequest("FAC001", 2L, VisitSource.MANUAL, null, null, null);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(childRepository.findById(2L)).willReturn(Optional.of(otherChild));

    assertThatThrownBy(() -> visitService.createVisit(userId, request))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.ACCESS_DENIED);
  }

  @Test
  @DisplayName("이용내역 상세 조회 - 성공")
  void getVisit_success() {
    Long userId = 1L;
    VisitHistory visit = createVisitHistory(userId);

    given(visitHistoryRepository.findById(1L)).willReturn(Optional.of(visit));

    VisitResponse result = visitService.getVisit(userId, 1L);

    assertThat(result.status()).isEqualTo(VisitStatus.PLANNED);
  }

  @Test
  @DisplayName("이용내역 수정 - 성공")
  void updateVisit_success() {
    Long userId = 1L;
    VisitHistory visit = createVisitHistory(userId);
    VisitUpdateRequest request =
        new VisitUpdateRequest(LocalDate.of(2025, 7, 1), LocalTime.of(10, 0), LocalTime.of(14, 0));

    given(visitHistoryRepository.findById(1L)).willReturn(Optional.of(visit));

    VisitResponse result = visitService.updateVisit(userId, 1L, request);

    assertThat(result.visitDate()).isEqualTo(LocalDate.of(2025, 7, 1));
    assertThat(result.startTime()).isEqualTo(LocalTime.of(10, 0));
    assertThat(result.endTime()).isEqualTo(LocalTime.of(14, 0));
  }

  @Test
  @DisplayName("이용내역 수정 - 확정된 내역은 수정할 수 없다")
  void updateVisit_confirmedVisit_throwsNotEditable() {
    Long userId = 1L;
    VisitHistory visit = createVisitHistory(userId);
    visit.confirm(LocalDate.of(2025, 6, 1), LocalTime.of(9, 0), LocalTime.of(13, 0));
    VisitUpdateRequest request =
        new VisitUpdateRequest(LocalDate.of(2025, 7, 1), LocalTime.of(10, 0), LocalTime.of(14, 0));

    given(visitHistoryRepository.findById(1L)).willReturn(Optional.of(visit));

    assertThatThrownBy(() -> visitService.updateVisit(userId, 1L, request))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.VISIT_NOT_EDITABLE);
  }

  @Test
  @DisplayName("이용내역 수정 - 종료 시간이 시작 시간보다 빠르면 예외")
  void updateVisit_invalidTimeRange_throwsException() {
    Long userId = 1L;
    VisitHistory visit = createVisitHistory(userId);
    VisitUpdateRequest request =
        new VisitUpdateRequest(LocalDate.of(2025, 7, 1), LocalTime.of(14, 0), LocalTime.of(10, 0));

    given(visitHistoryRepository.findById(1L)).willReturn(Optional.of(visit));

    assertThatThrownBy(() -> visitService.updateVisit(userId, 1L, request))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_VISIT_TIME_RANGE);
  }

  @Test
  @DisplayName("이용내역 취소 - 성공")
  void cancelVisit_success() {
    Long userId = 1L;
    VisitHistory visit = createVisitHistory(userId);

    given(visitHistoryRepository.findById(1L)).willReturn(Optional.of(visit));

    VisitResponse result = visitService.cancelVisit(userId, 1L);

    assertThat(result.status()).isEqualTo(VisitStatus.CANCELLED);
  }

  @Test
  @DisplayName("이용내역 확정 - 성공")
  void confirmVisit_success() {
    Long userId = 1L;
    VisitHistory visit = createVisitHistory(userId);
    VisitConfirmRequest request =
        new VisitConfirmRequest(LocalDate.of(2025, 6, 1), LocalTime.of(9, 0), LocalTime.of(13, 0));

    given(visitHistoryRepository.findById(1L)).willReturn(Optional.of(visit));

    VisitResponse result = visitService.confirmVisit(userId, 1L, request);

    assertThat(result.status()).isEqualTo(VisitStatus.CONFIRMED);
    assertThat(result.visitDate()).isEqualTo(LocalDate.of(2025, 6, 1));
  }

  @Test
  @DisplayName("이용내역 취소 - 이미 취소된 경우 예외")
  void cancelVisit_alreadyCancelled_throwsException() {
    Long userId = 1L;
    VisitHistory visit = createVisitHistory(userId);
    visit.cancel();

    given(visitHistoryRepository.findById(1L)).willReturn(Optional.of(visit));

    assertThatThrownBy(() -> visitService.cancelVisit(userId, 1L))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.VISIT_ALREADY_CANCELLED);
  }

  @Test
  @DisplayName("이용내역 확정 - 이미 확정된 경우 예외")
  void confirmVisit_alreadyConfirmed_throwsException() {
    Long userId = 1L;
    VisitHistory visit = createVisitHistory(userId);
    visit.confirm(LocalDate.of(2025, 6, 1), LocalTime.of(9, 0), LocalTime.of(13, 0));
    VisitConfirmRequest request =
        new VisitConfirmRequest(LocalDate.of(2025, 6, 2), LocalTime.of(10, 0), LocalTime.of(14, 0));

    given(visitHistoryRepository.findById(1L)).willReturn(Optional.of(visit));

    assertThatThrownBy(() -> visitService.confirmVisit(userId, 1L, request))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.VISIT_ALREADY_CONFIRMED);
  }

  @Test
  @DisplayName("이용내역 확정 - 종료 시간이 시작 시간보다 빠르면 예외")
  void confirmVisit_invalidTimeRange_throwsException() {
    Long userId = 1L;
    VisitHistory visit = createVisitHistory(userId);
    VisitConfirmRequest request =
        new VisitConfirmRequest(LocalDate.of(2025, 6, 1), LocalTime.of(13, 0), LocalTime.of(9, 0));

    given(visitHistoryRepository.findById(1L)).willReturn(Optional.of(visit));

    assertThatThrownBy(() -> visitService.confirmVisit(userId, 1L, request))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_VISIT_TIME_RANGE);
  }

  @Test
  @DisplayName("이용내역 목록 조회 - yearMonth와 status를 함께 전달하면 교집합 조건으로 조회한다")
  void getMyVisits_withYearMonthAndStatus_usesIntersectionFilter() {
    Long userId = 1L;
    VisitHistory visit = createVisitHistory(userId);
    SliceImpl<VisitHistory> slice = new SliceImpl<>(List.of(visit), PageRequest.of(0, 20), false);

    given(
            visitHistoryRepository.findByUserIdAndVisitDateBetweenAndStatusWithDetails(
                userId,
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                VisitStatus.PLANNED,
                PageRequest.of(0, 20)))
        .willReturn(slice);

    var result =
        visitService.getMyVisits(userId, 0, 20, VisitStatus.PLANNED, YearMonth.of(2025, 6));

    assertThat(result.content()).hasSize(1);
    assertThat(result.content().get(0).status()).isEqualTo(VisitStatus.PLANNED);
  }

  private User createUser() {
    User user =
        User.builder()
            .name("테스트")
            .email("test@test.com")
            .provider(Provider.KAKAO)
            .providerId("12345")
            .role(Role.USER)
            .status(UserStatus.ACTIVE)
            .birthDate(LocalDate.of(1990, 1, 1))
            .phone("010-1234-5678")
            .district("강남구")
            .build();
    ReflectionTestUtils.setField(user, "id", 1L);
    return user;
  }

  private User createOtherUser() {
    User user =
        User.builder()
            .name("다른사용자")
            .email("other@test.com")
            .provider(Provider.KAKAO)
            .providerId("99999")
            .role(Role.USER)
            .status(UserStatus.ACTIVE)
            .birthDate(LocalDate.of(1985, 5, 5))
            .phone("010-9999-9999")
            .district("서초구")
            .build();
    ReflectionTestUtils.setField(user, "id", 2L);
    return user;
  }

  private Child createChild(User user) {
    return Child.builder()
        .user(user)
        .name("아이")
        .birthDate(LocalDate.of(2020, 1, 1))
        .gender(Gender.MALE)
        .isPrimary(true)
        .build();
  }

  private Facility createFacility(String id) {
    return Facility.builder()
        .id(id)
        .facilityName("테스트 센터")
        .serviceTypeCode("A")
        .serviceType(ServiceType.CHILD_CENTER)
        .districtCode("11680")
        .districtName("강남구")
        .address("서울특별시 강남구 테헤란로 123")
        .ageGroup("3~12세")
        .ageMin(3)
        .ageMax(12)
        .bookingRequired(false)
        .isFree(true)
        .hasRegularProgram(false)
        .hasRegularCare(true)
        .hasTemporaryCare(false)
        .build();
  }

  private VisitHistory createVisitHistory(Long userId) {
    User user = createUser();
    Child child = createChild(user);
    Facility facility = createFacility("FAC001");
    return VisitHistory.builder()
        .user(user)
        .child(child)
        .facility(facility)
        .status(VisitStatus.PLANNED)
        .source(VisitSource.MANUAL)
        .build();
  }
}
