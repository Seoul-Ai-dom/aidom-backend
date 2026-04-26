package com.aidom.api.domain.facility.service;

import com.aidom.api.domain.facility.document.FacilityDocument;
import com.aidom.api.domain.facility.document.FacilityDocumentMapper;
import com.aidom.api.domain.facility.entity.Facility;
import com.aidom.api.domain.facility.repository.FacilityRepository;
import com.aidom.api.domain.facility.repository.FacilitySearchRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "spring.data.elasticsearch.repositories.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class FacilityIndexService {

  private final FacilitySearchRepository facilitySearchRepository;
  private final FacilitySearchIndexManager facilitySearchIndexManager;
  private final FacilityRepository facilityRepository;

  public void index(Facility facility) {
    FacilityDocument document = FacilityDocumentMapper.toDocument(facility);
    facilitySearchRepository.save(document);
  }

  public void indexAll(List<Facility> facilities) {
    List<FacilityDocument> documents =
        facilities.stream().map(FacilityDocumentMapper::toDocument).toList();
    facilitySearchRepository.saveAll(documents);
  }

  public void delete(String facilityId) {
    facilitySearchRepository.deleteById(facilityId);
  }

  public int reindexAll() {
    List<Facility> facilities = facilityRepository.findAll();
    List<FacilityDocument> documents =
        facilities.stream().map(FacilityDocumentMapper::toDocument).toList();
    facilitySearchIndexManager.rebuildIndex(documents);
    return documents.size();
  }

  public void reindexAll(List<Facility> facilities) {
    List<FacilityDocument> documents =
        facilities.stream().map(FacilityDocumentMapper::toDocument).toList();
    facilitySearchIndexManager.rebuildIndex(documents);
  }
}
