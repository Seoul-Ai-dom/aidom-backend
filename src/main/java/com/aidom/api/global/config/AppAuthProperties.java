package com.aidom.api.global.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth")
@ConfigurationPropertiesScan
public class AppAuthProperties {

  private Jwt jwt = new Jwt();
  private OAuth2 oAuth2 = new OAuth2();
  private Cors cors = new Cors();

  @Getter
  @Setter
  public static class Jwt {
    private String secret;
    private Duration accessTokenValidity = Duration.ofMinutes(30);
    private Duration refreshTokenValidity = Duration.ofDays(14);
    private Duration authCodeValidity = Duration.ofSeconds(60);
  }

  @Getter
  @Setter
  public static class OAuth2 {
    private String defaultSuccessUri;
    private List<String> authorizedRedirectUris = new ArrayList<>();
  }

  @Getter
  @Setter
  public static class Cors {
    private List<String> allowedOrigins = new ArrayList<>();
  }
}
