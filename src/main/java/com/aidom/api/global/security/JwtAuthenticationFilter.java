package com.aidom.api.global.security;

import com.aidom.api.domain.user.entity.User;
import com.aidom.api.domain.user.repository.UserRepository;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  public static final String AUTH_ERROR_CODE_ATTR = "authErrorCode";

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private final ProblemDetailAuthenticationEntryPoint authenticationEntryPoint;

  public JwtAuthenticationFilter(
      JwtTokenProvider jwtTokenProvider,
      UserRepository userRepository,
      ProblemDetailAuthenticationEntryPoint authenticationEntryPoint) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userRepository = userRepository;
    this.authenticationEntryPoint = authenticationEntryPoint;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
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
                user.getId(),
                user.getRole(),
                user.getStatus(),
                user.getProvider(),
                user.getEmail());

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                refreshedPrincipal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + refreshedPrincipal.role().name())));

        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (CustomException e) {
      SecurityContextHolder.clearContext();
      request.setAttribute(AUTH_ERROR_CODE_ATTR, e.getErrorCode());
      authenticationEntryPoint.commence(
          request, response, new InsufficientAuthenticationException(e.getMessage(), e));
      return;
    }

    filterChain.doFilter(request, response);
  }
}
