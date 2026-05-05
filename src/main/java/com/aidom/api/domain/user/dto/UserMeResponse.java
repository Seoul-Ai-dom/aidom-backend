package com.aidom.api.domain.user.dto;

import com.aidom.api.domain.user.enums.ParentRelation;
import com.aidom.api.global.common.entity.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "내 정보 조회/수정 응답")
public record UserMeResponse(
    @Schema(description = "유저 ID", example = "1") Long userId,
    @Schema(description = "학부모 정보") ParentInfo parentInfo,
    @Schema(description = "아이 정보 목록") List<ChildInfo> children) {

  @Schema(description = "학부모 정보")
  public record ParentInfo(
      @Schema(description = "학부모 이름", example = "홍길동") String name,
      @Schema(description = "관계", example = "MOTHER") ParentRelation relation,
      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
          @Schema(description = "학부모 생년월일", example = "1986-02-15")
          LocalDate birthDate,
      @Schema(description = "연락처", example = "010-1234-5678") String phoneNumber,
      @Schema(description = "주소", example = "서울특별시 마포구 만리재로 74") String address,
      @Schema(description = "상세주소", example = "103동 2004호") String detailAddress) {}

  @Schema(description = "아이 정보")
  public record ChildInfo(
      @Schema(description = "아이 이름", example = "김아이") String name,
      @Schema(description = "아이 성별", example = "MALE") Gender gender,
      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
          @Schema(description = "아이 생년월일", example = "2021-03-15")
          LocalDate birthDate,
      @Schema(description = "특이사항/알레르기", example = "우유 알레르기", nullable = true)
          String specialNotes) {}
}
