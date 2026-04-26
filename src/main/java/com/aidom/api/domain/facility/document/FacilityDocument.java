package com.aidom.api.domain.facility.document;

import static com.aidom.api.global.config.ElasticsearchIndexConstants.FACILITY_INDEX_ALIAS;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Document(indexName = FACILITY_INDEX_ALIAS, createIndex = false)
@Setting(settingPath = "/elasticsearch/facilities-settings.json")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FacilityDocument {

  @Id private String id;

  @Field(type = FieldType.Text, analyzer = "facility_nori")
  private String facilityName;

  @Field(type = FieldType.Keyword)
  private String serviceType;

  @Field(type = FieldType.Keyword)
  private String districtName;

  @Field(type = FieldType.Text)
  private String address;

  @GeoPointField private GeoPoint location;

  @Field(type = FieldType.Integer)
  private int ageMin;

  @Field(type = FieldType.Integer)
  private int ageMax;

  @Field(type = FieldType.Boolean)
  private boolean isFree;

  @Field(type = FieldType.Boolean)
  private boolean bookingRequired;

  @Field(type = FieldType.Boolean)
  private boolean hasRegularCare;

  @Field(type = FieldType.Boolean)
  private boolean hasTemporaryCare;

  @Field(type = FieldType.Boolean)
  private boolean hasRegularProgram;

  @Field(type = FieldType.Float)
  private float avgRating;

  @Builder
  private FacilityDocument(
      String id,
      String facilityName,
      String serviceType,
      String districtName,
      String address,
      GeoPoint location,
      int ageMin,
      int ageMax,
      boolean isFree,
      boolean bookingRequired,
      boolean hasRegularCare,
      boolean hasTemporaryCare,
      boolean hasRegularProgram,
      float avgRating) {
    this.id = id;
    this.facilityName = facilityName;
    this.serviceType = serviceType;
    this.districtName = districtName;
    this.address = address;
    this.location = location;
    this.ageMin = ageMin;
    this.ageMax = ageMax;
    this.isFree = isFree;
    this.bookingRequired = bookingRequired;
    this.hasRegularCare = hasRegularCare;
    this.hasTemporaryCare = hasTemporaryCare;
    this.hasRegularProgram = hasRegularProgram;
    this.avgRating = avgRating;
  }
}
