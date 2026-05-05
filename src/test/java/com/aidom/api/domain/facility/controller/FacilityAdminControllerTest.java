package com.aidom.api.domain.facility.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aidom.api.domain.facility.service.FacilityIndexService;
import com.aidom.api.global.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FacilityAdminController.class)
@Import(SecurityConfig.class)
@TestPropertySource(
    properties = {
      "spring.data.elasticsearch.repositories.enabled=true",
      "aidom.admin.username=admin",
      "aidom.admin.password={noop}admin"
    })
class FacilityAdminControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private FacilityIndexService facilityIndexService;

  @Test
  @DisplayName("관리자 인증이 없으면 재색인 요청은 401을 반환한다")
  void reindex_unauthorized() throws Exception {
    mockMvc.perform(post("/api/v1/admin/facilities/reindex")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("관리자 인증이 있으면 재색인 결과를 반환한다")
  void reindex_authorized() throws Exception {
    given(facilityIndexService.reindexAll()).willReturn(918);

    mockMvc
        .perform(post("/api/v1/admin/facilities/reindex").with(httpBasic("admin", "admin")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.indexed").value(918))
        .andExpect(jsonPath("$.message").value("재색인 완료"));
  }
}
