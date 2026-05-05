package com.aidom.api.domain.facility.repository;

import static com.aidom.api.global.config.ElasticsearchIndexConstants.FACILITY_INDEX_ALIAS;
import static org.assertj.core.api.Assertions.assertThat;

import com.aidom.api.domain.facility.document.FacilityDocument;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("integration-test")
class FacilitySearchRepositoryIntegrationTest {

  @Container
  static GenericContainer<?> elasticsearch =
      new GenericContainer<>(
              new ImageFromDockerfile()
                  .withDockerfileFromBuilder(
                      builder ->
                          builder
                              .from("docker.elastic.co/elasticsearch/elasticsearch:8.13.4")
                              .run("bin/elasticsearch-plugin install analysis-nori")
                              .build()))
          .withEnv("discovery.type", "single-node")
          .withEnv("xpack.security.enabled", "false")
          .withEnv("ES_JAVA_OPTS", "-Xms256m -Xmx256m")
          .withExposedPorts(9200)
          .waitingFor(Wait.forHttp("/_cluster/health").forStatusCode(200));

  @DynamicPropertySource
  static void elasticsearchProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.elasticsearch.uris",
        () -> "http://" + elasticsearch.getHost() + ":" + elasticsearch.getMappedPort(9200));
  }

  @Autowired private FacilitySearchRepository facilitySearchRepository;

  @Autowired private ElasticsearchOperations elasticsearchOperations;

  @BeforeEach
  void setUp() {
    facilitySearchRepository.deleteAll();

    List<FacilityDocument> docs =
        List.of(
            FacilityDocument.builder()
                .id("FAC001")
                .facilityName("강남 키움센터")
                .serviceType("우리동네키움센터")
                .districtName("강남구")
                .address("서울특별시 강남구 테헤란로 123")
                .location(new GeoPoint(37.5665, 126.978))
                .ageMin(3)
                .ageMax(12)
                .isFree(true)
                .bookingRequired(false)
                .hasRegularCare(true)
                .hasTemporaryCare(false)
                .hasRegularProgram(true)
                .avgRating(4.5f)
                .build(),
            FacilityDocument.builder()
                .id("FAC002")
                .facilityName("서초 아동센터")
                .serviceType("지역아동센터")
                .districtName("서초구")
                .address("서울특별시 서초구 서초대로 456")
                .location(new GeoPoint(37.4837, 127.0324))
                .ageMin(5)
                .ageMax(15)
                .isFree(false)
                .bookingRequired(true)
                .hasRegularCare(true)
                .hasTemporaryCare(true)
                .hasRegularProgram(true)
                .avgRating(4.2f)
                .build(),
            FacilityDocument.builder()
                .id("FAC003")
                .facilityName("강남 아동센터")
                .serviceType("지역아동센터")
                .districtName("강남구")
                .address("서울특별시 강남구 역삼동 789")
                .location(new GeoPoint(37.5012, 127.0396))
                .ageMin(6)
                .ageMax(13)
                .isFree(true)
                .bookingRequired(false)
                .hasRegularCare(false)
                .hasTemporaryCare(true)
                .hasRegularProgram(false)
                .avgRating(3.8f)
                .build());

    facilitySearchRepository.saveAll(docs);
    elasticsearchOperations.indexOps(FacilityDocument.class).refresh();
  }

  @Test
  @DisplayName("애플리케이션 시작 시 facilities alias가 기본 인덱스에 연결된다")
  void startup_createsAliasBackedIndex() {
    Map<String, Set<org.springframework.data.elasticsearch.core.index.AliasData>> aliases =
        elasticsearchOperations
            .indexOps(
                org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.of(
                    FACILITY_INDEX_ALIAS))
            .getAliases(FACILITY_INDEX_ALIAS);

    assertThat(aliases).isNotEmpty();
  }

  @Test
  @DisplayName("저장한 문서를 ID로 조회할 수 있다")
  void findById() {
    Optional<FacilityDocument> result = facilitySearchRepository.findById("FAC001");

    assertThat(result).isPresent();
    assertThat(result.get().getFacilityName()).isEqualTo("강남 키움센터");
    assertThat(result.get().getDistrictName()).isEqualTo("강남구");
    assertThat(result.get().getLocation().getLat()).isEqualTo(37.5665);
    assertThat(result.get().getLocation().getLon()).isEqualTo(126.978);
  }

  @Test
  @DisplayName("districtName으로 필터링하면 해당 구의 시설만 반환된다")
  void findByDistrictName() {
    List<FacilityDocument> results = facilitySearchRepository.findByDistrictName("강남구");

    assertThat(results).hasSize(2);
    assertThat(results)
        .extracting(FacilityDocument::getId)
        .containsExactlyInAnyOrder("FAC001", "FAC003");
  }

  @Test
  @DisplayName("serviceType으로 필터링하면 해당 서비스 유형만 반환된다")
  void findByServiceType() {
    List<FacilityDocument> results = facilitySearchRepository.findByServiceType("지역아동센터");

    assertThat(results).hasSize(2);
    assertThat(results)
        .extracting(FacilityDocument::getId)
        .containsExactlyInAnyOrder("FAC002", "FAC003");
  }

  @Test
  @DisplayName("존재하지 않는 districtName으로 조회하면 빈 리스트를 반환한다")
  void findByDistrictName_noResult() {
    List<FacilityDocument> results = facilitySearchRepository.findByDistrictName("마포구");

    assertThat(results).isEmpty();
  }

  @Test
  @DisplayName("문서를 삭제하면 더 이상 조회되지 않는다")
  void deleteById() {
    facilitySearchRepository.deleteById("FAC001");

    Optional<FacilityDocument> result = facilitySearchRepository.findById("FAC001");
    assertThat(result).isEmpty();
    assertThat(facilitySearchRepository.count()).isEqualTo(2);
  }

  @Test
  @DisplayName("전체 문서 수를 확인할 수 있다")
  void count() {
    long count = facilitySearchRepository.count();

    assertThat(count).isEqualTo(3);
  }

  @Test
  @DisplayName("nori 형태소 분석으로 '키움' 검색 시 '강남 키움센터'가 매칭된다")
  void searchByFacilityName_noriPartialMatch() {
    Page<FacilityDocument> results =
        facilitySearchRepository.searchByFacilityName("키움", PageRequest.of(0, 10));

    assertThat(results.getContent()).hasSize(1);
    assertThat(results.getContent().get(0).getId()).isEqualTo("FAC001");
  }

  @Test
  @DisplayName("nori 형태소 분석으로 '아동센터' 검색 시 2건이 매칭된다")
  void searchByFacilityName_noriMultipleMatch() {
    Page<FacilityDocument> results =
        facilitySearchRepository.searchByFacilityName("아동센터", PageRequest.of(0, 10));

    assertThat(results.getContent()).hasSize(2);
    assertThat(results.getContent())
        .extracting(FacilityDocument::getId)
        .containsExactlyInAnyOrder("FAC002", "FAC003");
  }

  @Test
  @DisplayName("searchWithFilters - districtName 필터로 해당 구의 시설만 반환된다")
  void searchWithFilters_districtName() {
    Page<FacilityDocument> results =
        facilitySearchRepository.searchWithFilters(
            "강남구", null, null, null, null, null, null, null, null, null, PageRequest.of(0, 10));

    assertThat(results.getContent()).hasSize(2);
    assertThat(results.getContent())
        .extracting(FacilityDocument::getId)
        .containsExactlyInAnyOrder("FAC001", "FAC003");
  }

  @Test
  @DisplayName("searchWithFilters - serviceType 필터로 해당 유형만 반환된다")
  void searchWithFilters_serviceType() {
    Page<FacilityDocument> results =
        facilitySearchRepository.searchWithFilters(
            null, "지역아동센터", null, null, null, null, null, null, null, null, PageRequest.of(0, 10));

    assertThat(results.getContent()).hasSize(2);
    assertThat(results.getContent())
        .extracting(FacilityDocument::getId)
        .containsExactlyInAnyOrder("FAC002", "FAC003");
  }

  @Test
  @DisplayName("searchWithFilters - isFree 필터로 무료 시설만 반환된다")
  void searchWithFilters_isFree() {
    Page<FacilityDocument> results =
        facilitySearchRepository.searchWithFilters(
            null, null, true, null, null, null, null, null, null, null, PageRequest.of(0, 10));

    assertThat(results.getContent()).hasSize(2);
    assertThat(results.getContent())
        .extracting(FacilityDocument::getId)
        .containsExactlyInAnyOrder("FAC001", "FAC003");
  }

  @Test
  @DisplayName("searchWithFilters - 여러 필터 조합으로 조회한다")
  void searchWithFilters_combined() {
    Page<FacilityDocument> results =
        facilitySearchRepository.searchWithFilters(
            "강남구", null, true, null, null, null, null, null, null, null, PageRequest.of(0, 10));

    assertThat(results.getContent()).hasSize(2);
    assertThat(results.getContent())
        .extracting(FacilityDocument::getId)
        .containsExactlyInAnyOrder("FAC001", "FAC003");
  }

  @Test
  @DisplayName("searchWithFilters - 연령 범위 필터로 해당 연령을 포함하는 시설만 반환된다")
  void searchWithFilters_ageRange() {
    // ageMin=4 이면 시설의 ageMax >= 4인 것만, ageMax=10 이면 시설의 ageMin <= 10인 것만
    Page<FacilityDocument> results =
        facilitySearchRepository.searchWithFilters(
            null, null, null, 4, 5, null, null, null, null, null, PageRequest.of(0, 10));

    // FAC001: 3~12 (포함), FAC002: 5~15 (포함), FAC003: 6~13 (ageMin=6 > ageMax=5 → 제외)
    assertThat(results.getContent()).hasSize(2);
    assertThat(results.getContent())
        .extracting(FacilityDocument::getId)
        .containsExactlyInAnyOrder("FAC001", "FAC002");
  }

  @Test
  @DisplayName("searchNearby - 위치 기반으로 가까운 시설을 반환한다")
  void searchNearby_returnsNearbyFacilities() {
    // 강남역 근처 좌표, 반경 20km (모든 시설 포함할 수 있는 범위)
    List<FacilityDocument> results =
        facilitySearchRepository.searchNearby(37.5665, 126.978, 20.0, 10);

    assertThat(results).isNotEmpty();
    assertThat(results).hasSizeLessThanOrEqualTo(10);
  }

  @Test
  @DisplayName("searchNearby - 좁은 반경에서는 가까운 시설만 반환된다")
  void searchNearby_narrowRadius() {
    // FAC001 좌표와 동일한 지점에서 검색, 0.1km 반경
    List<FacilityDocument> results =
        facilitySearchRepository.searchNearby(37.5665, 126.978, 0.1, 10);

    // 0.1km 반경이므로 FAC001(동일 좌표)만 정확히 1건 반환
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getId()).isEqualTo("FAC001");
  }

  @Test
  @DisplayName("recommendByChildAge - 해당 나이를 포함하는 시설을 추천한다")
  void recommendByChildAge_returnsMatchingFacilities() {
    // 7세 아이 → FAC001(3~12), FAC002(5~15), FAC003(6~13) 모두 매칭
    List<FacilityDocument> results =
        facilitySearchRepository.recommendByChildAge(7, null, null, 10);

    assertThat(results).hasSize(3);
  }

  @Test
  @DisplayName("recommendByChildAge - 범위 밖 나이면 빈 결과를 반환한다")
  void recommendByChildAge_noMatch() {
    // 20세 → 모든 시설의 ageMax보다 큼
    List<FacilityDocument> results =
        facilitySearchRepository.recommendByChildAge(20, null, null, 10);

    assertThat(results).isEmpty();
  }

  @Test
  @DisplayName("recommendByChildAge - 위치 기반 decay가 적용된다")
  void recommendByChildAge_withLocation() {
    List<FacilityDocument> results =
        facilitySearchRepository.recommendByChildAge(7, 37.5665, 126.978, 10);

    assertThat(results).isNotEmpty();
  }

  @Test
  @DisplayName("searchWithFilters - careType=TEMPORARY 필터로 임시돌봄 가능 시설만 반환된다")
  void searchWithFilters_careTypeTemporary() {
    // FAC002: hasTemporaryCare=true, FAC003: hasTemporaryCare=true
    Page<FacilityDocument> results =
        facilitySearchRepository.searchWithFilters(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "TEMPORARY",
            null,
            PageRequest.of(0, 10));

    assertThat(results.getContent()).hasSize(2);
    assertThat(results.getContent())
        .extracting(FacilityDocument::getId)
        .containsExactlyInAnyOrder("FAC002", "FAC003");
  }

  @Test
  @DisplayName("searchWithFilters - careType=REGULAR 필터로 정규돌봄 가능 시설만 반환된다")
  void searchWithFilters_careTypeRegular() {
    // FAC001: hasRegularCare=true, FAC002: hasRegularCare=true
    Page<FacilityDocument> results =
        facilitySearchRepository.searchWithFilters(
            null, null, null, null, null, null, null, null, "REGULAR", null, PageRequest.of(0, 10));

    assertThat(results.getContent()).hasSize(2);
    assertThat(results.getContent())
        .extracting(FacilityDocument::getId)
        .containsExactlyInAnyOrder("FAC001", "FAC002");
  }

  @Test
  @DisplayName("searchWithFilters - hasRegularProgram=true 필터로 정규프로그램 시설만 반환된다")
  void searchWithFilters_hasRegularProgramTrue() {
    // FAC001: hasRegularProgram=true, FAC002: hasRegularProgram=true
    Page<FacilityDocument> results =
        facilitySearchRepository.searchWithFilters(
            null, null, null, null, null, null, null, null, null, true, PageRequest.of(0, 10));

    assertThat(results.getContent()).hasSize(2);
    assertThat(results.getContent())
        .extracting(FacilityDocument::getId)
        .containsExactlyInAnyOrder("FAC001", "FAC002");
  }

  @Test
  @DisplayName("searchWithFilters - hasRegularProgram=false 필터로 정규프로그램 없는 시설만 반환된다")
  void searchWithFilters_hasRegularProgramFalse() {
    // FAC003: hasRegularProgram=false
    Page<FacilityDocument> results =
        facilitySearchRepository.searchWithFilters(
            null, null, null, null, null, null, null, null, null, false, PageRequest.of(0, 10));

    assertThat(results.getContent()).hasSize(1);
    assertThat(results.getContent().get(0).getId()).isEqualTo("FAC003");
  }

  @Test
  @DisplayName("recommendByChildAge - 평점이 높은 시설이 먼저 추천된다")
  void recommendByChildAge_orderedByRating() {
    // 7세: 3개 모두 매칭, avgRating: FAC001=4.5 > FAC002=4.2 > FAC003=3.8
    List<FacilityDocument> results =
        facilitySearchRepository.recommendByChildAge(7, null, null, 10);

    assertThat(results).hasSize(3);
    assertThat(results.get(0).getId()).isEqualTo("FAC001");
  }

  @Test
  @DisplayName("getDistinctDistrictNames - 고유한 자치구명 목록을 반환한다")
  void getDistinctDistrictNames() {
    List<String> districts = facilitySearchRepository.getDistinctDistrictNames();

    assertThat(districts).containsExactlyInAnyOrder("강남구", "서초구");
  }
}
