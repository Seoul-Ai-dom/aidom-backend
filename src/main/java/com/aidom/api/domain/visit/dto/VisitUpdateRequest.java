package com.aidom.api.domain.visit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "이용내역 수정 요청")
public record VisitUpdateRequest(
    @Schema(description = "이용 날짜", example = "2025-06-15") LocalDate visitDate,
    @Schema(description = "시작 시간", example = "10:00") LocalTime startTime,
    @Schema(description = "종료 시간", example = "14:00") LocalTime endTime) {}
