package com.aidom.api.domain.facility.service;

import com.aidom.api.domain.bookmark.enums.BookmarkStatus;
import com.aidom.api.domain.bookmark.repository.BookmarkRepository;
import com.aidom.api.domain.facility.document.FacilityDocument;
import com.aidom.api.domain.facility.dto.FacilityDetailResponse;
import com.aidom.api.domain.facility.dto.FacilityFilterResponse;
import com.aidom.api.domain.facility.dto.FacilityListResponse;
import com.aidom.api.domain.facility.dto.FacilityRecommendResponse;
import com.aidom.api.domain.facility.dto.FacilitySearchResponse;
import com.aidom.api.domain.facility.entity.Facility;
import com.aidom.api.domain.facility.entity.FacilityExternalInfo;
import com.aidom.api.domain.facility.entity.FacilityStats;
import com.aidom.api.domain.facility.enums.ServiceType;
import com.aidom.api.domain.facility.repository.FacilityRepository;
import com.aidom.api.domain.facility.repository.FacilitySearchRepository;
import com.aidom.api.domain.user.entity.Child;
import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.service.ChildService;
import com.aidom.api.domain.visit.enums.VisitStatus;
import com.aidom.api.domain.visit.repository.VisitHistoryRepository;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ConditionalOnProperty(
    name = "spring.data.elasticsearch.repositories.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class FacilityService {

  private final FacilitySearchRepository facilitySearchRepository;
  private final FacilityRepository facilityRepository;
  private final ChildService childService;
  private final BookmarkRepository bookmarkRepository;
  private final VisitHistoryRepository visitHistoryRepository;

  public Page<FacilityListResponse> listFacilities(
      String districtName,
      String serviceType,
      Boolean isFree,
      Integer ageMin,
      Integer ageMax,
      BigDecimal lat,
      BigDecimal lng,
      BigDecimal radius,
      String careType,
      Boolean hasRegularProgram,
      Pageable pageable) {

    Page<FacilityDocument> page =
        facilitySearchRepository.searchWithFilters(
            districtName,
            serviceType,
            isFree,
            ageMin,
            ageMax,
            lat,
            lng,
            radius,
            careType,
            hasRegularProgram,
            pageable);

    return page.map(this::toListResponse);
  }

  public Page<FacilitySearchResponse> searchFacilities(String keyword, Pageable pageable) {
    Page<FacilityDocument> page = facilitySearchRepository.searchByFacilityName(keyword, pageable);

    return page.map(this::toSearchResponse);
  }

  public FacilityDetailResponse getFacility(String facilityId) {
    Facility facility =
        facilityRepository
            .findById(facilityId)
            .orElseThrow(() -> new CustomException(ErrorCode.FACILITY_NOT_FOUND));

    return toDetailResponse(facility);
  }

  public List<FacilityRecommendResponse> recommendFacilities(
      Long childId, BigDecimal lat, BigDecimal lng, int limit) {

    Child child = childService.getChildById(childId);
    User user = child.getUser();
    int childAge = calculateAge(child.getBirthDate());

    // 찜한 시설의 서비스 유형 조회
    Set<String> preferredServiceTypes =
        bookmarkRepository
            .findDistinctServiceTypesByUserId(user.getId(), BookmarkStatus.ACTIVE)
            .stream()
            .map(ServiceType::getDescription)
            .collect(Collectors.toSet());

    // 방문한 시설 ID 조회 (제외 대상)
    List<String> visitedFacilityIds =
        visitHistoryRepository.findFacilityIdsByUserId(user.getId(), VisitStatus.CANCELLED);
    Set<String> excludeFacilityIds = new HashSet<>(visitedFacilityIds);

    // 위치 미제공 시 사용자 주소 기본값 사용
    Double latVal = lat != null ? lat.doubleValue() : null;
    Double lngVal = lng != null ? lng.doubleValue() : null;
    if (latVal == null && user.getAddressLat() != null) {
      latVal = user.getAddressLat().doubleValue();
    }
    if (lngVal == null && user.getAddressLng() != null) {
      lngVal = user.getAddressLng().doubleValue();
    }

    String userDistrict = user.getDistrict();

    List<FacilityDocument> docs =
        facilitySearchRepository.recommendByChildAge(
            childAge,
            latVal,
            lngVal,
            excludeFacilityIds,
            preferredServiceTypes,
            userDistrict,
            limit);

    return docs.stream()
        .map(doc -> toRecommendResponse(doc, childAge, userDistrict, preferredServiceTypes))
        .toList();
  }

  public List<FacilityListResponse> getNearbyFacilities(
      BigDecimal lat, BigDecimal lng, BigDecimal radius, int limit) {

    List<FacilityDocument> docs =
        facilitySearchRepository.searchNearby(
            lat.doubleValue(), lng.doubleValue(), radius.doubleValue(), limit);

    return docs.stream().map(this::toListResponse).toList();
  }

  public FacilityFilterResponse getFilters() {
    List<FacilityFilterResponse.FilterOption> serviceTypes =
        Arrays.stream(ServiceType.values())
            .map(
                st ->
                    new FacilityFilterResponse.FilterOption(
                        st.getDescription(), st.getDescription()))
            .toList();

    List<String> districtNames = facilitySearchRepository.getDistinctDistrictNames();
    List<FacilityFilterResponse.FilterOption> districts =
        districtNames.stream()
            .sorted()
            .map(name -> new FacilityFilterResponse.FilterOption(name, name))
            .toList();

    List<FacilityFilterResponse.FilterOption> ageRanges =
        List.of(
            new FacilityFilterResponse.FilterOption("0~2세", "0-2"),
            new FacilityFilterResponse.FilterOption("3~5세", "3-5"),
            new FacilityFilterResponse.FilterOption("6~9세", "6-9"),
            new FacilityFilterResponse.FilterOption("10~12세", "10-12"),
            new FacilityFilterResponse.FilterOption("13~18세", "13-18"));

    List<FacilityFilterResponse.FilterOption> careTypes =
        List.of(
            new FacilityFilterResponse.FilterOption("임시 돌봄", "TEMPORARY"),
            new FacilityFilterResponse.FilterOption("정규 돌봄", "REGULAR"));

    return new FacilityFilterResponse(serviceTypes, districts, ageRanges, careTypes);
  }

  private int calculateAge(LocalDate birthDate) {
    return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
  }

  private FacilityListResponse toListResponse(FacilityDocument doc) {
    return new FacilityListResponse(
        doc.getId(),
        doc.getFacilityName(),
        doc.getServiceType(),
        doc.getDistrictName(),
        doc.getAddress(),
        doc.getLocation() != null ? BigDecimal.valueOf(doc.getLocation().getLat()) : null,
        doc.getLocation() != null ? BigDecimal.valueOf(doc.getLocation().getLon()) : null,
        BigDecimal.valueOf(doc.getAvgRating()),
        null,
        doc.isFree(),
        doc.isBookingRequired());
  }

  private FacilitySearchResponse toSearchResponse(FacilityDocument doc) {
    return new FacilitySearchResponse(
        doc.getId(),
        doc.getFacilityName(),
        doc.getServiceType(),
        doc.getDistrictName(),
        doc.getAddress(),
        BigDecimal.valueOf(doc.getAvgRating()),
        null);
  }

  private FacilityDetailResponse toDetailResponse(Facility entity) {
    FacilityExternalInfo ext = entity.getExternalInfo();
    FacilityStats stats = entity.getStats();

    return new FacilityDetailResponse(
        entity.getId(),
        entity.getFacilityName(),
        entity.getServiceType().getDescription(),
        entity.getServiceTypeCode(),
        entity.getDistrictName(),
        entity.getDistrictCode(),
        entity.getAddress(),
        entity.getLat(),
        entity.getLng(),
        entity.getAgeGroup(),
        entity.getAgeMin(),
        entity.getAgeMax(),
        entity.isBookingRequired(),
        entity.isFree(),
        entity.getFee(),
        entity.getMonthlyFee(),
        entity.getCapacityRegular(),
        entity.getCapacityTemporary(),
        entity.getAreaSqm(),
        entity.getOperatingDays(),
        entity.getClosedDays(),
        entity.isHasRegularProgram(),
        entity.isHasRegularCare(),
        entity.isHasTemporaryCare(),
        ext != null ? ext.getPhone() : null,
        ext != null ? ext.getWebsite() : null,
        ext != null ? ext.getNaverHours() : null,
        ext != null ? ext.getBusinessStatus() : null,
        ext != null ? ext.getThumbnailUrl() : null,
        stats != null ? stats.getAvgRating() : BigDecimal.ZERO,
        stats != null ? stats.getAvgRatingSafety() : BigDecimal.ZERO,
        stats != null ? stats.getAvgRatingCleanliness() : BigDecimal.ZERO,
        stats != null ? stats.getAvgRatingManagement() : BigDecimal.ZERO,
        stats != null ? stats.getAvgRatingKindness() : BigDecimal.ZERO,
        stats != null ? stats.getReviewCount() : 0);
  }

  private FacilityRecommendResponse toRecommendResponse(
      FacilityDocument doc, int childAge, String userDistrict, Set<String> preferredServiceTypes) {

    List<String> tags = new ArrayList<>();
    String reason =
        String.format("%d세 아이에게 적합한 시설이에요 (%d~%d세 대상)", childAge, doc.getAgeMin(), doc.getAgeMax());

    if (userDistrict != null && userDistrict.equals(doc.getDistrictName())) {
      reason = String.format("우리 동네(%s)에 있는 추천 시설이에요", userDistrict);
      tags.add("우리동네");
    }

    if (preferredServiceTypes != null && preferredServiceTypes.contains(doc.getServiceType())) {
      if (tags.isEmpty()) {
        reason = "찜한 시설과 비슷한 유형이에요";
      }
      tags.add("관심유형");
    }

    if (doc.isFree()) {
      if (tags.isEmpty()) {
        reason = "무료로 이용 가능해요";
      }
      tags.add("무료");
    }

    if (doc.getAvgRating() >= 4.0) {
      tags.add("인기");
    }

    return new FacilityRecommendResponse(
        doc.getId(),
        doc.getFacilityName(),
        doc.getServiceType(),
        doc.getDistrictName(),
        BigDecimal.valueOf(doc.getAvgRating()),
        null,
        reason,
        tags);
  }
}
