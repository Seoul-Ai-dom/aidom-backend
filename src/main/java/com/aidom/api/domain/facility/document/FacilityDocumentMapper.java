package com.aidom.api.domain.facility.document;

import com.aidom.api.domain.facility.entity.Facility;
import com.aidom.api.domain.facility.entity.FacilityStats;
import java.math.BigDecimal;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

public final class FacilityDocumentMapper {

  private FacilityDocumentMapper() {}

  public static FacilityDocument toDocument(Facility facility) {
    return FacilityDocument.builder()
        .id(facility.getId())
        .facilityName(facility.getFacilityName())
        .serviceType(facility.getServiceType().getDescription())
        .districtName(facility.getDistrictName())
        .address(facility.getAddress())
        .location(toGeoPoint(facility.getLat(), facility.getLng()))
        .ageMin(facility.getAgeMin())
        .ageMax(facility.getAgeMax())
        .isFree(facility.isFree())
        .bookingRequired(facility.isBookingRequired())
        .hasRegularCare(facility.isHasRegularCare())
        .hasTemporaryCare(facility.isHasTemporaryCare())
        .hasRegularProgram(facility.isHasRegularProgram())
        .avgRating(extractAvgRating(facility.getStats()))
        .build();
  }

  private static GeoPoint toGeoPoint(BigDecimal lat, BigDecimal lng) {
    if (lat == null || lng == null) {
      return null;
    }
    return new GeoPoint(lat.doubleValue(), lng.doubleValue());
  }

  private static float extractAvgRating(FacilityStats stats) {
    if (stats == null || stats.getAvgRating() == null) {
      return 0.0f;
    }
    return stats.getAvgRating().floatValue();
  }
}
