package com.aidom.api.domain.facility.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "시설 필터 옵션 응답")
public record FacilityFilterResponse(
    @Schema(description = "서비스 유형 필터") List<FilterOption> serviceTypes,
    @Schema(description = "자치구 필터") List<FilterOption> districts,
    @Schema(description = "연령대 필터") List<FilterOption> ageRanges,
    @Schema(description = "돌봄 유형 필터") List<FilterOption> careTypes) {

  @Schema(description = "필터 옵션 항목")
  public record FilterOption(
      @Schema(description = "표시 레이블", example = "우리동네키움센터") String label,
      @Schema(description = "필터 값", example = "KIUM_CENTER") String value) {}
}
