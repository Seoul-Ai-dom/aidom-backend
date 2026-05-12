package com.aidom.api.global.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.claude")
public class ClaudeProperties {

  private String apiKey;
  private String model = "claude-haiku-4-20250414";
  private Duration connectTimeout = Duration.ofSeconds(1);
  private Duration readTimeout = Duration.ofSeconds(3);
  private boolean enabled = true;
}
