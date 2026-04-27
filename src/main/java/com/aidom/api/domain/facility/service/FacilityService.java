package com.aidom.api.domain.facility.service;

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
import com.aidom.api.domain.user.service.ChildService;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
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

    int childAge = calculateAge(child.getBirthDate());

    Double latVal = lat != null ? lat.doubleValue() : null;
    Double lngVal = lng != null ? lng.doubleValue() : null;

    List<FacilityDocument> docs =
        facilitySearchRepository.recommendByChildAge(childAge, latVal, lngVal, limit);

    return docs.stream().map(doc -> toRecommendResponse(doc, childAge)).toList();
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

  private FacilityRecommendResponse toRecommendResponse(FacilityDocument doc, int childAge) {
    String reason =
        String.format("%d세 아이에게 적합한 시설이에요 (%d~%d세 대상)", childAge, doc.getAgeMin(), doc.getAgeMax());

    return new FacilityRecommendResponse(
        doc.getId(),
        doc.getFacilityName(),
        doc.getServiceType(),
        doc.getDistrictName(),
        BigDecimal.valueOf(doc.getAvgRating()),
        null,
        reason);
  }
}
