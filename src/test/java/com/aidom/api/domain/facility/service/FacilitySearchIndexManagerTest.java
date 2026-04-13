package com.aidom.api.domain.facility.service;

import static com.aidom.api.global.config.ElasticsearchIndexConstants.FACILITY_INDEX_ALIAS;
import static com.aidom.api.global.config.ElasticsearchIndexConstants.FACILITY_PRIMARY_INDEX;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aidom.api.domain.facility.document.FacilityDocument;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.index.AliasData;
import org.springframework.data.elasticsearch.core.index.Settings;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

@ExtendWith(MockitoExtension.class)
class FacilitySearchIndexManagerTest {

  private static final Clock FIXED_CLOCK =
      Clock.fixed(Instant.parse("2026-04-14T00:00:00Z"), ZoneId.of("Asia/Seoul"));

  @Mock private ElasticsearchOperations operations;
  @Mock private IndexOperations classIndexOperations;
  @Mock private IndexOperations aliasIndexOperations;
  @Mock private IndexOperations primaryIndexOperations;
  @Mock private IndexOperations rebuiltIndexOperations;
  @Mock private Document mapping;

  private final Settings settings = new Settings();

  @Test
  @DisplayName("초기화 시 기본 인덱스를 생성하고 alias를 연결한다")
  void initializeIndex_createsPrimaryIndexAndAlias() {
    FacilitySearchIndexManager manager = new FacilitySearchIndexManager(operations, FIXED_CLOCK);

    when(operations.indexOps(FacilityDocument.class)).thenReturn(classIndexOperations);
    when(operations.indexOps(IndexCoordinates.of(FACILITY_INDEX_ALIAS))).thenReturn(aliasIndexOperations);
    when(operations.indexOps(IndexCoordinates.of(FACILITY_PRIMARY_INDEX))).thenReturn(primaryIndexOperations);
    when(aliasIndexOperations.getAliases(FACILITY_INDEX_ALIAS)).thenReturn(Map.of());
    when(primaryIndexOperations.exists()).thenReturn(false);
    when(classIndexOperations.createSettings()).thenReturn(settings);
    when(classIndexOperations.createMapping()).thenReturn(mapping);

    manager.initializeIndex();

    verify(primaryIndexOperations).create(settings, mapping);
    verify(primaryIndexOperations).alias(any());
  }

  @Test
  @DisplayName("재색인 시 새 인덱스로 alias를 교체하고 이전 인덱스를 삭제한다")
  void rebuildIndex_swapsAliasAndDeletesOldIndices() {
    FacilitySearchIndexManager manager = new FacilitySearchIndexManager(operations, FIXED_CLOCK);
    FacilityDocument document = FacilityDocument.builder().id("FAC001").facilityName("강남 키움센터").build();
    String rebuiltIndexName = FACILITY_PRIMARY_INDEX + "-20260414090000";

    when(operations.indexOps(FacilityDocument.class)).thenReturn(classIndexOperations);
    when(operations.indexOps(IndexCoordinates.of(FACILITY_PRIMARY_INDEX))).thenReturn(primaryIndexOperations);
    when(operations.indexOps(IndexCoordinates.of(rebuiltIndexName))).thenReturn(rebuiltIndexOperations);
    when(operations.indexOps(IndexCoordinates.of(FACILITY_INDEX_ALIAS))).thenReturn(aliasIndexOperations);
    when(rebuiltIndexOperations.exists()).thenReturn(false);
    when(classIndexOperations.createSettings()).thenReturn(settings);
    when(classIndexOperations.createMapping()).thenReturn(mapping);
    when(aliasIndexOperations.getAliases(FACILITY_INDEX_ALIAS))
        .thenReturn(
            Map.of(
                FACILITY_PRIMARY_INDEX,
                Set.of(AliasData.of(FACILITY_INDEX_ALIAS, null, null, null, true, false))));

    manager.rebuildIndex(List.of(document));

    verify(rebuiltIndexOperations).create(settings, mapping);
    verify(operations).save(List.of(document), IndexCoordinates.of(rebuiltIndexName));
    verify(rebuiltIndexOperations).refresh();
    verify(rebuiltIndexOperations).alias(any());
    verify(primaryIndexOperations).delete();
  }

  @Test
  @DisplayName("초기화 시 alias가 이미 있으면 다시 연결하지 않는다")
  void initializeIndex_skipsAliasCreationWhenAliasExists() {
    FacilitySearchIndexManager manager = new FacilitySearchIndexManager(operations, FIXED_CLOCK);

    when(operations.indexOps(IndexCoordinates.of(FACILITY_INDEX_ALIAS))).thenReturn(aliasIndexOperations);
    when(operations.indexOps(IndexCoordinates.of(FACILITY_PRIMARY_INDEX))).thenReturn(primaryIndexOperations);
    when(aliasIndexOperations.getAliases(FACILITY_INDEX_ALIAS))
        .thenReturn(
            Map.of(
                FACILITY_PRIMARY_INDEX,
                Set.of(AliasData.of(FACILITY_INDEX_ALIAS, null, null, null, true, false))));
    when(primaryIndexOperations.exists()).thenReturn(true);

    manager.initializeIndex();

    verify(primaryIndexOperations, never()).alias(any());
  }
}
