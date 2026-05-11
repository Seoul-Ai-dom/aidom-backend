package com.aidom.api.domain.bookmark.repository;

import com.aidom.api.domain.bookmark.entity.Bookmark;
import com.aidom.api.domain.bookmark.enums.BookmarkStatus;
import com.aidom.api.domain.facility.enums.ServiceType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

  Optional<Bookmark> findByUserIdAndFacilityId(Long userId, String facilityId);

  @Query(
      "SELECT b FROM Bookmark b JOIN FETCH b.facility f"
          + " LEFT JOIN FETCH f.stats"
          + " LEFT JOIN FETCH f.externalInfo"
          + " WHERE b.user.id = :userId AND b.status = :status"
          + " ORDER BY b.createdAt DESC")
  Slice<Bookmark> findByUserIdAndStatusWithFacility(
      @Param("userId") Long userId, @Param("status") BookmarkStatus status, Pageable pageable);

  boolean existsByUserIdAndFacilityIdAndStatus(
      Long userId, String facilityId, BookmarkStatus status);

  @Query("SELECT b.facility.id FROM Bookmark b WHERE b.user.id = :userId AND b.status = :status")
  List<String> findFacilityIdsByUserId(
      @Param("userId") Long userId, @Param("status") BookmarkStatus status);

  @Query(
      "SELECT DISTINCT f.serviceType FROM Bookmark b JOIN b.facility f"
          + " WHERE b.user.id = :userId AND b.status = :status")
  List<ServiceType> findDistinctServiceTypesByUserId(
      @Param("userId") Long userId, @Param("status") BookmarkStatus status);
}
