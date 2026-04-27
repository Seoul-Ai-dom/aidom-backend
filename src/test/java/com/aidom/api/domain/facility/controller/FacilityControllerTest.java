package com.aidom.api.domain.facility.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aidom.api.domain.facility.dto.FacilityDetailResponse;
import com.aidom.api.domain.facility.dto.FacilityFilterResponse;
import com.aidom.api.domain.facility.dto.FacilityListResponse;
import com.aidom.api.domain.facility.dto.FacilityRecommendResponse;
import com.aidom.api.domain.facility.dto.FacilitySearchResponse;
import com.aidom.api.domain.facility.service.FacilityService;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FacilityController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.data.elasticsearch.repositories.enabled=true")
class FacilityControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private FacilityService facilityService;

  @Test
  @DisplayName("GET /api/v1/facilities - 시설 목록 조회")
  void getFacilities() throws Exception {
    FacilityListResponse response =
        new FacilityListResponse(
            "FAC001",
            "강남 키움센터",
            "우리동네키움센터",
            "강남구",
            "서울특별시 강남구 테헤란로 123",
            new BigDecimal("37.5665"),
            new BigDecimal("126.978"),
            new BigDecimal("4.5"),
            null,
            true,
            false);

    given(
            facilityService.listFacilities(
                isNull(),
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
        .willReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

    mockMvc
        .perform(get("/api/v1/facilities"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value("FAC001"))
        .andExpect(jsonPath("$.content[0].facilityName").value("강남 키움센터"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  @DisplayName("GET /api/v1/facilities/search - 시설 검색")
  void searchFacilities() throws Exception {
    FacilitySearchResponse response =
        new FacilitySearchResponse(
            "FAC001",
            "강남 키움센터",
            "우리동네키움센터",
            "강남구",
            "서울특별시 강남구 테헤란로 123",
            new BigDecimal("4.5"),
            null);

    given(facilityService.searchFacilities(eq("키움"), any(Pageable.class)))
        .willReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

    mockMvc
        .perform(get("/api/v1/facilities/search").param("keyword", "키움"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value("FAC001"));
  }

  @Test
  @DisplayName("GET /api/v1/facilities/{facilityId} - 시설 상세 조회")
  void getFacility() throws Exception {
    FacilityDetailResponse response =
        new FacilityDetailResponse(
            "FAC001",
            "강남 키움센터",
            "우리동네키움센터",
            "KIUM_CENTER",
            "강남구",
            "11680",
            "서울특별시 강남구 테헤란로 123",
            new BigDecimal("37.5665"),
            new BigDecimal("126.978"),
            "3~12세",
            3,
            12,
            false,
            true,
            null,
            null,
            30,
            10,
            new BigDecimal("120.50"),
            "월~금",
            "주말",
            false,
            true,
            false,
            "02-1234-5678",
            null,
            null,
            null,
            null,
            new BigDecimal("4.5"),
            new BigDecimal("4.3"),
            new BigDecimal("4.4"),
            new BigDecimal("4.2"),
            new BigDecimal("4.6"),
            42);

    given(facilityService.getFacility("FAC001")).willReturn(response);

    mockMvc
        .perform(get("/api/v1/facilities/FAC001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("FAC001"))
        .andExpect(jsonPath("$.facilityName").value("강남 키움센터"))
        .andExpect(jsonPath("$.reviewCount").value(42));
  }

  @Test
  @DisplayName("GET /api/v1/facilities/{facilityId} - 존재하지 않는 시설 404")
  void getFacility_notFound() throws Exception {
    given(facilityService.getFacility("INVALID"))
        .willThrow(new CustomException(ErrorCode.FACILITY_NOT_FOUND));

    mockMvc.perform(get("/api/v1/facilities/INVALID")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/v1/facilities/recommend - AI 추천")
  void recommendFacilities() throws Exception {
    FacilityRecommendResponse response =
        new FacilityRecommendResponse(
            "FAC001",
            "강남 키움센터",
            "우리동네키움센터",
            "강남구",
            new BigDecimal("4.5"),
            null,
            "7세 아이에게 적합한 시설이에요 (3~12세 대상)");

    given(facilityService.recommendFacilities(eq(1L), isNull(), isNull(), eq(5)))
        .willReturn(List.of(response));

    mockMvc
        .perform(get("/api/v1/facilities/recommend").param("childId", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("FAC001"))
        .andExpect(jsonPath("$[0].recommendReason").exists());
  }

  @Test
  @DisplayName("GET /api/v1/facilities/nearby - 주변 시설 조회")
  void getNearbyFacilities() throws Exception {
    FacilityListResponse response =
        new FacilityListResponse(
            "FAC001",
            "강남 키움센터",
            "우리동네키움센터",
            "강남구",
            "서울특별시 강남구 테헤란로 123",
            new BigDecimal("37.5665"),
            new BigDecimal("126.978"),
            new BigDecimal("4.5"),
            null,
            true,
            false);

    given(
            facilityService.getNearbyFacilities(
                eq(new BigDecimal("37.5172")),
                eq(new BigDecimal("127.0473")),
                eq(new BigDecimal("3.0")),
                eq(10)))
        .willReturn(List.of(response));

    mockMvc
        .perform(get("/api/v1/facilities/nearby").param("lat", "37.5172").param("lng", "127.0473"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("FAC001"));
  }

  @Test
  @DisplayName("GET /api/v1/facilities/filters - 필터 옵션 조회")
  void getFilters() throws Exception {
    FacilityFilterResponse response =
        new FacilityFilterResponse(
            List.of(new FacilityFilterResponse.FilterOption("우리동네키움센터", "우리동네키움센터")),
            List.of(new FacilityFilterResponse.FilterOption("강남구", "강남구")),
            List.of(new FacilityFilterResponse.FilterOption("6~9세", "6-9")),
            List.of(new FacilityFilterResponse.FilterOption("임시 돌봄", "TEMPORARY")));

    given(facilityService.getFilters()).willReturn(response);

    mockMvc
        .perform(get("/api/v1/facilities/filters"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.serviceTypes[0].label").value("우리동네키움센터"))
        .andExpect(jsonPath("$.districts[0].label").value("강남구"));
  }
}
