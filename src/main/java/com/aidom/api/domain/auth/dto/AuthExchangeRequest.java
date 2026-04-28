package com.aidom.api.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthExchangeRequest(@NotBlank String code) {}
