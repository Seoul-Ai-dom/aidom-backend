package com.aidom.api.domain.facility.dto;

public record ScoringWeights(
    double ratingFactor,
    double districtMatchWeight,
    double serviceTypeWeight,
    double freeWeight,
    double distanceDecay) {

  public static final ScoringWeights DEFAULT = new ScoringWeights(1.2, 3.0, 2.0, 1.5, 0.5);

  public boolean isValid() {
    return ratingFactor >= 0.1
        && ratingFactor <= 5.0
        && districtMatchWeight >= 0.0
        && districtMatchWeight <= 10.0
        && serviceTypeWeight >= 0.0
        && serviceTypeWeight <= 10.0
        && freeWeight >= 0.0
        && freeWeight <= 10.0
        && distanceDecay >= 0.1
        && distanceDecay <= 1.0;
  }
}
