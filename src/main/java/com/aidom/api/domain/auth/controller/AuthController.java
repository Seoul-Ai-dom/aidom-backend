package com.aidom.api.domain.auth.controller;

import com.aidom.api.domain.auth.dto.AuthExchangeRequest;
import com.aidom.api.domain.auth.dto.AuthLogoutRequest;
import com.aidom.api.domain.auth.dto.AuthRefreshRequest;
import com.aidom.api.domain.auth.dto.AuthTokenResponse;
import com.aidom.api.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증 Auth", description = "소셜 로그인 후 토큰 교환/재발급/로그아웃 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @Operation(
      summary = "인가 코드 교환",
      description = "카카오 로그인 성공 후 전달받은 one-time code를 access/refresh 토큰으로 교환합니다.")
  @ApiResponse(responseCode = "200", description = "토큰 교환 성공")
  @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 code")
  @PostMapping("/exchange")
  public ResponseEntity<AuthTokenResponse> exchange(
      @Valid @RequestBody AuthExchangeRequest request) {
    return ResponseEntity.ok(authService.exchangeCode(request.code()));
  }

  @Operation(
      summary = "토큰 재발급",
      description = "refresh token rotation 방식으로 access/refresh 토큰을 재발급합니다.")
  @ApiResponse(responseCode = "200", description = "토큰 재발급 성공")
  @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 refresh token")
  @PostMapping("/refresh")
  public ResponseEntity<AuthTokenResponse> refresh(@Valid @RequestBody AuthRefreshRequest request) {
    return ResponseEntity.ok(authService.refresh(request.refreshToken()));
  }

  @Operation(summary = "로그아웃", description = "요청한 refresh token을 무효화(revoke)합니다.")
  @ApiResponse(responseCode = "204", description = "로그아웃 성공")
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody AuthLogoutRequest request) {
    authService.logout(request.refreshToken());
    return ResponseEntity.noContent().build();
  }
}
