package com.aidom.api.domain.visit.dto;

import com.aidom.api.domain.visit.enums.VisitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "최근 방문 간략 응답")
public record VisitSummaryResponse(
    @Schema(description = "이용내역 ID", example = "1") Long visitId,
    @Schema(description = "시설 ID", example = "FAC001") String facilityId,
    @Schema(description = "시설명") String facilityName,
    @Schema(description = "서비스 유형", example = "우리동네키움센터") String serviceType,
    @Schema(description = "이용 날짜", example = "2025-06-01") LocalDate visitDate,
    @Schema(description = "이용 상태", example = "CONFIRMED") VisitStatus status) {}
