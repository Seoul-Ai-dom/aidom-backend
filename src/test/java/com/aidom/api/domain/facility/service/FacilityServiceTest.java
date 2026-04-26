package com.aidom.api.domain.facility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

import com.aidom.api.domain.facility.document.FacilityDocument;
import com.aidom.api.domain.facility.dto.*;
import com.aidom.api.domain.facility.entity.Facility;
import com.aidom.api.domain.facility.enums.ServiceType;
import com.aidom.api.domain.facility.repository.FacilityRepository;
import com.aidom.api.domain.facility.repository.FacilitySearchRepository;
import com.aidom.api.domain.user.entity.Child;
import com.aidom.api.domain.user.repository.ChildRepository;
import com.aidom.api.global.common.entity.Gender;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

  @Mock private FacilitySearchRepository facilitySearchRepository;
  @Mock private FacilityRepository facilityRepository;
  @Mock private ChildRepository childRepository;

  @InjectMocks private FacilityService facilityService;

  @Test
  @DisplayName("키워드 검색 시 ES 결과를 FacilitySearchResponse로 매핑한다")
  void searchFacilities_mapsResult() {
    FacilityDocument doc = createDocument("FAC001", "강남 키움센터");
    Page<FacilityDocument> page = new PageImpl<>(List.of(doc), PageRequest.of(0, 10), 1);
    given(facilitySearchRepository.searchByFacilityName(eq("키움"), any(Pageable.class)))
        .willReturn(page);

    Page<FacilitySearchResponse> result =
        facilityService.searchFacilities("키움", PageRequest.of(0, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).id()).isEqualTo("FAC001");
    assertThat(result.getContent().get(0).facilityName()).isEqualTo("강남 키움센터");
  }

  @Test
  @DisplayName("필터 조건으로 시설 목록을 조회한다")
  void listFacilities_withFilters() {
    FacilityDocument doc = createDocument("FAC001", "강남 키움센터");
    Page<FacilityDocument> page = new PageImpl<>(List.of(doc), PageRequest.of(0, 10), 1);
    given(
            facilitySearchRepository.searchWithFilters(
                eq("강남구"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)))
        .willReturn(page);

    Page<FacilityListResponse> result =
        facilityService.listFacilities(
            "강남구", null, null, null, null, null, null, null, null, null, PageRequest.of(0, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).districtName()).isEqualTo("강남구");
  }

  @Test
  @DisplayName("시설 상세 조회 성공")
  void getFacility_success() {
    Facility facility = createFacility("FAC001");
    given(facilityRepository.findById("FAC001")).willReturn(Optional.of(facility));

    FacilityDetailResponse result = facilityService.getFacility("FAC001");

    assertThat(result.id()).isEqualTo("FAC001");
    assertThat(result.facilityName()).isEqualTo("테스트 센터");
  }

  @Test
  @DisplayName("존재하지 않는 시설 상세 조회 시 FACILITY_NOT_FOUND 예외 발생")
  void getFacility_notFound() {
    given(facilityRepository.findById("INVALID")).willReturn(Optional.empty());

    assertThatThrownBy(() -> facilityService.getFacility("INVALID"))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.FACILITY_NOT_FOUND);
  }

  @Test
  @DisplayName("아이 나이 기반으로 시설을 추천한다")
  void recommendFacilities_mapsResult() {
    Child child =
        Child.builder()
            .name("테스트")
            .birthDate(LocalDate.now().minusYears(7))
            .gender(Gender.MALE)
            .build();
    given(childRepository.findById(1L)).willReturn(Optional.of(child));

    FacilityDocument doc = createDocument("FAC001", "강남 키움센터");
    given(facilitySearchRepository.recommendByChildAge(eq(7), isNull(), isNull(), eq(5)))
        .willReturn(List.of(doc));

    List<FacilityRecommendResponse> result = facilityService.recommendFacilities(1L, null, null, 5);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo("FAC001");
    assertThat(result.get(0).recommendReason()).contains("7세");
  }

  @Test
  @DisplayName("존재하지 않는 아이 ID로 추천 요청 시 CHILD_NOT_FOUND 예외 발생")
  void recommendFacilities_childNotFound() {
    given(childRepository.findById(999L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> facilityService.recommendFacilities(999L, null, null, 5))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.CHILD_NOT_FOUND);
  }

  @Test
  @DisplayName("주변 시설을 조회한다")
  void getNearbyFacilities_returnsResults() {
    FacilityDocument doc = createDocument("FAC001", "강남 키움센터");
    given(facilitySearchRepository.searchNearby(eq(37.5), eq(127.0), eq(3.0), eq(10)))
        .willReturn(List.of(doc));

    List<FacilityListResponse> result =
        facilityService.getNearbyFacilities(
            new BigDecimal("37.5"), new BigDecimal("127.0"), new BigDecimal("3.0"), 10);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo("FAC001");
  }

  @Test
  @DisplayName("필터 옵션을 반환한다")
  void getFilters_returnsFilterOptions() {
    given(facilitySearchRepository.getDistinctDistrictNames()).willReturn(List.of("강남구", "서초구"));

    FacilityFilterResponse result = facilityService.getFilters();

    assertThat(result.serviceTypes()).hasSize(ServiceType.values().length);
    assertThat(result.serviceTypes())
        .extracting(FacilityFilterResponse.FilterOption::value)
        .contains("우리동네키움센터", "지역아동센터");
    assertThat(result.districts()).hasSize(2);
    assertThat(result.ageRanges()).isNotEmpty();
    assertThat(result.careTypes()).isNotEmpty();
  }

  private FacilityDocument createDocument(String id, String name) {
    return FacilityDocument.builder()
        .id(id)
        .facilityName(name)
        .serviceType("우리동네키움센터")
        .districtName("강남구")
        .address("서울특별시 강남구 테헤란로 123")
        .location(new GeoPoint(37.5665, 126.978))
        .ageMin(3)
        .ageMax(12)
        .isFree(true)
        .bookingRequired(false)
        .hasRegularCare(true)
        .hasTemporaryCare(false)
        .avgRating(4.5f)
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
        .lat(new BigDecimal("37.5665"))
        .lng(new BigDecimal("126.978"))
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
