package com.aidom.api.domain.facility.repository;

import com.aidom.api.domain.facility.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityRepository extends JpaRepository<Facility, String> {}
