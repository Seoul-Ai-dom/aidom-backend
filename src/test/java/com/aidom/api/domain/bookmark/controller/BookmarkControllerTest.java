package com.aidom.api.domain.bookmark.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aidom.api.domain.bookmark.dto.BookmarkResponse;
import com.aidom.api.domain.bookmark.dto.BookmarkStatusResponse;
import com.aidom.api.domain.bookmark.service.BookmarkService;
import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.domain.user.enums.Role;
import com.aidom.api.domain.user.enums.UserStatus;
import com.aidom.api.global.common.dto.SliceResponse;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import com.aidom.api.global.security.AuthenticatedUserPrincipal;
import com.aidom.api.support.WebMvcTestSecurityConfig;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = BookmarkController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = ".*SecurityConfig.*|.*JwtAuthentication.*|.*OAuth2.*|.*ProblemDetail.*"))
@Import(WebMvcTestSecurityConfig.class)
class BookmarkControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private BookmarkService bookmarkService;

  private static final AuthenticatedUserPrincipal PRINCIPAL =
      new AuthenticatedUserPrincipal(
          1L, Role.USER, UserStatus.ACTIVE, Provider.KAKAO, "test@test.com");

  private Authentication auth() {
    return new UsernamePasswordAuthenticationToken(
        PRINCIPAL, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }

  @Test
  @DisplayName("찜 목록 조회 - 인증 시 200")
  void getMyBookmarks_authenticated_returns200() throws Exception {
    BookmarkResponse response =
        new BookmarkResponse(
            1L,
            "FAC001",
            "테스트 센터",
            "지역아동센터",
            "서울시 강남구",
            new BigDecimal("4.5"),
            "https://example.com/thumb.jpg",
            LocalDateTime.now());
    SliceResponse<BookmarkResponse> sliceResponse =
        new SliceResponse<>(List.of(response), 0, 20, false);

    given(bookmarkService.getMyBookmarks(anyLong(), anyInt(), anyInt())).willReturn(sliceResponse);

    mockMvc
        .perform(
            get("/api/v1/users/me/bookmarks")
                .param("page", "0")
                .param("size", "20")
                .with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].facilityId").value("FAC001"))
        .andExpect(jsonPath("$.content[0].avgRating").value(4.5))
        .andExpect(jsonPath("$.content[0].thumbnailUrl").value("https://example.com/thumb.jpg"));
  }

  @Test
  @DisplayName("찜 등록 - 인증 시 200")
  void addBookmark_authenticated_returns200() throws Exception {
    willDoNothing().given(bookmarkService).addBookmark(anyLong(), anyString());

    mockMvc
        .perform(post("/api/v1/facilities/FAC001/bookmark").with(authentication(auth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("찜 등록 - 이미 찜한 시설 409")
  void addBookmark_alreadyBookmarked_returns409() throws Exception {
    willThrow(new CustomException(ErrorCode.ALREADY_BOOKMARKED))
        .given(bookmarkService)
        .addBookmark(anyLong(), anyString());

    mockMvc
        .perform(post("/api/v1/facilities/FAC001/bookmark").with(authentication(auth())))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("찜 취소 - 인증 시 200")
  void removeBookmark_authenticated_returns200() throws Exception {
    willDoNothing().given(bookmarkService).removeBookmark(anyLong(), anyString());

    mockMvc
        .perform(delete("/api/v1/facilities/FAC001/bookmark").with(authentication(auth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("찜 취소 - 찜 내역 없음 404")
  void removeBookmark_notFound_returns404() throws Exception {
    willThrow(new CustomException(ErrorCode.BOOKMARK_NOT_FOUND))
        .given(bookmarkService)
        .removeBookmark(anyLong(), anyString());

    mockMvc
        .perform(delete("/api/v1/facilities/FAC001/bookmark").with(authentication(auth())))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("찜 여부 확인 - 인증 시 200")
  void getBookmarkStatus_authenticated_returns200() throws Exception {
    given(bookmarkService.getBookmarkStatus(anyLong(), anyString()))
        .willReturn(new BookmarkStatusResponse(true));

    mockMvc
        .perform(get("/api/v1/facilities/FAC001/bookmark/status").with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookmarked").value(true));
  }
}
