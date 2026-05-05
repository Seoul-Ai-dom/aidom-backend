package com.aidom.api.domain.visit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.domain.user.enums.Role;
import com.aidom.api.domain.user.enums.UserStatus;
import com.aidom.api.domain.visit.dto.VisitConfirmRequest;
import com.aidom.api.domain.visit.dto.VisitCreateRequest;
import com.aidom.api.domain.visit.dto.VisitResponse;
import com.aidom.api.domain.visit.dto.VisitSummaryResponse;
import com.aidom.api.domain.visit.dto.VisitUpdateRequest;
import com.aidom.api.domain.visit.enums.VisitSource;
import com.aidom.api.domain.visit.enums.VisitStatus;
import com.aidom.api.domain.visit.service.VisitService;
import com.aidom.api.global.common.dto.SliceResponse;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import com.aidom.api.global.security.AuthenticatedUserPrincipal;
import com.aidom.api.support.WebMvcTestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = VisitController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = ".*SecurityConfig.*|.*JwtAuthentication.*|.*OAuth2.*|.*ProblemDetail.*"))
@Import(WebMvcTestSecurityConfig.class)
class VisitControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private VisitService visitService;

  private static final AuthenticatedUserPrincipal PRINCIPAL =
      new AuthenticatedUserPrincipal(
          1L, Role.USER, UserStatus.ACTIVE, Provider.KAKAO, "test@test.com");

  private Authentication auth() {
    return new UsernamePasswordAuthenticationToken(
        PRINCIPAL, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }

  private VisitResponse sampleVisitResponse() {
    return new VisitResponse(
        1L,
        "FAC001",
        "테스트 센터",
        1L,
        "아이",
        VisitStatus.PLANNED,
        VisitSource.MANUAL,
        LocalDate.of(2025, 6, 1),
        LocalTime.of(9, 0),
        LocalTime.of(13, 0),
        240L,
        null,
        null,
        null);
  }

  @Test
  @DisplayName("이용내역 등록 - 정상 요청 시 201")
  void createVisit_validRequest_returns201() throws Exception {
    VisitCreateRequest request = new VisitCreateRequest("FAC001", 1L, VisitSource.MANUAL);
    given(visitService.createVisit(anyLong(), any(VisitCreateRequest.class)))
        .willReturn(sampleVisitResponse());

    mockMvc
        .perform(
            post("/api/v1/visits")
                .with(authentication(auth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.visitId").value(1))
        .andExpect(jsonPath("$.status").value("PLANNED"));
  }

  @Test
  @DisplayName("이용내역 등록 - source 누락 시 400")
  void createVisit_missingSource_returns400() throws Exception {
    String body = """
        {"facilityId": "FAC001", "childId": 1}
        """;

    mockMvc
        .perform(
            post("/api/v1/visits")
                .with(authentication(auth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("이용내역 등록 - facilityId 누락 시 400")
  void createVisit_missingFacilityId_returns400() throws Exception {
    String body = """
        {"childId": 1, "source": "MANUAL"}
        """;

    mockMvc
        .perform(
            post("/api/v1/visits")
                .with(authentication(auth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("이용내역 목록 조회 - 정상 200")
  void getMyVisits_returns200() throws Exception {
    SliceResponse<VisitResponse> sliceResponse =
        new SliceResponse<>(List.of(sampleVisitResponse()), 0, 20, false);
    given(visitService.getMyVisits(anyLong(), anyInt(), anyInt(), any(), any()))
        .willReturn(sliceResponse);

    mockMvc
        .perform(get("/api/v1/users/me/visits").with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].visitId").value(1));
  }

  @Test
  @DisplayName("이용내역 상세 조회 - 정상 200")
  void getVisit_returns200() throws Exception {
    given(visitService.getVisit(anyLong(), anyLong())).willReturn(sampleVisitResponse());

    mockMvc
        .perform(get("/api/v1/visits/1").with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.facilityId").value("FAC001"));
  }

  @Test
  @DisplayName("이용내역 상세 조회 - 존재하지 않으면 404")
  void getVisit_notFound_returns404() throws Exception {
    given(visitService.getVisit(anyLong(), anyLong()))
        .willThrow(new CustomException(ErrorCode.VISIT_NOT_FOUND));

    mockMvc
        .perform(get("/api/v1/visits/999").with(authentication(auth())))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("이용내역 수정 - 정상 200")
  void updateVisit_returns200() throws Exception {
    VisitUpdateRequest request =
        new VisitUpdateRequest(LocalDate.of(2025, 7, 1), LocalTime.of(10, 0), LocalTime.of(14, 0));
    given(visitService.updateVisit(anyLong(), anyLong(), any(VisitUpdateRequest.class)))
        .willReturn(sampleVisitResponse());

    mockMvc
        .perform(
            patch("/api/v1/visits/1")
                .with(authentication(auth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("이용내역 취소 - 정상 200")
  void cancelVisit_returns200() throws Exception {
    given(visitService.cancelVisit(anyLong(), anyLong())).willReturn(sampleVisitResponse());

    mockMvc
        .perform(put("/api/v1/visits/1/cancel").with(authentication(auth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("이용내역 취소 - 이미 취소됨 422")
  void cancelVisit_alreadyCancelled_returns422() throws Exception {
    given(visitService.cancelVisit(anyLong(), anyLong()))
        .willThrow(new CustomException(ErrorCode.VISIT_ALREADY_CANCELLED));

    mockMvc
        .perform(put("/api/v1/visits/1/cancel").with(authentication(auth())))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  @DisplayName("이용내역 확정 - 정상 200")
  void confirmVisit_returns200() throws Exception {
    VisitConfirmRequest request =
        new VisitConfirmRequest(LocalDate.of(2025, 6, 1), LocalTime.of(9, 0), LocalTime.of(13, 0));
    given(visitService.confirmVisit(anyLong(), anyLong(), any(VisitConfirmRequest.class)))
        .willReturn(sampleVisitResponse());

    mockMvc
        .perform(
            put("/api/v1/visits/1/confirm")
                .with(authentication(auth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("이용내역 확정 - 이미 확정됨 422")
  void confirmVisit_alreadyConfirmed_returns422() throws Exception {
    VisitConfirmRequest request =
        new VisitConfirmRequest(LocalDate.of(2025, 6, 1), LocalTime.of(9, 0), LocalTime.of(13, 0));
    given(visitService.confirmVisit(anyLong(), anyLong(), any(VisitConfirmRequest.class)))
        .willThrow(new CustomException(ErrorCode.VISIT_ALREADY_CONFIRMED));

    mockMvc
        .perform(
            put("/api/v1/visits/1/confirm")
                .with(authentication(auth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  @DisplayName("최근 방문 내역 조회 - 정상 200")
  void getRecentVisits_returns200() throws Exception {
    VisitSummaryResponse summary =
        new VisitSummaryResponse(
            1L, "FAC001", "테스트 센터", "지역아동센터", LocalDate.of(2025, 6, 1), VisitStatus.CONFIRMED);
    given(visitService.getRecentVisits(anyLong(), anyInt())).willReturn(List.of(summary));

    mockMvc
        .perform(get("/api/v1/users/me/visits/recent").with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].facilityName").value("테스트 센터"));
  }
}
