package com.aidom.api.global.security;

import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.domain.user.enums.Role;
import com.aidom.api.domain.user.enums.UserStatus;

public record AuthenticatedUserPrincipal(
    Long userId, Role role, UserStatus status, Provider provider, String email) {}
