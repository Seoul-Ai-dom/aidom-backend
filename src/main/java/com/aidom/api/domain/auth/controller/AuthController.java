package com.aidom.api.domain.auth.controller;

import com.aidom.api.domain.auth.dto.AuthExchangeRequest;
import com.aidom.api.domain.auth.dto.AuthLogoutRequest;
import com.aidom.api.domain.auth.dto.AuthRefreshRequest;
import com.aidom.api.domain.auth.dto.AuthTokenResponse;
import com.aidom.api.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/exchange")
  public ResponseEntity<AuthTokenResponse> exchange(
      @Valid @RequestBody AuthExchangeRequest request) {
    return ResponseEntity.ok(authService.exchangeCode(request.code()));
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthTokenResponse> refresh(@Valid @RequestBody AuthRefreshRequest request) {
    return ResponseEntity.ok(authService.refresh(request.refreshToken()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody AuthLogoutRequest request) {
    authService.logout(request.refreshToken());
    return ResponseEntity.noContent().build();
  }
}
