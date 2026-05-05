package com.aidom.api.domain.user.dto;

import com.aidom.api.domain.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 완료(온보딩) 응답")
public record UserOnboardingResponse(
    @Schema(description = "유저 ID", example = "1") Long userId,
    @Schema(description = "변경된 유저 상태", example = "ACTIVE") UserStatus status,
    @Schema(description = "저장된 아이 수", example = "1") int childCount) {}
