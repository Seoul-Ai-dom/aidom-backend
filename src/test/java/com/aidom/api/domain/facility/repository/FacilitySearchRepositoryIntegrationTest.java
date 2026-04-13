package com.aidom.api.domain.facility.repository;

import static org.assertj.core.api.Assertions.assertThat;

import static com.aidom.api.global.config.ElasticsearchIndexConstants.FACILITY_INDEX_ALIAS;

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
@Testcontainers
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
                .avgRating(3.8f)
                .build());

    facilitySearchRepository.saveAll(docs);
  }

  @Test
  @DisplayName("애플리케이션 시작 시 facilities alias가 기본 인덱스에 연결된다")
  void startup_createsAliasBackedIndex() {
    Map<String, Set<org.springframework.data.elasticsearch.core.index.AliasData>> aliases =
        elasticsearchOperations
            .indexOps(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.of(FACILITY_INDEX_ALIAS))
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
}
