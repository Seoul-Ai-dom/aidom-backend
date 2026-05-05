package com.aidom.api.domain.user.enums;

import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeoulDistrict {
  GANGNAM("강남구"),
  GANGDONG("강동구"),
  GANGBUK("강북구"),
  GANGSEO("강서구"),
  GWANAK("관악구"),
  GWANGJIN("광진구"),
  GURO("구로구"),
  GEUMCHEON("금천구"),
  NOWON("노원구"),
  DOBONG("도봉구"),
  DONGDAEMUN("동대문구"),
  DONGJAK("동작구"),
  MAPO("마포구"),
  SEODAEMUN("서대문구"),
  SEOCHO("서초구"),
  SEONGDONG("성동구"),
  SEONGBUK("성북구"),
  SONGPA("송파구"),
  YANGCHEON("양천구"),
  YEONGDEUNGPO("영등포구"),
  YONGSAN("용산구"),
  EUNPYEONG("은평구"),
  JONGNO("종로구"),
  JUNG("중구"),
  JUNGNANG("중랑구");

  @JsonValue private final String description;

  @JsonCreator
  public static SeoulDistrict from(String value) {
    if (value == null || value.isBlank()) {
      throw new CustomException(ErrorCode.INVALID_DISTRICT_VALUE);
    }
    String normalizedValue = value.trim();
    return Arrays.stream(SeoulDistrict.values())
        .filter(district -> district.description.equals(normalizedValue))
        .findFirst()
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_DISTRICT_VALUE));
  }
}
