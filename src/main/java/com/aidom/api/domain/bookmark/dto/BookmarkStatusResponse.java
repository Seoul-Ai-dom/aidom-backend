package com.aidom.api.domain.bookmark.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "찜 여부 응답")
public record BookmarkStatusResponse(
    @Schema(description = "찜 여부", example = "true") boolean bookmarked) {}
