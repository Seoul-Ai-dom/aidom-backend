package com.aidom.api.domain.facility.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceType {
  CHILD_CENTER("지역아동센터"),
  KIUM_CENTER("우리동네키움센터"),
  KIDS_CAFE("서울형키즈카페"),
  SHARED_CHILDCARE("공동육아방"),
  CHILDCARE_SHARING_CENTER("공동육아나눔터"),
  YOUTH_ACADEMY("청소년방과후아카데미");

  private final String description;

  private static final Map<String, ServiceType> BY_DESCRIPTION =
      Arrays.stream(values())
          .collect(Collectors.toMap(ServiceType::getDescription, Function.identity()));

  public static ServiceType fromDescription(String description) {
    ServiceType type = BY_DESCRIPTION.get(description);
    if (type == null) {
      throw new IllegalArgumentException("Unknown ServiceType description: " + description);
    }
    return type;
  }

  @Converter(autoApply = true)
  public static class ServiceTypeConverter implements AttributeConverter<ServiceType, String> {

    @Override
    public String convertToDatabaseColumn(ServiceType attribute) {
      return attribute == null ? null : attribute.getDescription();
    }

    @Override
    public ServiceType convertToEntityAttribute(String dbData) {
      return dbData == null ? null : ServiceType.fromDescription(dbData);
    }
  }
}
