package com.aidom.api.domain.facility.service;

import static com.aidom.api.global.config.ElasticsearchIndexConstants.FACILITY_INDEX_ALIAS;
import static com.aidom.api.global.config.ElasticsearchIndexConstants.FACILITY_PRIMARY_INDEX;

import com.aidom.api.domain.facility.document.FacilityDocument;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "spring.data.elasticsearch.repositories.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class FacilitySearchIndexManager implements ApplicationRunner {

  private static final DateTimeFormatter REINDEX_SUFFIX_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private final ElasticsearchOperations operations;
  private final Clock clock;

  @Override
  public void run(ApplicationArguments args) {
    initializeIndex();
  }

  public void initializeIndex() {
    ensureIndexExists(FACILITY_PRIMARY_INDEX);

    if (findAliasIndices().isEmpty()) {
      attachAlias(FACILITY_PRIMARY_INDEX);
    }
  }

  public void rebuildIndex(List<FacilityDocument> documents) {
    String newIndexName =
        FACILITY_PRIMARY_INDEX
            + "-"
            + LocalDateTime.now(clock).format(REINDEX_SUFFIX_FORMATTER);

    ensureIndexExists(newIndexName);

    if (!documents.isEmpty()) {
      operations.save(documents, IndexCoordinates.of(newIndexName));
      operations.indexOps(IndexCoordinates.of(newIndexName)).refresh();
    }

    Set<String> previousIndices = findAliasIndices();
    switchAlias(previousIndices, newIndexName);
    deleteIndices(previousIndices);
  }

  Set<String> findAliasIndices() {
    try {
      Map<String, Set<org.springframework.data.elasticsearch.core.index.AliasData>> aliases =
          operations
              .indexOps(IndexCoordinates.of(FACILITY_INDEX_ALIAS))
              .getAliases(FACILITY_INDEX_ALIAS);
      return new LinkedHashSet<>(aliases.keySet());
    } catch (ResourceNotFoundException e) {
      return new LinkedHashSet<>();
    }
  }

  private void ensureIndexExists(String indexName) {
    IndexOperations targetIndexOps = operations.indexOps(IndexCoordinates.of(indexName));
    if (targetIndexOps.exists()) {
      return;
    }

    IndexOperations documentIndexOps = operations.indexOps(FacilityDocument.class);
    targetIndexOps.create(documentIndexOps.createSettings(), documentIndexOps.createMapping());
  }

  private void attachAlias(String indexName) {
    AliasActionParameters parameters =
        AliasActionParameters.builder()
            .withIndices(indexName)
            .withAliases(FACILITY_INDEX_ALIAS)
            .withIsWriteIndex(true)
            .build();

    operations
        .indexOps(IndexCoordinates.of(indexName))
        .alias(new AliasActions(new AliasAction.Add(parameters)));
  }

  private void switchAlias(Set<String> previousIndices, String newIndexName) {
    List<AliasAction> actions = new ArrayList<>();

    for (String previousIndex : previousIndices) {
      if (!previousIndex.equals(newIndexName)) {
        actions.add(
            new AliasAction.Remove(
                AliasActionParameters.builder()
                    .withIndices(previousIndex)
                    .withAliases(FACILITY_INDEX_ALIAS)
                    .build()));
      }
    }

    actions.add(
        new AliasAction.Add(
            AliasActionParameters.builder()
                .withIndices(newIndexName)
                .withAliases(FACILITY_INDEX_ALIAS)
                .withIsWriteIndex(true)
                .build()));

    operations
        .indexOps(IndexCoordinates.of(newIndexName))
        .alias(new AliasActions(actions.toArray(AliasAction[]::new)));
  }

  private void deleteIndices(Set<String> indexNames) {
    for (String indexName : indexNames) {
      operations.indexOps(IndexCoordinates.of(indexName)).delete();
    }
  }
}
