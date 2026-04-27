package com.aidom.api.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
        .components(
            new Components()
                .addSecuritySchemes(
                    "basicAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic")
                        .description("Admin API 인증 (아이디/비밀번호)")))
        .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
        .tags(
            List.of(
                new Tag().name("시설 Facilities").description("시설 조회·검색·추천·필터 API"),
                new Tag().name("시설 관리 Admin").description("시설 데이터 관리 API"),
                new Tag().name("찜 Bookmarks").description("시설 찜(북마크) API"),
                new Tag().name("이용내역 Visits").description("시설 이용내역 관리 API")));
  }
}
