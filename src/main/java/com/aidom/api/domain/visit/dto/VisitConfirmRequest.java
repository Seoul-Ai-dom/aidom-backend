package com.aidom.api.domain.visit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "이용내역 확정 요청 (실제 이용 날짜·시간 입력)")
public record VisitConfirmRequest(
    @Schema(description = "이용 날짜", example = "2025-06-01") @NotNull LocalDate visitDate,
    @Schema(description = "시작 시간", example = "09:00") @NotNull LocalTime startTime,
    @Schema(description = "종료 시간", example = "13:00") @NotNull LocalTime endTime) {}
