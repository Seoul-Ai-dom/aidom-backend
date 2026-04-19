package com.aidom.api.domain.facility.controller;

import com.aidom.api.domain.facility.dto.*;
import com.aidom.api.global.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "시설 Facilities", description = "시설 조회·검색·추천·필터 API")
@RestController
@RequestMapping("/api/v1/facilities")
public class FacilityController {

  @Operation(summary = "시설 목록 조회", description = "필터 조건에 따른 시설 목록을 페이지네이션으로 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @GetMapping
  public ResponseEntity<PageResponse<FacilityListResponse>> getFacilities(
      @Parameter(description = "자치구명", example = "강남구") @RequestParam(required = false)
          String districtName,
      @Parameter(description = "서비스 유형", example = "KIUM_CENTER") @RequestParam(required = false)
          String serviceType,
      @Parameter(description = "무료 여부") @RequestParam(required = false) Boolean isFree,
      @Parameter(description = "최소 연령", example = "6") @RequestParam(required = false)
          Integer ageMin,
      @Parameter(description = "최대 연령", example = "12") @RequestParam(required = false)
          Integer ageMax,
      @Parameter(description = "위도", example = "37.5172") @RequestParam(required = false)
          BigDecimal lat,
      @Parameter(description = "경도", example = "127.0473") @RequestParam(required = false)
          BigDecimal lng,
      @Parameter(description = "반경(km)", example = "3.0") @RequestParam(required = false)
          BigDecimal radius,
      @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20")
          int size) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Operation(summary = "시설 검색", description = "키워드로 시설을 검색합니다. (Elasticsearch)")
  @ApiResponse(responseCode = "200", description = "검색 성공")
  @GetMapping("/search")
  public ResponseEntity<PageResponse<FacilitySearchResponse>> searchFacilities(
      @Parameter(description = "검색 키워드", required = true, example = "키움센터") @RequestParam
          String keyword,
      @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20")
          int size) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Operation(summary = "시설 상세 조회", description = "시설 ID로 상세 정보를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @ApiResponse(responseCode = "404", description = "시설을 찾을 수 없음")
  @GetMapping("/{facilityId}")
  public ResponseEntity<FacilityDetailResponse> getFacility(
      @Parameter(description = "시설 ID", required = true, example = "FAC001") @PathVariable
          String facilityId) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Operation(summary = "AI 시설 추천", description = "아이 정보와 위치 기반으로 시설을 추천합니다.")
  @ApiResponse(responseCode = "200", description = "추천 성공")
  @GetMapping("/recommend")
  public ResponseEntity<List<FacilityRecommendResponse>> recommendFacilities(
      @Parameter(description = "아이 ID", required = true, example = "1") @RequestParam Long childId,
      @Parameter(description = "위도", example = "37.5172") @RequestParam(required = false)
          BigDecimal lat,
      @Parameter(description = "경도", example = "127.0473") @RequestParam(required = false)
          BigDecimal lng,
      @Parameter(description = "최대 결과 수", example = "5") @RequestParam(defaultValue = "5")
          int limit) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Operation(summary = "주변 시설 조회", description = "현재 위치 기반으로 주변 시설을 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @GetMapping("/nearby")
  public ResponseEntity<List<FacilityListResponse>> getNearbyFacilities(
      @Parameter(description = "위도", required = true, example = "37.5172") @RequestParam
          BigDecimal lat,
      @Parameter(description = "경도", required = true, example = "127.0473") @RequestParam
          BigDecimal lng,
      @Parameter(description = "반경(km)", example = "3.0") @RequestParam(defaultValue = "3.0")
          BigDecimal radius,
      @Parameter(description = "최대 결과 수", example = "10") @RequestParam(defaultValue = "10")
          int limit) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Operation(summary = "필터 옵션 조회", description = "시설 목록 필터에 사용할 옵션(서비스유형, 자치구, 연령대, 돌봄유형)을 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @GetMapping("/filters")
  public ResponseEntity<FacilityFilterResponse> getFilters() {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
