package com.aidom.api.domain.user.controller;

import com.aidom.api.domain.user.service.UserWithdrawalService;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import com.aidom.api.global.security.AuthenticatedUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 User", description = "유저 계정 API")
@RestController
@RequestMapping("/api/v1/users/me")
public class UserAccountController {

  private final UserWithdrawalService userWithdrawalService;

  public UserAccountController(UserWithdrawalService userWithdrawalService) {
    this.userWithdrawalService = userWithdrawalService;
  }

  @Operation(summary = "회원 탈퇴", description = "카카오 연동 해제 후 회원을 소프트 삭제합니다.")
  @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공")
  @ApiResponse(responseCode = "401", description = "인증 실패 또는 토큰 문제")
  @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
  @ApiResponse(responseCode = "422", description = "이미 탈퇴한 유저")
  @ApiResponse(responseCode = "502", description = "카카오 연동 해제 실패")
  @DeleteMapping
  public ResponseEntity<Void> withdraw(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    if (principal == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    userWithdrawalService.withdraw(principal.userId());
    return ResponseEntity.noContent().build();
  }
}
