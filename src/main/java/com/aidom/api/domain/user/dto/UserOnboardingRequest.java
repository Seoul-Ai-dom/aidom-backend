package com.aidom.api.domain.user.dto;

import com.aidom.api.domain.user.enums.ParentRelation;
import com.aidom.api.global.common.entity.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "회원가입 완료(온보딩) 요청")
public record UserOnboardingRequest(
    @NotNull @Valid @Schema(description = "부모 정보", requiredMode = Schema.RequiredMode.REQUIRED)
        ParentInfo parentInfo,
    @NotEmpty @Valid @Schema(description = "아이 정보 목록(현재 UI는 1명, 백엔드는 List로 다자녀 확장 지원)")
        List<@NotNull ChildInfo> children) {

  @Schema(description = "부모(유저) 정보")
  public record ParentInfo(
      @NotBlank
          @Size(max = 50)
          @Schema(
              description = "부모 이름",
              example = "홍길동",
              requiredMode = Schema.RequiredMode.REQUIRED)
          String name,
      @NotNull
          @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
          @Schema(
              description = "부모 생년월일 (yyyy.MM.dd)",
              example = "1980.01.01",
              requiredMode = Schema.RequiredMode.REQUIRED)
          LocalDate birthDate,
      @NotNull
          @Schema(
              description = "부모와 아이의 관계 (FATHER=부, MOTHER=모, GUARDIAN=보호자(조부모, 기타))",
              allowableValues = {"FATHER", "MOTHER", "GUARDIAN"},
              example = "MOTHER",
              requiredMode = Schema.RequiredMode.REQUIRED)
          ParentRelation relation,
      @NotBlank
          @Size(max = 50)
          @Schema(
              description = "거주 자치구",
              example = "강남구",
              requiredMode = Schema.RequiredMode.REQUIRED)
          String district,
      @NotBlank
          @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "연락처는 010-XXXX-XXXX 형식이어야 합니다.")
          @Schema(
              description = "연락처 (010-XXXX-XXXX 형식)",
              example = "010-4543-8167",
              requiredMode = Schema.RequiredMode.REQUIRED)
          String phone) {}

  @Schema(description = "아이 정보")
  public record ChildInfo(
      @NotBlank
          @Size(max = 50)
          @Schema(
              description = "아이 이름",
              example = "김아이",
              requiredMode = Schema.RequiredMode.REQUIRED)
          String name,
      @NotNull
          @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
          @Schema(
              description = "아이 생년월일 (yyyy.MM.dd)",
              example = "2021.03.15",
              requiredMode = Schema.RequiredMode.REQUIRED)
          LocalDate birthDate,
      @NotNull
          @Schema(
              description = "아이 성별 (MALE=남자, FEMALE=여자)",
              allowableValues = {"MALE", "FEMALE"},
              example = "MALE",
              requiredMode = Schema.RequiredMode.REQUIRED)
          Gender gender,
      @Size(max = 1000) @Schema(description = "특이사항/알레르기 메모", example = "계란 알레르기", nullable = true)
          String specialNote) {}
}
