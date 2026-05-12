package com.aidom.api.domain.facility.dto;

import java.util.Set;

public record UserRecommendationContext(
    int childAge,
    String district,
    int bookmarkCount,
    Set<String> preferredServiceTypes,
    int visitCount,
    boolean locationProvided,
    String timeOfDay,
    String season) {}
