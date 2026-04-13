package com.aidom.api.domain.facility.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.aidom.api.domain.facility.document.FacilityDocument;
import com.aidom.api.domain.facility.entity.Facility;
import com.aidom.api.domain.facility.enums.ServiceType;
import com.aidom.api.domain.facility.repository.FacilitySearchRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FacilityIndexServiceTest {

  @Mock private FacilitySearchRepository facilitySearchRepository;
  @Mock private FacilitySearchIndexManager facilitySearchIndexManager;

  @InjectMocks private FacilityIndexService facilityIndexService;

  @Test
  @DisplayName("단건 색인 시 save가 호출된다")
  void index_callsSave() {
    Facility facility = createFacility("FAC001");

    facilityIndexService.index(facility);

    verify(facilitySearchRepository).save(any(FacilityDocument.class));
  }

  @Test
  @DisplayName("일괄 색인 시 saveAll이 호출된다")
  void indexAll_callsSaveAll() {
    List<Facility> facilities = List.of(createFacility("FAC001"), createFacility("FAC002"));

    facilityIndexService.indexAll(facilities);

    verify(facilitySearchRepository).saveAll(anyList());
  }

  @Test
  @DisplayName("삭제 시 deleteById가 호출된다")
  void delete_callsDeleteById() {
    facilityIndexService.delete("FAC001");

    verify(facilitySearchRepository).deleteById(eq("FAC001"));
  }

  @Test
  @DisplayName("전체 재색인 시 deleteAll 후 saveAll이 호출된다")
  void reindexAll_callsDeleteAllThenSaveAll() {
    List<Facility> facilities = List.of(createFacility("FAC001"));

    facilityIndexService.reindexAll(facilities);

    verify(facilitySearchIndexManager).rebuildIndex(anyList());
  }

  private Facility createFacility(String id) {
    return Facility.builder()
        .id(id)
        .facilityName("테스트 센터")
        .serviceTypeCode("A")
        .serviceType(ServiceType.CHILD_CENTER)
        .districtCode("11680")
        .districtName("강남구")
        .address("서울특별시 강남구 테헤란로 123")
        .lat(new BigDecimal("37.5665"))
        .lng(new BigDecimal("126.978"))
        .ageGroup("3~12세")
        .ageMin(3)
        .ageMax(12)
        .bookingRequired(false)
        .isFree(true)
        .hasRegularProgram(false)
        .hasRegularCare(true)
        .hasTemporaryCare(false)
        .build();
  }
}
