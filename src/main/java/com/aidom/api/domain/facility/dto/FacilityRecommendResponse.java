package com.aidom.api.domain.facility.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "AI 추천 시설 응답")
public record FacilityRecommendResponse(
    @Schema(description = "시설 ID", example = "FAC001") String id,
    @Schema(description = "시설명") String facilityName,
    @Schema(description = "서비스 유형", example = "우리동네키움센터") String serviceType,
    @Schema(description = "자치구명", example = "강남구") String districtName,
    @Schema(description = "평균 평점", example = "4.5") BigDecimal avgRating,
    @Schema(description = "썸네일 URL") String thumbnailUrl,
    @Schema(description = "추천 사유", example = "아이 연령대에 적합한 프로그램이 있어요") String recommendReason) {}
