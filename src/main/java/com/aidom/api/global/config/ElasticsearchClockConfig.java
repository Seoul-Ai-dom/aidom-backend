package com.aidom.api.global.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchClockConfig {

  @Bean
  public Clock elasticsearchClock() {
    return Clock.systemDefaultZone();
  }
}
