package com.aidom.api.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증 Auth", description = "OAuth2 로그인 진입 API")
@RestController
@RequestMapping("/api/v1/oauth2")
public class OAuth2LoginController {

  @Operation(
      summary = "카카오 로그인 시작",
      description = "프론트 호환을 위해 /oauth2/authorization/kakao 경로로 리다이렉트합니다.")
  @ApiResponse(responseCode = "302", description = "카카오 로그인 페이지로 리다이렉트")
  @GetMapping("/kakao")
  public ResponseEntity<Void> startKakaoLogin() {
    return ResponseEntity.status(302).location(URI.create("/oauth2/authorization/kakao")).build();
  }
}
