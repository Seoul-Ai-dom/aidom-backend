package com.aidom.api.domain.user.service;

import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.domain.user.enums.Role;
import com.aidom.api.domain.user.enums.UserStatus;
import com.aidom.api.domain.user.repository.UserRepository;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

  private final UserRepository userRepository;

  @Transactional
  public User socialLogin(String email, String name, Provider provider, String providerId) {
    Optional<User> providerUser = userRepository.findByProviderAndProviderId(provider, providerId);
    if (providerUser.isPresent()) {
      User user = providerUser.get();
      user.updateOAuthProfile(email, name, providerId);
      return user;
    }

    userRepository
        .findByEmail(email)
        .ifPresent(
            existingUser -> {
              throw new CustomException(ErrorCode.SOCIAL_PROVIDER_MISMATCH);
            });

    User newUser =
        User.createSocialUser(email, name, provider, providerId, Role.USER, UserStatus.ONBOARDING);
    return userRepository.save(newUser);
  }

  public User getUser(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
  }
}
