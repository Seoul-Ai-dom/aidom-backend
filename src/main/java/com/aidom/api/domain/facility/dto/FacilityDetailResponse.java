package com.aidom.api.domain.facility.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "시설 상세 조회 응답")
public record FacilityDetailResponse(
    @Schema(description = "시설 ID", example = "FAC001") String id,
    @Schema(description = "시설명") String facilityName,
    @Schema(description = "서비스 유형", example = "우리동네키움센터") String serviceType,
    @Schema(description = "서비스 유형 코드", example = "KIUM_CENTER") String serviceTypeCode,
    @Schema(description = "자치구명", example = "강남구") String districtName,
    @Schema(description = "자치구 코드", example = "11680") String districtCode,
    @Schema(description = "주소") String address,
    @Schema(description = "위도") BigDecimal lat,
    @Schema(description = "경도") BigDecimal lng,
    @Schema(description = "연령 그룹", example = "6~12세") String ageGroup,
    @Schema(description = "최소 연령", example = "6") int ageMin,
    @Schema(description = "최대 연령", example = "12") int ageMax,
    @Schema(description = "예약 필요 여부") boolean bookingRequired,
    @Schema(description = "무료 여부") boolean isFree,
    @Schema(description = "이용료", example = "5000") Integer fee,
    @Schema(description = "월 이용료", example = "50000") Integer monthlyFee,
    @Schema(description = "정규 정원", example = "30") Integer capacityRegular,
    @Schema(description = "임시 정원", example = "10") Integer capacityTemporary,
    @Schema(description = "면적(㎡)", example = "120.50") BigDecimal areaSqm,
    @Schema(description = "운영일") String operatingDays,
    @Schema(description = "휴무일") String closedDays,
    @Schema(description = "정규 프로그램 여부") boolean hasRegularProgram,
    @Schema(description = "정규 돌봄 여부") boolean hasRegularCare,
    @Schema(description = "임시 돌봄 여부") boolean hasTemporaryCare,
    @Schema(description = "전화번호") String phone,
    @Schema(description = "웹사이트") String website,
    @Schema(description = "네이버 영업시간") String naverHours,
    @Schema(description = "영업 상태") String businessStatus,
    @Schema(description = "썸네일 URL") String thumbnailUrl,
    @Schema(description = "평균 평점") BigDecimal avgRating,
    @Schema(description = "안전 평점") BigDecimal avgRatingSafety,
    @Schema(description = "청결 평점") BigDecimal avgRatingCleanliness,
    @Schema(description = "관리 평점") BigDecimal avgRatingManagement,
    @Schema(description = "친절 평점") BigDecimal avgRatingKindness,
    @Schema(description = "리뷰 수", example = "42") int reviewCount) {}
