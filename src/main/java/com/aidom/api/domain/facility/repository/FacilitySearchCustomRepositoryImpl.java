package com.aidom.api.domain.facility.repository;

import co.elastic.clients.elasticsearch._types.GeoDistanceType;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.aidom.api.domain.facility.document.FacilityDocument;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
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

  @Override
  public Page<FacilityDocument> searchWithFilters(
      String districtName,
      String serviceType,
      Boolean isFree,
      Integer ageMin,
      Integer ageMax,
      BigDecimal lat,
      BigDecimal lng,
      BigDecimal radius,
      String careType,
      Boolean hasRegularProgram,
      Pageable pageable) {

    var builder = NativeQuery.builder();

    builder.withQuery(
        q ->
            q.bool(
                b -> {
                  if (districtName != null) {
                    b.filter(f -> f.term(t -> t.field("districtName").value(districtName)));
                  }
                  if (serviceType != null) {
                    b.filter(f -> f.term(t -> t.field("serviceType").value(serviceType)));
                  }
                  if (isFree != null) {
                    b.filter(f -> f.term(t -> t.field("isFree").value(isFree)));
                  }
                  if (ageMin != null) {
                    b.filter(
                        f -> f.range(r -> r.number(n -> n.field("ageMax").gte((double) ageMin))));
                  }
                  if (ageMax != null) {
                    b.filter(
                        f -> f.range(r -> r.number(n -> n.field("ageMin").lte((double) ageMax))));
                  }
                  if (lat != null && lng != null && radius != null) {
                    b.filter(
                        f ->
                            f.geoDistance(
                                g ->
                                    g.field("location")
                                        .location(
                                            l ->
                                                l.latlon(
                                                    ll ->
                                                        ll.lat(lat.doubleValue())
                                                            .lon(lng.doubleValue())))
                                        .distance(radius.toPlainString() + "km")
                                        .distanceType(GeoDistanceType.Arc)));
                  }
                  if ("TEMPORARY".equals(careType)) {
                    b.filter(f -> f.term(t -> t.field("hasTemporaryCare").value(true)));
                  } else if ("REGULAR".equals(careType)) {
                    b.filter(f -> f.term(t -> t.field("hasRegularCare").value(true)));
                  }
                  if (hasRegularProgram != null) {
                    b.filter(
                        f -> f.term(t -> t.field("hasRegularProgram").value(hasRegularProgram)));
                  }
                  return b;
                }));

    if (lat != null && lng != null) {
      builder.withSort(
          s ->
              s.geoDistance(
                  g ->
                      g.field("location")
                          .location(
                              l -> l.latlon(ll -> ll.lat(lat.doubleValue()).lon(lng.doubleValue())))
                          .order(SortOrder.Asc)
                          .unit(co.elastic.clients.elasticsearch._types.DistanceUnit.Kilometers)));
    }

    builder.withPageable(pageable);

    NativeQuery query = builder.build();
    SearchHits<FacilityDocument> hits = operations.search(query, FacilityDocument.class);

    return SearchHitSupport.searchPageFor(hits, pageable).map(searchHit -> searchHit.getContent());
  }

  @Override
  public List<FacilityDocument> searchNearby(double lat, double lng, double radiusKm, int limit) {
    NativeQuery query =
        NativeQuery.builder()
            .withQuery(
                q ->
                    q.bool(
                        b ->
                            b.filter(
                                f ->
                                    f.geoDistance(
                                        g ->
                                            g.field("location")
                                                .location(l -> l.latlon(ll -> ll.lat(lat).lon(lng)))
                                                .distance(radiusKm + "km")
                                                .distanceType(GeoDistanceType.Arc)))))
            .withSort(
                s ->
                    s.geoDistance(
                        g ->
                            g.field("location")
                                .location(l -> l.latlon(ll -> ll.lat(lat).lon(lng)))
                                .order(SortOrder.Asc)
                                .unit(
                                    co.elastic.clients.elasticsearch._types.DistanceUnit
                                        .Kilometers)))
            .withPageable(PageRequest.of(0, limit))
            .build();

    SearchHits<FacilityDocument> hits = operations.search(query, FacilityDocument.class);

    return hits.getSearchHits().stream().map(SearchHit::getContent).toList();
  }

  @Override
  public List<FacilityDocument> recommendByChildAge(
      int childAge, Double lat, Double lng, int limit) {

    NativeQuery query =
        NativeQuery.builder()
            .withQuery(
                q ->
                    q.functionScore(
                        fs -> {
                          fs.query(
                              mq ->
                                  mq.bool(
                                      b ->
                                          b.filter(
                                                  f ->
                                                      f.range(
                                                          r ->
                                                              r.number(
                                                                  n ->
                                                                      n.field("ageMin")
                                                                          .lte((double) childAge))))
                                              .filter(
                                                  f ->
                                                      f.range(
                                                          r ->
                                                              r.number(
                                                                  n ->
                                                                      n.field("ageMax")
                                                                          .gte(
                                                                              (double)
                                                                                  childAge))))));

                          fs.functions(
                              fn ->
                                  fn.fieldValueFactor(
                                      fvf ->
                                          fvf.field("avgRating")
                                              .factor(1.2)
                                              .modifier(
                                                  co.elastic.clients.elasticsearch._types.query_dsl
                                                      .FieldValueFactorModifier.Log1p)
                                              .missing(0.0)));

                          if (lat != null && lng != null) {
                            fs.functions(
                                fn ->
                                    fn.exp(
                                        d ->
                                            d.geo(
                                                g ->
                                                    g.field("location")
                                                        .placement(
                                                            p ->
                                                                p.origin(
                                                                        co.elastic.clients
                                                                            .elasticsearch._types
                                                                            .GeoLocation.of(
                                                                            o ->
                                                                                o.latlon(
                                                                                    ll ->
                                                                                        ll.lat(lat)
                                                                                            .lon(
                                                                                                lng))))
                                                                    .scale("3km")
                                                                    .offset("0km")
                                                                    .decay(0.5)))));
                          }

                          fs.scoreMode(FunctionScoreMode.Sum);
                          fs.boostMode(FunctionBoostMode.Replace);
                          return fs;
                        }))
            .withPageable(PageRequest.of(0, limit))
            .build();

    SearchHits<FacilityDocument> hits = operations.search(query, FacilityDocument.class);

    return hits.getSearchHits().stream().map(SearchHit::getContent).toList();
  }

  @Override
  public List<String> getDistinctDistrictNames() {
    NativeQuery query =
        NativeQuery.builder()
            .withQuery(q -> q.matchAll(m -> m))
            .withAggregation(
                "distinct_districts",
                co.elastic.clients.elasticsearch._types.aggregations.Aggregation.of(
                    a -> a.terms(t -> t.field("districtName").size(100))))
            .withMaxResults(0)
            .build();

    SearchHits<FacilityDocument> hits = operations.search(query, FacilityDocument.class);

    List<String> districts = new ArrayList<>();
    if (hits.hasAggregations()) {
      var aggregations =
          (org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations)
              hits.getAggregations();
      if (aggregations != null) {
        var termsAgg =
            aggregations
                .aggregationsAsMap()
                .get("distinct_districts")
                .aggregation()
                .getAggregate()
                .sterms();
        for (var bucket : termsAgg.buckets().array()) {
          districts.add(bucket.key().stringValue());
        }
      }
    }

    return districts;
  }
}
