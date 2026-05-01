package com.aidom.api.domain.user.repository;

import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.enums.Provider;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

  Optional<User> findByEmail(String email);

  @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
  Optional<User> findByEmailIncludingDeleted(@Param("email") String email);

  @Query(
      value = "SELECT * FROM users WHERE provider = :provider AND provider_id = :providerId",
      nativeQuery = true)
  Optional<User> findByProviderAndProviderIdIncludingDeleted(
      @Param("provider") String provider, @Param("providerId") String providerId);

  @EntityGraph(attributePaths = "children")
  @Query("select u from User u where u.id = :userId")
  Optional<User> findByIdWithChildren(@Param("userId") Long userId);

  @Query(value = "SELECT * FROM users WHERE user_id = :userId", nativeQuery = true)
  Optional<User> findByIdIncludingDeleted(@Param("userId") Long userId);
}
