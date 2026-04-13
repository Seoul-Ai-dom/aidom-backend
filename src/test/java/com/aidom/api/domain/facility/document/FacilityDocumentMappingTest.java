package com.aidom.api.domain.facility.document;

import static org.assertj.core.api.Assertions.assertThat;

import static com.aidom.api.global.config.ElasticsearchIndexConstants.FACILITY_INDEX_ALIAS;

import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;

class FacilityDocumentMappingTest {

  @Test
  @DisplayName("클래스에 @Document 어노테이션이 facilities 인덱스로 설정되어 있다")
  void classHasDocumentAnnotation() {
    Document annotation = FacilityDocument.class.getAnnotation(Document.class);

    assertThat(annotation).isNotNull();
    assertThat(annotation.indexName()).isEqualTo(FACILITY_INDEX_ALIAS);
    assertThat(annotation.createIndex()).isFalse();
  }

  @Test
  @DisplayName("id 필드에 @Id 어노테이션이 있다")
  void idFieldHasIdAnnotation() throws NoSuchFieldException {
    Field idField = FacilityDocument.class.getDeclaredField("id");

    assertThat(idField.getAnnotation(Id.class)).isNotNull();
  }

  @Test
  @DisplayName("facilityName 필드는 Text 타입에 nori 분석기가 설정되어 있다")
  void facilityNameFieldHasNoriAnalyzer() throws NoSuchFieldException {
    Field field = FacilityDocument.class.getDeclaredField("facilityName");
    org.springframework.data.elasticsearch.annotations.Field annotation =
        field.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);

    assertThat(annotation).isNotNull();
    assertThat(annotation.type()).isEqualTo(FieldType.Text);
    assertThat(annotation.analyzer()).isEqualTo("facility_nori");
  }

  @Test
  @DisplayName("serviceType과 districtName 필드는 Keyword 타입이다")
  void keywordFields() throws NoSuchFieldException {
    Field serviceTypeField = FacilityDocument.class.getDeclaredField("serviceType");
    Field districtNameField = FacilityDocument.class.getDeclaredField("districtName");

    assertThat(
            serviceTypeField
                .getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class)
                .type())
        .isEqualTo(FieldType.Keyword);
    assertThat(
            districtNameField
                .getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class)
                .type())
        .isEqualTo(FieldType.Keyword);
  }

  @Test
  @DisplayName("location 필드에 @GeoPointField 어노테이션이 있다")
  void locationFieldHasGeoPointAnnotation() throws NoSuchFieldException {
    Field field = FacilityDocument.class.getDeclaredField("location");

    assertThat(field.getAnnotation(GeoPointField.class)).isNotNull();
  }

  @Test
  @DisplayName("avgRating 필드는 Float 타입이다")
  void avgRatingFieldIsFloat() throws NoSuchFieldException {
    Field field = FacilityDocument.class.getDeclaredField("avgRating");
    org.springframework.data.elasticsearch.annotations.Field annotation =
        field.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);

    assertThat(annotation).isNotNull();
    assertThat(annotation.type()).isEqualTo(FieldType.Float);
  }

  @Test
  @DisplayName("Boolean 필드들이 올바른 타입으로 매핑되어 있다")
  void booleanFieldsMapping() throws NoSuchFieldException {
    String[] booleanFields = {"isFree", "bookingRequired", "hasRegularCare", "hasTemporaryCare"};

    for (String fieldName : booleanFields) {
      Field field = FacilityDocument.class.getDeclaredField(fieldName);
      org.springframework.data.elasticsearch.annotations.Field annotation =
          field.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);

      assertThat(annotation).as("Field %s should have @Field annotation", fieldName).isNotNull();
      assertThat(annotation.type())
          .as("Field %s should be Boolean type", fieldName)
          .isEqualTo(FieldType.Boolean);
    }
  }

  @Test
  @DisplayName("Integer 필드들이 올바른 타입으로 매핑되어 있다")
  void integerFieldsMapping() throws NoSuchFieldException {
    String[] intFields = {"ageMin", "ageMax"};

    for (String fieldName : intFields) {
      Field field = FacilityDocument.class.getDeclaredField(fieldName);
      org.springframework.data.elasticsearch.annotations.Field annotation =
          field.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);

      assertThat(annotation).as("Field %s should have @Field annotation", fieldName).isNotNull();
      assertThat(annotation.type())
          .as("Field %s should be Integer type", fieldName)
          .isEqualTo(FieldType.Integer);
    }
  }
}
