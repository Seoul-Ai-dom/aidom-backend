package com.aidom.api.domain.user.entity;

import com.aidom.api.domain.user.enums.ParentRelation;
import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.domain.user.enums.Role;
import com.aidom.api.domain.user.enums.UserStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record ProvisionedProfile(
    String name,
    String email,
    Provider provider,
    String providerId,
    Role role,
    UserStatus status,
    ParentRelation relation,
    LocalDate birthDate,
    String phone,
    String address,
    String city,
    String district,
    String addressDetail,
    BigDecimal addressLat,
    BigDecimal addressLng) {}
