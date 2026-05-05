package com.aidom.api.domain.facility.controller;

import com.aidom.api.domain.facility.service.FacilityIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "시설 관리 Admin", description = "시설 데이터 관리 API")
@RestController
@RequestMapping("/api/v1/admin/facilities")
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "spring.data.elasticsearch.repositories.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class FacilityAdminController {

  private final FacilityIndexService facilityIndexService;

  @Operation(
      summary = "ES 전체 재색인",
      description = "DB의 모든 시설 데이터를 Elasticsearch에 재색인합니다. 무중단 alias 스위칭 방식.")
  @ApiResponse(responseCode = "200", description = "재색인 완료")
  @PostMapping("/reindex")
  public ResponseEntity<Map<String, Object>> reindex() {
    int count = facilityIndexService.reindexAll();
    return ResponseEntity.ok(Map.of("indexed", count, "message", "재색인 완료"));
  }
}
