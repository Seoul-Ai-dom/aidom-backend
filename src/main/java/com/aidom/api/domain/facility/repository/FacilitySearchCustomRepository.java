package com.aidom.api.domain.facility.repository;

import com.aidom.api.domain.facility.document.FacilityDocument;
import com.aidom.api.domain.facility.dto.ScoringWeights;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FacilitySearchCustomRepository {

  Page<FacilityDocument> searchByFacilityName(String keyword, Pageable pageable);

  Page<FacilityDocument> searchWithFilters(
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
      Pageable pageable);

  List<FacilityDocument> searchNearby(double lat, double lng, double radiusKm, int limit);

  List<FacilityDocument> recommendByChildAge(
      int childAge,
      Double lat,
      Double lng,
      Set<String> excludeFacilityIds,
      Set<String> preferredServiceTypes,
      String userDistrict,
      int limit);

  List<FacilityDocument> recommendByChildAge(
      int childAge,
      Double lat,
      Double lng,
      Set<String> excludeFacilityIds,
      Set<String> preferredServiceTypes,
      String userDistrict,
      int limit,
      ScoringWeights weights);

  List<String> getDistinctDistrictNames();
}
