package com.aidom.api.global.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

  @Valid private Jwt jwt = new Jwt();
  @Valid private Oauth2 oauth2 = new Oauth2();
  @Valid private Cors cors = new Cors();

  @Getter
  @Setter
  public static class Jwt {

    @NotBlank private String secret;
    private Duration accessTokenValidity = Duration.ofHours(6);
  }

  @Getter
  @Setter
  public static class Oauth2 {

    @NotBlank private String defaultSuccessUri = "http://localhost:3000/auth/callback";
    private List<String> authorizedRedirectUris =
        new ArrayList<>(List.of("http://localhost:3000/auth/callback"));
  }

  @Getter
  @Setter
  public static class Cors {

    private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:3000"));
  }
}
