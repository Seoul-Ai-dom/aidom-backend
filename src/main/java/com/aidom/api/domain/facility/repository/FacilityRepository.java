package com.aidom.api.domain.facility.repository;

import com.aidom.api.domain.facility.entity.Facility;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FacilityRepository extends JpaRepository<Facility, String> {

  @EntityGraph(attributePaths = "stats")
  @Query("select f from Facility f")
  List<Facility> findAllWithStats();
}
