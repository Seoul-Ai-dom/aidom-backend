package com.aidom.api.global.security;

import com.aidom.api.domain.user.UserRepository;
import com.aidom.api.domain.user.entity.User;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (bearer != null && bearer.startsWith("Bearer ")) {
      String token = bearer.substring(7);
      AuthenticatedUserPrincipal principal = jwtTokenProvider.getPrincipalFromAccessToken(token);

      User user =
          userRepository
              .findById(principal.userId())
              .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

      AuthenticatedUserPrincipal refreshedPrincipal =
          new AuthenticatedUserPrincipal(
              user.getId(), user.getRole(), user.getStatus(), user.getProvider(), user.getEmail());

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              refreshedPrincipal,
              null,
              List.of(new SimpleGrantedAuthority("ROLE_" + refreshedPrincipal.role().name())));

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }
}
