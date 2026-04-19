package com.aidom.api.domain.visit.controller;

import com.aidom.api.domain.visit.dto.*;
import com.aidom.api.domain.visit.enums.VisitStatus;
import com.aidom.api.global.common.dto.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.YearMonth;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "이용내역 Visits", description = "시설 이용내역 관리 API")
@RestController
public class VisitController {

  @Operation(
      summary = "내 이용내역 목록 조회",
      description = "로그인 사용자의 이용내역을 무한스크롤로 조회합니다. yearMonth를 지정하면 해당 월의 내역만 반환합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @GetMapping("/api/v1/users/me/visits")
  public ResponseEntity<SliceResponse<VisitResponse>> getMyVisits(
      @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20")
          int size,
      @Parameter(description = "이용 상태 필터", example = "PLANNED") @RequestParam(required = false)
          VisitStatus status,
      @Parameter(description = "조회 월 (달력용)", example = "2025-06") @RequestParam(required = false)
          YearMonth yearMonth) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Operation(summary = "이용내역 등록", description = "시설 문의 내역을 등록합니다. (PLANNED 상태)")
  @ApiResponse(responseCode = "201", description = "등록 성공")
  @PostMapping("/api/v1/visits")
  public ResponseEntity<VisitResponse> createVisit(@Valid @RequestBody VisitCreateRequest request) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Operation(summary = "이용내역 상세 조회", description = "이용내역 ID로 상세 정보를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @ApiResponse(responseCode = "404", description = "이용 내역을 찾을 수 없음")
  @GetMapping("/api/v1/visits/{visitId}")
  public ResponseEntity<VisitResponse> getVisit(
      @Parameter(description = "이용내역 ID", required = true, example = "1") @PathVariable
          Long visitId) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Operation(summary = "이용내역 수정", description = "이용내역의 이용 날짜·시간을 수정합니다.")
  @ApiResponse(responseCode = "200", description = "수정 성공")
  @ApiResponse(responseCode = "404", description = "이용 내역을 찾을 수 없음")
  @PatchMapping("/api/v1/visits/{visitId}")
  public ResponseEntity<VisitResponse> updateVisit(
      @Parameter(description = "이용내역 ID", required = true, example = "1") @PathVariable
          Long visitId,
      @Valid @RequestBody VisitUpdateRequest request) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Operation(summary = "이용내역 취소", description = "이용내역을 취소합니다. (소프트 삭제)")
  @ApiResponse(responseCode = "200", description = "취소 성공")
  @ApiResponse(responseCode = "404", description = "이용 내역을 찾을 수 없음")
  @ApiResponse(responseCode = "422", description = "이미 취소된 이용 내역")
  @PutMapping("/api/v1/visits/{visitId}/cancel")
  public ResponseEntity<VisitResponse> cancelVisit(
      @Parameter(description = "이용내역 ID", required = true, example = "1") @PathVariable
          Long visitId) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Operation(summary = "이용내역 확정", description = "이용내역을 확정합니다. 실제 이용 날짜와 시간을 입력받습니다.")
  @ApiResponse(responseCode = "200", description = "확정 성공")
  @ApiResponse(responseCode = "404", description = "이용 내역을 찾을 수 없음")
  @ApiResponse(responseCode = "422", description = "이미 확정된 이용 내역")
  @PutMapping("/api/v1/visits/{visitId}/confirm")
  public ResponseEntity<VisitResponse> confirmVisit(
      @Parameter(description = "이용내역 ID", required = true, example = "1") @PathVariable
          Long visitId,
      @Valid @RequestBody VisitConfirmRequest request) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Operation(summary = "최근 방문 내역 조회", description = "로그인 사용자의 최근 방문 내역을 간략히 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @GetMapping("/api/v1/users/me/visits/recent")
  public ResponseEntity<List<VisitSummaryResponse>> getRecentVisits(
      @Parameter(description = "최대 결과 수", example = "5") @RequestParam(defaultValue = "5")
          int limit) {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
