package com.aidom.api.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      @Value("${aidom.admin.username:}") String username,
      @Value("${aidom.admin.password:}") String password)
      throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> applyAuthorizationRules(auth, username, password))
        .httpBasic(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  public UserDetailsService userDetailsService(
      @Value("${aidom.admin.username:}") String username,
      @Value("${aidom.admin.password:}") String password) {
    if (!isAdminConfigured(username, password)) {
      return new InMemoryUserDetailsManager();
    }

    return new InMemoryUserDetailsManager(
        User.withUsername(username).password(password).roles("ADMIN").build());
  }

  private void applyAuthorizationRules(
      AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth,
      String username,
      String password) {
    if (isAdminConfigured(username, password)) {
      auth.requestMatchers("/api/v1/admin/**").hasRole("ADMIN");
    } else {
      auth.requestMatchers("/api/v1/admin/**").denyAll();
    }

    auth.anyRequest().permitAll();
  }

  private boolean isAdminConfigured(String username, String password) {
    return StringUtils.hasText(username) && StringUtils.hasText(password);
  }
}
