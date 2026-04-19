package com.aidom.api.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("AIDOM Backend API")
                .version("1.0.0")
                .description("AIDOM Backend REST API Documentation"))
        .tags(
            List.of(
                new Tag().name("시설 Facilities").description("시설 조회·검색·추천·필터 API"),
                new Tag().name("찜 Bookmarks").description("시설 찜(북마크) API"),
                new Tag().name("이용내역 Visits").description("시설 이용내역 관리 API")));
  }
}
