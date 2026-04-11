package com.aidom.api.domain.facility.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "facilities")
@Getter
@NoArgsConstructor
public class Facility {

  @Id
  @Column(name = "facility_id", length = 20)
  private String id;
}
