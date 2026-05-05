package com.aidom.api.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  public static final String BEARER_SCHEME = "bearerAuth";

  @Bean
  public OpenAPI openAPI() {
    SecurityScheme bearerScheme =
        new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("JWT Access Token을 입력하세요 (Bearer 접두사 불필요)");

    return new OpenAPI()
        .info(
            new Info()
                .title("AIDOM Backend API")
                .version("1.0.0")
                .description("AIDOM Backend REST API Documentation"))
        .components(new Components().addSecuritySchemes(BEARER_SCHEME, bearerScheme))
        .tags(
            List.of(
                new Tag().name("시설 Facilities").description("시설 조회·검색·추천·필터 API"),
                new Tag().name("찜 Bookmarks").description("시설 찜(북마크) API"),
                new Tag().name("이용내역 Visits").description("시설 이용내역 관리 API")));
  }
}
