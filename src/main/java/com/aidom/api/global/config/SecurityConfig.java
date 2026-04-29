package com.aidom.api.global.config;

import com.aidom.api.domain.auth.service.CustomOAuth2UserService;
import com.aidom.api.domain.auth.service.OAuth2AuthenticationSuccessHandler;
import com.aidom.api.domain.user.enums.Role;
import com.aidom.api.domain.user.enums.UserStatus;
import com.aidom.api.global.security.AuthenticatedUserPrincipal;
import com.aidom.api.global.security.JwtAuthenticationFilter;
import com.aidom.api.global.security.ProblemDetailAccessDeniedHandler;
import com.aidom.api.global.security.ProblemDetailAuthenticationEntryPoint;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableConfigurationProperties(AppAuthProperties.class)
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      ProblemDetailAuthenticationEntryPoint authenticationEntryPoint,
      ProblemDetailAccessDeniedHandler accessDeniedHandler,
      CustomOAuth2UserService customOAuth2UserService,
      OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
      ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider,
      AppAuthProperties appAuthProperties)
      throws Exception {

    http.csrf(csrf -> csrf.disable())
        .cors(
            cors ->
                cors.configurationSource(
                    request -> {
                      CorsConfiguration config = new CorsConfiguration();
                      config.setAllowedOrigins(appAuthProperties.getCors().getAllowedOrigins());
                      config.setAllowedMethods(
                          List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                      config.setAllowedHeaders(List.of("*"));
                      config.setAllowCredentials(true);
                      return config;
                    }))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/api/v1/auth/**",
                        "/oauth2/**",
                        "/login/oauth2/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/health")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/facilities/**")
                    .permitAll()
                    .requestMatchers("/api/v1/users/me/onboarding")
                    .authenticated()
                    .anyRequest()
                    .access(
                        (authentication, context) ->
                            new AuthorizationDecision(isActiveOrAdmin(authentication.get()))))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    if (clientRegistrationRepositoryProvider.getIfAvailable() != null) {
      http.oauth2Login(
          oauth2 ->
              oauth2
                  .userInfoEndpoint(endpoint -> endpoint.userService(customOAuth2UserService))
                  .successHandler(oAuth2AuthenticationSuccessHandler));
    }
    return http.build();
  }

  private boolean isActiveOrAdmin(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof AuthenticatedUserPrincipal userPrincipal)) {
      return false;
    }

    return userPrincipal.role() == Role.ADMIN || userPrincipal.status() == UserStatus.ACTIVE;
  }
}
