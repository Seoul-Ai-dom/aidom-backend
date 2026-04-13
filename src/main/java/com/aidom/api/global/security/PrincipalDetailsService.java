package com.aidom.api.global.security;

import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
    long id;
    try {
      id = Long.parseLong(userId);
    } catch (NumberFormatException e) {
      throw new UsernameNotFoundException("유효하지 않은 사용자 식별자입니다.");
    }

    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다."));
    return new PrincipalDetails(user);
  }

  public PrincipalDetails loadPrincipal(Long userId) {
    return (PrincipalDetails) loadUserByUsername(String.valueOf(userId));
  }
}
