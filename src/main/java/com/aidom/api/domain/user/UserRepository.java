package com.aidom.api.domain.user;

import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.enums.Provider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}
