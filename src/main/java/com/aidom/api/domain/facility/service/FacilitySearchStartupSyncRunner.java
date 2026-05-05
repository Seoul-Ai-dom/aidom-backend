package com.aidom.api.domain.facility.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "spring.data.elasticsearch.repositories.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class FacilitySearchStartupSyncRunner implements ApplicationRunner {

  private final FacilitySearchIndexManager facilitySearchIndexManager;
  private final FacilityIndexService facilityIndexService;

  @Override
  public void run(ApplicationArguments args) {
    facilitySearchIndexManager.initializeIndex();

    int syncedCount = facilityIndexService.syncIfEmpty();
    if (syncedCount > 0) {
      log.info("ES 인덱스가 비어 있어 DB 기준으로 시설 {}건을 재색인했습니다.", syncedCount);
    }
  }
}
