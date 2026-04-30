package com.aidom.api.domain.user.service;

import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.domain.user.repository.UserRepository;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
public class UserWithdrawalService {

  private final UserRepository userRepository;
  private final KakaoUnlinkClient kakaoUnlinkClient;

  public UserWithdrawalService(UserRepository userRepository, KakaoUnlinkClient kakaoUnlinkClient) {
    this.userRepository = userRepository;
    this.kakaoUnlinkClient = kakaoUnlinkClient;
  }

  public void withdraw(Long userId) {
    User user =
        userRepository
            .findByIdIncludingDeleted(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

    if (user.isWithdrawn() || user.getDeletedAt() != null) {
      throw new CustomException(ErrorCode.ALREADY_WITHDRAWN_USER);
    }

    if (user.getProvider() == Provider.KAKAO) {
      kakaoUnlinkClient.unlink(parseKakaoUserId(user.getProviderId()));
    }

    softDelete(userId);
  }

  private Long parseKakaoUserId(String providerId) {
    try {
      return Long.parseLong(providerId);
    } catch (NumberFormatException e) {
      throw new CustomException(ErrorCode.KAKAO_UNLINK_FAILED);
    }
  }

  private void softDelete(Long userId) {
    try {
      userRepository.deleteById(userId);
    } catch (EmptyResultDataAccessException e) {
      throw new CustomException(ErrorCode.ALREADY_WITHDRAWN_USER);
    }
  }
}
