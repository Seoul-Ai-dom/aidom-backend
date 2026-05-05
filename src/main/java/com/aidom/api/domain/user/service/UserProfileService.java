package com.aidom.api.domain.user.service;

import com.aidom.api.domain.user.dto.UserMeResponse;
import com.aidom.api.domain.user.dto.UserProfileUpdateRequest;
import com.aidom.api.domain.user.entity.Child;
import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.repository.UserRepository;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

  private final UserRepository userRepository;

  public UserProfileService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public UserMeResponse getMyProfile(Long userId) {
    User user = getUserWithChildren(userId);
    return toResponse(user);
  }

  @Transactional
  public UserMeResponse updateMyProfile(Long userId, UserProfileUpdateRequest request) {
    User user = getUserWithChildren(userId);

    if (request.parentInfo() != null) {
      updateParentProfile(user, request.parentInfo());
    }

    if (request.children() != null) {
      updateChildren(user, request.children());
    }

    return toResponse(user);
  }

  private void updateParentProfile(User user, UserProfileUpdateRequest.ParentInfo parentInfo) {
    user.updateProfile(
        parentInfo.name(),
        parentInfo.relation(),
        parentInfo.birthDate(),
        parentInfo.phoneNumber(),
        parentInfo.address(),
        parentInfo.detailAddress());
  }

  private void updateChildren(User user, List<UserProfileUpdateRequest.ChildInfo> childInfos) {
    List<Child> existingChildren = new ArrayList<>(user.getChildren());

    for (int i = 0; i < childInfos.size(); i++) {
      UserProfileUpdateRequest.ChildInfo childInfo = childInfos.get(i);
      boolean isPrimary = i == 0;

      if (i < existingChildren.size()) {
        Child existingChild = existingChildren.get(i);
        existingChild.updateProfile(
            childInfo.name(), childInfo.birthDate(), childInfo.gender(), childInfo.specialNotes());
        existingChild.markPrimary(isPrimary);
        continue;
      }

      user.addChild(
          Child.of(
              childInfo.name(),
              childInfo.birthDate(),
              childInfo.gender(),
              childInfo.specialNotes(),
              isPrimary));
    }

    for (int i = existingChildren.size() - 1; i >= childInfos.size(); i--) {
      user.removeChild(existingChildren.get(i));
    }
  }

  private User getUserWithChildren(Long userId) {
    return userRepository
        .findByIdWithChildren(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
  }

  private UserMeResponse toResponse(User user) {
    UserMeResponse.ParentInfo parentInfo =
        new UserMeResponse.ParentInfo(
            user.getName(),
            user.getRelation(),
            user.getBirthDate(),
            user.getPhone(),
            user.getAddress(),
            user.getAddressDetail());

    List<UserMeResponse.ChildInfo> children =
        user.getChildren().stream()
            .map(
                child ->
                    new UserMeResponse.ChildInfo(
                        child.getName(),
                        child.getGender(),
                        child.getBirthDate(),
                        child.getSpecialNote()))
            .toList();

    return new UserMeResponse(user.getId(), parentInfo, children);
  }
}
