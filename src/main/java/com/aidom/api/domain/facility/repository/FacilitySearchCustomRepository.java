package com.aidom.api.domain.facility.repository;

import com.aidom.api.domain.facility.document.FacilityDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FacilitySearchCustomRepository {

  Page<FacilityDocument> searchByFacilityName(String keyword, Pageable pageable);
}
