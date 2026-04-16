package com.aidom.api.domain.visit.dto;

import com.aidom.api.domain.visit.enums.VisitSource;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "이용내역 등록 요청 (문의 기록)")
public record VisitCreateRequest(
    @Schema(description = "시설 ID", example = "FAC001") @NotNull String facilityId,
    @Schema(description = "아이 ID", example = "1") @NotNull Long childId,
    @Schema(description = "등록 경로", example = "MANUAL") VisitSource source) {}
