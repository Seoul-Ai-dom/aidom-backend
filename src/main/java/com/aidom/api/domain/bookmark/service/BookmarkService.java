package com.aidom.api.domain.bookmark.service;

import com.aidom.api.domain.bookmark.dto.BookmarkResponse;
import com.aidom.api.domain.bookmark.dto.BookmarkStatusResponse;
import com.aidom.api.domain.bookmark.entity.Bookmark;
import com.aidom.api.domain.bookmark.enums.BookmarkStatus;
import com.aidom.api.domain.bookmark.repository.BookmarkRepository;
import com.aidom.api.domain.facility.entity.Facility;
import com.aidom.api.domain.facility.entity.FacilityExternalInfo;
import com.aidom.api.domain.facility.entity.FacilityStats;
import com.aidom.api.domain.facility.repository.FacilityRepository;
import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.repository.UserRepository;
import com.aidom.api.global.common.dto.SliceResponse;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

  private final BookmarkRepository bookmarkRepository;
  private final UserRepository userRepository;
  private final FacilityRepository facilityRepository;

  public SliceResponse<BookmarkResponse> getMyBookmarks(Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Slice<BookmarkResponse> slice =
        bookmarkRepository
            .findByUserIdAndStatusWithFacility(userId, BookmarkStatus.ACTIVE, pageable)
            .map(this::toBookmarkResponse);
    return SliceResponse.from(slice);
  }

  @Transactional
  public void addBookmark(Long userId, String facilityId) {
    Optional<Bookmark> existing = bookmarkRepository.findByUserIdAndFacilityId(userId, facilityId);

    if (existing.isPresent()) {
      Bookmark bookmark = existing.get();
      if (bookmark.isActive()) {
        throw new CustomException(ErrorCode.ALREADY_BOOKMARKED);
      }
      bookmark.reactivate();
      return;
    }

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    Facility facility =
        facilityRepository
            .findById(facilityId)
            .orElseThrow(() -> new CustomException(ErrorCode.FACILITY_NOT_FOUND));

    bookmarkRepository.save(Bookmark.of(user, facility));
  }

  @Transactional
  public void removeBookmark(Long userId, String facilityId) {
    Bookmark bookmark =
        bookmarkRepository
            .findByUserIdAndFacilityId(userId, facilityId)
            .filter(Bookmark::isActive)
            .orElseThrow(() -> new CustomException(ErrorCode.BOOKMARK_NOT_FOUND));

    bookmark.cancel();
  }

  public BookmarkStatusResponse getBookmarkStatus(Long userId, String facilityId) {
    boolean bookmarked =
        bookmarkRepository.existsByUserIdAndFacilityIdAndStatus(
            userId, facilityId, BookmarkStatus.ACTIVE);
    return new BookmarkStatusResponse(bookmarked);
  }

  private BookmarkResponse toBookmarkResponse(Bookmark bookmark) {
    Facility facility = bookmark.getFacility();
    FacilityStats stats = facility.getStats();
    FacilityExternalInfo externalInfo = facility.getExternalInfo();

    BigDecimal avgRating = stats != null ? stats.getAvgRating() : null;
    String thumbnailUrl = externalInfo != null ? externalInfo.getThumbnailUrl() : null;

    return new BookmarkResponse(
        bookmark.getId(),
        facility.getId(),
        facility.getFacilityName(),
        facility.getServiceType().getDescription(),
        facility.getAddress(),
        avgRating,
        thumbnailUrl,
        bookmark.getCreatedAt());
  }
}
