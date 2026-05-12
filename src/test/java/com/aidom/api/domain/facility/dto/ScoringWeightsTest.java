package com.aidom.api.domain.facility.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScoringWeightsTest {

  @Test
  @DisplayName("DEFAULT 상수가 valid이다")
  void defaultIsValid() {
    assertThat(ScoringWeights.DEFAULT.isValid()).isTrue();
  }

  @Test
  @DisplayName("모든 필드가 경계 최솟값이면 valid이다")
  void validBoundaryValues() {
    ScoringWeights weights = new ScoringWeights(0.1, 0.0, 0.0, 0.0, 0.1);
    assertThat(weights.isValid()).isTrue();
  }

  @Test
  @DisplayName("ratingFactor가 0.0이면 invalid이다")
  void ratingFactorTooLow_invalid() {
    ScoringWeights weights = new ScoringWeights(0.0, 0.0, 0.0, 0.0, 0.1);
    assertThat(weights.isValid()).isFalse();
  }

  @Test
  @DisplayName("ratingFactor가 5.1이면 invalid이다")
  void ratingFactorTooHigh_invalid() {
    ScoringWeights weights = new ScoringWeights(5.1, 0.0, 0.0, 0.0, 0.1);
    assertThat(weights.isValid()).isFalse();
  }

  @Test
  @DisplayName("distanceDecay가 1.1이면 invalid이다")
  void distanceDecayTooHigh_invalid() {
    ScoringWeights weights = new ScoringWeights(0.1, 0.0, 0.0, 0.0, 1.1);
    assertThat(weights.isValid()).isFalse();
  }

  @Test
  @DisplayName("모든 필드가 경계 최댓값이면 valid이다")
  void allMaxBoundary_valid() {
    ScoringWeights weights = new ScoringWeights(5.0, 10.0, 10.0, 10.0, 1.0);
    assertThat(weights.isValid()).isTrue();
  }
}
