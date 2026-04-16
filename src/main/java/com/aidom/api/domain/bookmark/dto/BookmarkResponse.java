package com.aidom.api.domain.bookmark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "찜 목록 항목 응답")
public record BookmarkResponse(
    @Schema(description = "찜 ID", example = "1") Long bookmarkId,
    @Schema(description = "시설 ID", example = "FAC001") String facilityId,
    @Schema(description = "시설명") String facilityName,
    @Schema(description = "서비스 유형", example = "우리동네키움센터") String serviceType,
    @Schema(description = "주소") String address,
    @Schema(description = "평균 평점", example = "4.5") BigDecimal avgRating,
    @Schema(description = "썸네일 URL") String thumbnailUrl,
    @Schema(description = "찜 등록일시") LocalDateTime createdAt) {}
