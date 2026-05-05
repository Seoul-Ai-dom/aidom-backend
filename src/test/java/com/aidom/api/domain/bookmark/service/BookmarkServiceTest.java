package com.aidom.api.domain.bookmark.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.aidom.api.domain.bookmark.dto.BookmarkResponse;
import com.aidom.api.domain.bookmark.dto.BookmarkStatusResponse;
import com.aidom.api.domain.bookmark.entity.Bookmark;
import com.aidom.api.domain.bookmark.enums.BookmarkStatus;
import com.aidom.api.domain.bookmark.repository.BookmarkRepository;
import com.aidom.api.domain.facility.entity.Facility;
import com.aidom.api.domain.facility.entity.FacilityExternalInfo;
import com.aidom.api.domain.facility.entity.FacilityStats;
import com.aidom.api.domain.facility.enums.ServiceType;
import com.aidom.api.domain.facility.repository.FacilityRepository;
import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.repository.UserRepository;
import com.aidom.api.global.common.dto.SliceResponse;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
class BookmarkServiceTest {

  @Mock private BookmarkRepository bookmarkRepository;
  @Mock private UserRepository userRepository;
  @Mock private FacilityRepository facilityRepository;

  @InjectMocks private BookmarkService bookmarkService;

  @Test
  @DisplayName("찜 등록 - 신규 등록 성공")
  void addBookmark_newBookmark_success() {
    Long userId = 1L;
    String facilityId = "FAC001";
    User user = createUser(userId);
    Facility facility = createFacility(facilityId);

    given(bookmarkRepository.findByUserIdAndFacilityId(userId, facilityId))
        .willReturn(Optional.empty());
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(facilityRepository.findById(facilityId)).willReturn(Optional.of(facility));

    bookmarkService.addBookmark(userId, facilityId);

    verify(bookmarkRepository).save(any(Bookmark.class));
  }

  @Test
  @DisplayName("찜 등록 - 이미 ACTIVE인 경우 ALREADY_BOOKMARKED 예외")
  void addBookmark_alreadyActive_throwsException() {
    Long userId = 1L;
    String facilityId = "FAC001";
    Bookmark activeBookmark = Bookmark.of(createUser(userId), createFacility(facilityId));

    given(bookmarkRepository.findByUserIdAndFacilityId(userId, facilityId))
        .willReturn(Optional.of(activeBookmark));

    assertThatThrownBy(() -> bookmarkService.addBookmark(userId, facilityId))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.ALREADY_BOOKMARKED);
  }

  @Test
  @DisplayName("찜 등록 - CANCELLED 상태에서 재활성화")
  void addBookmark_cancelledBookmark_reactivates() {
    Long userId = 1L;
    String facilityId = "FAC001";
    Bookmark cancelledBookmark = Bookmark.of(createUser(userId), createFacility(facilityId));
    cancelledBookmark.cancel();

    given(bookmarkRepository.findByUserIdAndFacilityId(userId, facilityId))
        .willReturn(Optional.of(cancelledBookmark));

    bookmarkService.addBookmark(userId, facilityId);

    assertThat(cancelledBookmark.isActive()).isTrue();
  }

  @Test
  @DisplayName("찜 취소 - 성공")
  void removeBookmark_success() {
    Long userId = 1L;
    String facilityId = "FAC001";
    Bookmark bookmark = Bookmark.of(createUser(userId), createFacility(facilityId));

    given(bookmarkRepository.findByUserIdAndFacilityId(userId, facilityId))
        .willReturn(Optional.of(bookmark));

    bookmarkService.removeBookmark(userId, facilityId);

    assertThat(bookmark.isActive()).isFalse();
  }

  @Test
  @DisplayName("찜 목록 조회 - 성공")
  void getMyBookmarks_success() {
    Long userId = 1L;
    Facility facility = createFacility("FAC001");
    Bookmark bookmark = Bookmark.of(createUser(userId), facility);
    SliceImpl<Bookmark> slice = new SliceImpl<>(List.of(bookmark), PageRequest.of(0, 20), false);

    given(
            bookmarkRepository.findByUserIdAndStatusWithFacility(
                userId, BookmarkStatus.ACTIVE, PageRequest.of(0, 20)))
        .willReturn(slice);

    SliceResponse<BookmarkResponse> result = bookmarkService.getMyBookmarks(userId, 0, 20);

    assertThat(result.content()).hasSize(1);
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  @DisplayName("찜 목록 조회 - avgRating과 thumbnailUrl이 정상 매핑된다")
  void getMyBookmarks_mapsAvgRatingAndThumbnail() {
    Long userId = 1L;
    Facility facility = createFacility("FAC001");
    FacilityStats stats =
        FacilityStats.builder()
            .facility(facility)
            .avgRating(new BigDecimal("4.5"))
            .avgRatingSafety(new BigDecimal("4.0"))
            .avgRatingCleanliness(new BigDecimal("4.5"))
            .avgRatingManagement(new BigDecimal("4.5"))
            .avgRatingKindness(new BigDecimal("5.0"))
            .reviewCount(10)
            .build();
    FacilityExternalInfo externalInfo =
        FacilityExternalInfo.builder()
            .facility(facility)
            .thumbnailUrl("https://example.com/thumb.jpg")
            .syncedAt(LocalDateTime.now())
            .build();
    ReflectionTestUtils.setField(facility, "stats", stats);
    ReflectionTestUtils.setField(facility, "externalInfo", externalInfo);

    Bookmark bookmark = Bookmark.of(createUser(userId), facility);
    SliceImpl<Bookmark> slice = new SliceImpl<>(List.of(bookmark), PageRequest.of(0, 20), false);

    given(
            bookmarkRepository.findByUserIdAndStatusWithFacility(
                userId, BookmarkStatus.ACTIVE, PageRequest.of(0, 20)))
        .willReturn(slice);

    SliceResponse<BookmarkResponse> result = bookmarkService.getMyBookmarks(userId, 0, 20);

    BookmarkResponse response = result.content().get(0);
    assertThat(response.avgRating()).isEqualByComparingTo(new BigDecimal("4.5"));
    assertThat(response.thumbnailUrl()).isEqualTo("https://example.com/thumb.jpg");
  }

  @Test
  @DisplayName("찜 여부 확인 - bookmarked true")
  void getBookmarkStatus_bookmarked() {
    Long userId = 1L;
    String facilityId = "FAC001";

    given(
            bookmarkRepository.existsByUserIdAndFacilityIdAndStatus(
                userId, facilityId, BookmarkStatus.ACTIVE))
        .willReturn(true);

    BookmarkStatusResponse result = bookmarkService.getBookmarkStatus(userId, facilityId);

    assertThat(result.bookmarked()).isTrue();
  }

  private User createUser(Long userId) {
    return User.builder()
        .name("테스트")
        .email("test@test.com")
        .provider(com.aidom.api.domain.user.enums.Provider.KAKAO)
        .providerId("12345")
        .role(com.aidom.api.domain.user.enums.Role.USER)
        .status(com.aidom.api.domain.user.enums.UserStatus.ACTIVE)
        .birthDate(java.time.LocalDate.of(1990, 1, 1))
        .phone("010-1234-5678")
        .district("강남구")
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
}
