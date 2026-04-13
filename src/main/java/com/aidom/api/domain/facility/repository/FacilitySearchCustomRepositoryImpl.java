package com.aidom.api.domain.facility.repository;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.aidom.api.domain.facility.document.FacilityDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;

@RequiredArgsConstructor
public class FacilitySearchCustomRepositoryImpl implements FacilitySearchCustomRepository {

  private final ElasticsearchOperations operations;

  @Override
  public Page<FacilityDocument> searchByFacilityName(String keyword, Pageable pageable) {
    NativeQuery query =
        NativeQuery.builder()
            .withQuery(
                q -> q.match(m -> m.field("facilityName").query(keyword).operator(Operator.And)))
            .withPageable(pageable)
            .build();

    SearchHits<FacilityDocument> hits = operations.search(query, FacilityDocument.class);

    return SearchHitSupport.searchPageFor(hits, pageable).map(searchHit -> searchHit.getContent());
  }
}
