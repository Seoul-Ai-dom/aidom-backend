package com.aidom.api.domain.facility.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

class FacilityDocumentTest {

  @Test
  @DisplayName("Builder로 FacilityDocument를 생성하면 모든 필드가 올바르게 설정된다")
  void builder_setsAllFields() {
    GeoPoint location = new GeoPoint(37.5665, 126.978);

    FacilityDocument doc =
        FacilityDocument.builder()
            .id("FAC001")
            .facilityName("강남 키움센터")
            .serviceType("우리동네키움센터")
            .districtName("강남구")
            .address("서울특별시 강남구 테헤란로 123")
            .location(location)
            .ageMin(3)
            .ageMax(12)
            .isFree(true)
            .bookingRequired(false)
            .hasRegularCare(true)
            .hasTemporaryCare(false)
            .avgRating(4.5f)
            .build();

    assertThat(doc.getId()).isEqualTo("FAC001");
    assertThat(doc.getFacilityName()).isEqualTo("강남 키움센터");
    assertThat(doc.getServiceType()).isEqualTo("우리동네키움센터");
    assertThat(doc.getDistrictName()).isEqualTo("강남구");
    assertThat(doc.getAddress()).isEqualTo("서울특별시 강남구 테헤란로 123");
    assertThat(doc.getLocation().getLat()).isEqualTo(37.5665);
    assertThat(doc.getLocation().getLon()).isEqualTo(126.978);
    assertThat(doc.getAgeMin()).isEqualTo(3);
    assertThat(doc.getAgeMax()).isEqualTo(12);
    assertThat(doc.isFree()).isTrue();
    assertThat(doc.isBookingRequired()).isFalse();
    assertThat(doc.isHasRegularCare()).isTrue();
    assertThat(doc.isHasTemporaryCare()).isFalse();
    assertThat(doc.getAvgRating()).isEqualTo(4.5f);
  }

  @Test
  @DisplayName("Builder 기본값으로 생성하면 숫자 필드는 0, boolean 필드는 false이다")
  void builder_defaultValues() {
    FacilityDocument doc = FacilityDocument.builder().id("FAC002").build();

    assertThat(doc.getId()).isEqualTo("FAC002");
    assertThat(doc.getFacilityName()).isNull();
    assertThat(doc.getLocation()).isNull();
    assertThat(doc.getAgeMin()).isZero();
    assertThat(doc.getAgeMax()).isZero();
    assertThat(doc.isFree()).isFalse();
    assertThat(doc.isBookingRequired()).isFalse();
    assertThat(doc.getAvgRating()).isZero();
  }

  @Test
  @DisplayName("GeoPoint 없이 생성할 수 있다")
  void builder_withoutGeoPoint() {
    FacilityDocument doc =
        FacilityDocument.builder().id("FAC003").facilityName("종로 아동센터").districtName("종로구").build();

    assertThat(doc.getId()).isEqualTo("FAC003");
    assertThat(doc.getLocation()).isNull();
    assertThat(doc.getFacilityName()).isEqualTo("종로 아동센터");
    assertThat(doc.getDistrictName()).isEqualTo("종로구");
  }
}
