package com.aidom.api.domain.auth.repository;

import com.aidom.api.domain.auth.entity.AuthCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthCodeRepository extends JpaRepository<AuthCode, Long> {
  Optional<AuthCode> findByCodeHash(String codeHash);
}
