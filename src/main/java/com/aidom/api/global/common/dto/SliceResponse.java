package com.aidom.api.global.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Slice;

@Schema(description = "무한스크롤 응답")
public record SliceResponse<T>(
    @Schema(description = "데이터 목록") List<T> content,
    @Schema(description = "현재 페이지 번호 (0-based)", example = "0") int page,
    @Schema(description = "페이지 크기", example = "20") int size,
    @Schema(description = "다음 페이지 존재 여부", example = "true") boolean hasNext) {

  public static <T> SliceResponse<T> from(Slice<T> slice) {
    return new SliceResponse<>(
        slice.getContent(), slice.getNumber(), slice.getSize(), slice.hasNext());
  }
}
