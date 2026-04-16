package com.aidom.api.domain.bookmark.controller;

import com.aidom.api.domain.bookmark.dto.BookmarkResponse;
import com.aidom.api.domain.bookmark.dto.BookmarkStatusResponse;
import com.aidom.api.global.common.dto.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "찜 Bookmarks", description = "시설 찜(북마크) API")
@RestController
public class BookmarkController {

  @Operation(summary = "내 찜 목록 조회", description = "로그인 사용자의 찜 목록을 무한스크롤로 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @GetMapping("/api/v1/users/me/bookmarks")
  public ResponseEntity<SliceResponse<BookmarkResponse>> getMyBookmarks(
      @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20")
          int size) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Operation(summary = "찜 등록", description = "시설을 찜합니다. 이미 찜한 시설이면 409 에러를 반환합니다.")
  @ApiResponse(responseCode = "200", description = "찜 등록 성공")
  @ApiResponse(responseCode = "409", description = "이미 찜한 시설")
  @PostMapping("/api/v1/facilities/{facilityId}/bookmark")
  public ResponseEntity<Void> addBookmark(
      @Parameter(description = "시설 ID", required = true, example = "FAC001") @PathVariable
          String facilityId) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Operation(summary = "찜 취소", description = "시설 찜을 취소합니다. (소프트 삭제)")
  @ApiResponse(responseCode = "200", description = "찜 취소 성공")
  @ApiResponse(responseCode = "404", description = "찜 내역을 찾을 수 없음")
  @DeleteMapping("/api/v1/facilities/{facilityId}/bookmark")
  public ResponseEntity<Void> removeBookmark(
      @Parameter(description = "시설 ID", required = true, example = "FAC001") @PathVariable
          String facilityId) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Operation(summary = "찜 여부 확인", description = "해당 시설의 찜 여부를 확인합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @GetMapping("/api/v1/facilities/{facilityId}/bookmark/status")
  public ResponseEntity<BookmarkStatusResponse> getBookmarkStatus(
      @Parameter(description = "시설 ID", required = true, example = "FAC001") @PathVariable
          String facilityId) {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
