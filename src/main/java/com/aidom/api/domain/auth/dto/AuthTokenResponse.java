package com.aidom.api.domain.auth.dto;

import com.aidom.api.domain.user.enums.UserStatus;

public record AuthTokenResponse(
    String accessToken,
    String refreshToken,
    long expiresIn,
    String tokenType,
    UserStatus userStatus) {}
