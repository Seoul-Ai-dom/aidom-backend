package com.aidom.api.domain.visit.dto;

import com.aidom.api.domain.visit.enums.VisitSource;
import com.aidom.api.domain.visit.enums.VisitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "이용내역 응답")
public record VisitResponse(
    @Schema(description = "이용내역 ID", example = "1") Long visitId,
    @Schema(description = "시설 ID", example = "FAC001") String facilityId,
    @Schema(description = "시설명") String facilityName,
    @Schema(description = "아이 ID", example = "1") Long childId,
    @Schema(description = "아이 이름") String childName,
    @Schema(description = "이용 상태", example = "PLANNED") VisitStatus status,
    @Schema(description = "등록 경로", example = "MANUAL") VisitSource source,
    @Schema(description = "이용 날짜", example = "2025-06-01") LocalDate visitDate,
    @Schema(description = "시작 시간", example = "09:00") LocalTime startTime,
    @Schema(description = "종료 시간", example = "13:00") LocalTime endTime,
    @Schema(description = "이용 시간(분)", example = "240") Long durationMinutes,
    @Schema(description = "확정 일시") LocalDateTime confirmedAt,
    @Schema(description = "취소 일시") LocalDateTime cancelledAt,
    @Schema(description = "등록 일시") LocalDateTime createdAt) {}
