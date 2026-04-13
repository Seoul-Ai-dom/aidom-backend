package com.aidom.api.domain.user.controller;

import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.service.AuthService;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import com.aidom.api.global.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @GetMapping("/me")
  public ResponseEntity<AuthUserResponse> me(
      @AuthenticationPrincipal PrincipalDetails principalDetails) {
    if (principalDetails == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    User user = authService.getUser(principalDetails.getId());
    return ResponseEntity.ok(
        new AuthUserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getProvider().name(),
            user.getRole().name(),
            user.getStatus().name(),
            user.requiresOnboarding()));
  }

  public record AuthUserResponse(
      Long userId,
      String email,
      String name,
      String provider,
      String role,
      String status,
      boolean onboardingRequired) {}
}
