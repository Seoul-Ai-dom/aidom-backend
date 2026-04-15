package com.aidom.api.domain.facility.document;

import static org.assertj.core.api.Assertions.assertThat;

import com.aidom.api.domain.facility.entity.Facility;
import com.aidom.api.domain.facility.entity.FacilityStats;
import com.aidom.api.domain.facility.enums.ServiceType;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FacilityDocumentMapperTest {

  @Test
  @DisplayName("Facility 엔티티를 FacilityDocument로 변환한다")
  void toDocument_mapsAllFields() {
    Facility facility = createFacility();

    FacilityDocument document = FacilityDocumentMapper.toDocument(facility);

    assertThat(document.getId()).isEqualTo("FAC001");
    assertThat(document.getFacilityName()).isEqualTo("강남 키움센터");
    assertThat(document.getServiceType()).isEqualTo("우리동네키움센터");
    assertThat(document.getDistrictName()).isEqualTo("강남구");
    assertThat(document.getAddress()).isEqualTo("서울특별시 강남구 테헤란로 123");
    assertThat(document.getLocation().getLat()).isEqualTo(37.5665);
    assertThat(document.getLocation().getLon()).isEqualTo(126.978);
    assertThat(document.getAgeMin()).isEqualTo(3);
    assertThat(document.getAgeMax()).isEqualTo(12);
    assertThat(document.isFree()).isTrue();
    assertThat(document.isBookingRequired()).isFalse();
    assertThat(document.isHasRegularCare()).isTrue();
    assertThat(document.isHasTemporaryCare()).isFalse();
    assertThat(document.getAvgRating()).isEqualTo(4.5f);
  }

  @Test
  @DisplayName("lat/lng가 null이면 location은 null이다")
  void toDocument_nullLatLng_locationIsNull() {
    Facility facility =
        Facility.builder()
            .id("FAC002")
            .facilityName("테스트 센터")
            .serviceTypeCode("A")
            .serviceType(ServiceType.CHILD_CENTER)
            .districtCode("11680")
            .districtName("강남구")
            .address("주소")
            .lat(null)
            .lng(null)
            .ageGroup("3~12세")
            .ageMin(3)
            .ageMax(12)
            .bookingRequired(false)
            .isFree(true)
            .hasRegularProgram(false)
            .hasRegularCare(false)
            .hasTemporaryCare(false)
            .build();

    FacilityDocument document = FacilityDocumentMapper.toDocument(facility);

    assertThat(document.getLocation()).isNull();
  }

  @Test
  @DisplayName("stats가 null이면 avgRating은 0.0이다")
  void toDocument_nullStats_avgRatingIsZero() {
    Facility facility =
        Facility.builder()
            .id("FAC003")
            .facilityName("테스트 센터")
            .serviceTypeCode("A")
            .serviceType(ServiceType.CHILD_CENTER)
            .districtCode("11680")
            .districtName("강남구")
            .address("주소")
            .lat(new BigDecimal("37.5665"))
            .lng(new BigDecimal("126.978"))
            .ageGroup("3~12세")
            .ageMin(3)
            .ageMax(12)
            .bookingRequired(false)
            .isFree(true)
            .hasRegularProgram(false)
            .hasRegularCare(false)
            .hasTemporaryCare(false)
            .build();

    FacilityDocument document = FacilityDocumentMapper.toDocument(facility);

    assertThat(document.getAvgRating()).isEqualTo(0.0f);
  }

  private Facility createFacility() {
    Facility facility =
        Facility.builder()
            .id("FAC001")
            .facilityName("강남 키움센터")
            .serviceTypeCode("B")
            .serviceType(ServiceType.KIUM_CENTER)
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
            .hasRegularProgram(true)
            .hasRegularCare(true)
            .hasTemporaryCare(false)
            .build();

    FacilityStats stats =
        FacilityStats.builder()
            .facility(facility)
            .avgRating(new BigDecimal("4.5"))
            .avgRatingSafety(new BigDecimal("4.5"))
            .avgRatingCleanliness(new BigDecimal("4.5"))
            .avgRatingManagement(new BigDecimal("4.5"))
            .avgRatingKindness(new BigDecimal("4.5"))
            .reviewCount(10)
            .build();

    // Use reflection to set the stats field since there's no setter
    try {
      java.lang.reflect.Field statsField = Facility.class.getDeclaredField("stats");
      statsField.setAccessible(true);
      statsField.set(facility, stats);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return facility;
  }
}
