package com.aidom.api.domain.facility.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FacilitySearchStartupSyncRunnerTest {

  @Mock private FacilitySearchIndexManager facilitySearchIndexManager;
  @Mock private FacilityIndexService facilityIndexService;

  @InjectMocks private FacilitySearchStartupSyncRunner facilitySearchStartupSyncRunner;

  @Test
  @DisplayName("시작 시 인덱스를 초기화하고 비어 있으면 동기화를 수행한다")
  void run_initializesIndexAndSyncsWhenEmpty() throws Exception {
    when(facilityIndexService.syncIfEmpty()).thenReturn(918);

    facilitySearchStartupSyncRunner.run(null);

    verify(facilitySearchIndexManager).initializeIndex();
    verify(facilityIndexService).syncIfEmpty();
  }

  @Test
  @DisplayName("시작 시 인덱스를 초기화하고 데이터가 있으면 추가 동기화를 건너뛴다")
  void run_initializesIndexAndSkipsSyncWhenNotEmpty() throws Exception {
    when(facilityIndexService.syncIfEmpty()).thenReturn(0);

    facilitySearchStartupSyncRunner.run(null);

    verify(facilitySearchIndexManager).initializeIndex();
    verify(facilityIndexService).syncIfEmpty();
  }
}
