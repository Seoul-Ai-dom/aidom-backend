package com.aidom.api.domain.user.controller;

import com.aidom.api.domain.user.dto.UserMeResponse;
import com.aidom.api.domain.user.dto.UserProfileUpdateRequest;
import com.aidom.api.domain.user.service.UserProfileService;
import com.aidom.api.domain.user.service.UserWithdrawalService;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import com.aidom.api.global.security.AuthenticatedUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 User", description = "유저 계정 API")
@RestController
@RequestMapping("/api/v1/users/me")
public class UserAccountController {

  private final UserProfileService userProfileService;
  private final UserWithdrawalService userWithdrawalService;

  public UserAccountController(
      UserProfileService userProfileService, UserWithdrawalService userWithdrawalService) {
    this.userProfileService = userProfileService;
    this.userWithdrawalService = userWithdrawalService;
  }

  @Operation(summary = "내 정보 조회", description = "로그인한 학부모와 아이 정보를 조회합니다.")
  @ApiResponse(
      responseCode = "200",
      description = "내 정보 조회 성공",
      content = @Content(schema = @Schema(implementation = UserMeResponse.class)))
  @ApiResponse(responseCode = "401", description = "인증 실패 또는 토큰 문제")
  @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
  @GetMapping
  public ResponseEntity<UserMeResponse> getMyProfile(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    validatePrincipal(principal);
    return ResponseEntity.ok(userProfileService.getMyProfile(principal.userId()));
  }

  @Operation(
      summary = "내 정보 수정",
      description =
          "학부모 정보와 아이 정보를 한 번에 또는 섹션 단위로 수정합니다. children이 포함되면 현재 목록 기준으로 추가/수정/삭제를 반영합니다.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = UserProfileUpdateRequest.class),
                      examples = {
                        @ExampleObject(
                            name = "부모+아이 동시 수정",
                            value =
                                """
                                {
                                  "parentInfo": {
                                    "name": "홍길동",
                                    "relation": "MOTHER",
                                    "birthDate": "1986-02-15",
                                    "phoneNumber": "010-1234-5678",
                                    "address": "서울특별시 마포구 만리재로 74",
                                    "detailAddress": "103동 2004호"
                                  },
                                  "children": [
                                    {
                                      "name": "김아이",
                                      "gender": "MALE",
                                      "birthDate": "2021-03-15",
                                      "specialNotes": "우유 알레르기"
                                    }
                                  ]
                                }
                                """),
                        @ExampleObject(
                            name = "아이 정보만 수정",
                            value =
                                """
                                {
                                  "children": [
                                    {
                                      "name": "김첫째",
                                      "gender": "MALE",
                                      "birthDate": "2021-03-15",
                                      "specialNotes": null
                                    },
                                    {
                                      "name": "김둘째",
                                      "gender": "FEMALE",
                                      "birthDate": "2023-05-20",
                                      "specialNotes": "견과류 알레르기"
                                    }
                                  ]
                                }
                                """)
                      })))
  @ApiResponse(
      responseCode = "200",
      description = "내 정보 수정 성공",
      content = @Content(schema = @Schema(implementation = UserMeResponse.class)))
  @ApiResponse(responseCode = "400", description = "요청 값 유효성 검증 실패")
  @ApiResponse(responseCode = "401", description = "인증 실패 또는 토큰 문제")
  @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
  @PatchMapping
  public ResponseEntity<UserMeResponse> updateMyProfile(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
      @Valid @RequestBody UserProfileUpdateRequest request) {
    validatePrincipal(principal);
    return ResponseEntity.ok(userProfileService.updateMyProfile(principal.userId(), request));
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
    validatePrincipal(principal);
    userWithdrawalService.withdraw(principal.userId());
    return ResponseEntity.noContent().build();
  }

  private void validatePrincipal(AuthenticatedUserPrincipal principal) {
    if (principal == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
  }
}
