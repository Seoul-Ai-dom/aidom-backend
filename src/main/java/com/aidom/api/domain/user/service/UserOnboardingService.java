package com.aidom.api.domain.user.service;

import com.aidom.api.domain.user.dto.UserOnboardingRequest;
import com.aidom.api.domain.user.dto.UserOnboardingResponse;
import com.aidom.api.domain.user.entity.Child;
import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.enums.SeoulDistrict;
import com.aidom.api.domain.user.enums.UserStatus;
import com.aidom.api.domain.user.repository.UserRepository;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserOnboardingService {

  private static final String SEOUL_CITY = "서울특별시";

  private final UserRepository userRepository;

  public UserOnboardingService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public UserOnboardingResponse completeOnboarding(Long userId, UserOnboardingRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

    if (user.getStatus() != UserStatus.ONBOARDING) {
      throw new CustomException(ErrorCode.BUSINESS_VALIDATION_ERROR);
    }

    UserOnboardingRequest.ParentInfo parentInfo = request.parentInfo();
    SeoulDistrict district = SeoulDistrict.from(parentInfo.district());

    user.completeOnboarding(
        parentInfo.name().trim(),
        parentInfo.birthDate(),
        parentInfo.relation(),
        SEOUL_CITY,
        district.getDescription(),
        parentInfo.phone().trim());

    for (int i = 0; i < request.children().size(); i++) {
      UserOnboardingRequest.ChildInfo childInfo = request.children().get(i);
      boolean isPrimary = i == 0;
      Child child =
          Child.of(
              normalizeName(childInfo.name()),
              childInfo.birthDate(),
              childInfo.gender(),
              normalizeNote(childInfo.specialNote()),
              isPrimary);
      user.addChild(child);
    }

    userRepository.save(user);

    return new UserOnboardingResponse(user.getId(), user.getStatus(), user.getChildren().size());
  }

  private String normalizeNote(String note) {
    if (note == null || note.isBlank()) {
      return null;
    }
    return note.trim();
  }

  private String normalizeName(String name) {
    return name.trim();
  }
}
