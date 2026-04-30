package com.aidom.api.domain.user.controller;

import com.aidom.api.domain.user.dto.UserOnboardingRequest;
import com.aidom.api.domain.user.dto.UserOnboardingResponse;
import com.aidom.api.domain.user.service.UserOnboardingService;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import com.aidom.api.global.security.AuthenticatedUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 User", description = "유저 온보딩(회원가입 완료) API")
@RestController
@RequestMapping("/api/v1/users/me")
public class UserOnboardingController {

  private final UserOnboardingService userOnboardingService;

  public UserOnboardingController(UserOnboardingService userOnboardingService) {
    this.userOnboardingService = userOnboardingService;
  }

  @Operation(
      summary = "회원가입 완료(온보딩)",
      description = "ONBOARDING 상태의 유저가 부모/아이 정보를 저장하고 ACTIVE 상태로 전환합니다.")
  @ApiResponse(
      responseCode = "200",
      description = "온보딩 완료",
      content = @Content(schema = @Schema(implementation = UserOnboardingResponse.class)))
  @ApiResponse(responseCode = "400", description = "요청 값 유효성 검증 실패")
  @ApiResponse(responseCode = "401", description = "인증 실패 또는 토큰 문제")
  @ApiResponse(responseCode = "422", description = "이미 ACTIVE 상태 등 비즈니스 규칙 위반")
  @PostMapping("/onboarding")
  public ResponseEntity<UserOnboardingResponse> completeOnboarding(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
      @Valid @RequestBody UserOnboardingRequest request) {
    if (principal == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    UserOnboardingResponse response =
        userOnboardingService.completeOnboarding(principal.userId(), request);
    return ResponseEntity.ok(response);
  }
}
