package com.aidom.api.domain.user.repository;

import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.enums.Provider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

  @Query(value = "SELECT * FROM users WHERE user_id = :userId", nativeQuery = true)
  Optional<User> findByIdIncludingDeleted(@Param("userId") Long userId);
}
