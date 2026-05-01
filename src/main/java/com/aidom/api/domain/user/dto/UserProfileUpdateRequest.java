package com.aidom.api.domain.user.dto;

import com.aidom.api.domain.user.enums.ParentRelation;
import com.aidom.api.global.common.entity.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "내 정보 수정 요청")
public record UserProfileUpdateRequest(
    @Valid
        @Schema(
            description = "학부모 정보 (해당 섹션을 수정할 때만 포함)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        ParentInfo parentInfo,
    @Valid
        @Size(min = 1, message = "children은 최소 1명 이상이어야 합니다.")
        @Schema(
            description = "아이 정보 목록 (해당 섹션을 수정할 때만 포함)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        List<@NotNull ChildInfo> children) {

  @AssertTrue(message = "parentInfo 또는 children 중 하나는 반드시 포함해야 합니다.")
  @Schema(hidden = true)
  public boolean hasUpdatableSection() {
    return parentInfo != null || children != null;
  }

  @Schema(description = "학부모 정보")
  public record ParentInfo(
      @NotBlank
          @Size(max = 50)
          @Schema(
              description = "학부모 이름",
              example = "홍길동",
              requiredMode = Schema.RequiredMode.REQUIRED)
          String name,
      @NotNull
          @Schema(
              description = "부모와 아이의 관계 (FATHER=부, MOTHER=모, GUARDIAN=보호자)",
              allowableValues = {"FATHER", "MOTHER", "GUARDIAN"},
              example = "MOTHER",
              requiredMode = Schema.RequiredMode.REQUIRED)
          ParentRelation relation,
      @NotNull
          @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
          @Schema(
              description = "학부모 생년월일 (yyyy-MM-dd)",
              example = "1986-02-15",
              requiredMode = Schema.RequiredMode.REQUIRED)
          LocalDate birthDate,
      @NotBlank
          @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "연락처는 010-XXXX-XXXX 형식이어야 합니다.")
          @Schema(
              description = "연락처 (010-XXXX-XXXX 형식)",
              example = "010-1234-5678",
              requiredMode = Schema.RequiredMode.REQUIRED)
          String phoneNumber,
      @NotBlank
          @Size(max = 200)
          @Schema(
              description = "주소",
              example = "서울특별시 마포구 만리재로 74",
              requiredMode = Schema.RequiredMode.REQUIRED)
          String address,
      @NotBlank
          @Size(max = 200)
          @Schema(
              description = "상세주소",
              example = "103동 2004호",
              requiredMode = Schema.RequiredMode.REQUIRED)
          String detailAddress) {}

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
          @Schema(
              description = "아이 성별 (MALE=남성, FEMALE=여성)",
              allowableValues = {"MALE", "FEMALE"},
              example = "MALE",
              requiredMode = Schema.RequiredMode.REQUIRED)
          Gender gender,
      @NotNull
          @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
          @Schema(
              description = "아이 생년월일 (yyyy-MM-dd)",
              example = "2021-03-15",
              requiredMode = Schema.RequiredMode.REQUIRED)
          LocalDate birthDate,
      @Size(max = 1000) @Schema(description = "특이사항/알레르기", example = "우유 알레르기", nullable = true)
          String specialNotes) {}
}
