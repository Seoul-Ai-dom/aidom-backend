package com.aidom.api.domain.facility.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "시설 목록/지도 응답")
public record FacilityListResponse(
    @Schema(description = "시설 ID", example = "FAC001") String id,
    @Schema(description = "시설명", example = "강남 키움센터") String facilityName,
    @Schema(description = "서비스 유형", example = "우리동네키움센터") String serviceType,
    @Schema(description = "자치구명", example = "강남구") String districtName,
    @Schema(description = "주소", example = "서울특별시 강남구 ...") String address,
    @Schema(description = "위도", example = "37.5172") BigDecimal lat,
    @Schema(description = "경도", example = "127.0473") BigDecimal lng,
    @Schema(description = "평균 평점", example = "4.5") BigDecimal avgRating,
    @Schema(description = "썸네일 URL") String thumbnailUrl,
    @Schema(description = "무료 여부", example = "true") boolean isFree,
    @Schema(description = "예약 필요 여부", example = "false") boolean bookingRequired) {}
