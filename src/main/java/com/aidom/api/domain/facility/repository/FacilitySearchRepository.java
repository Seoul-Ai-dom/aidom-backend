package com.aidom.api.domain.facility.repository;

import com.aidom.api.domain.facility.document.FacilityDocument;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface FacilitySearchRepository
    extends ElasticsearchRepository<FacilityDocument, String>, FacilitySearchCustomRepository {

  List<FacilityDocument> findByDistrictName(String districtName);

  List<FacilityDocument> findByServiceType(String serviceType);
}
