package com.aidom.api.global.security;

import com.aidom.api.global.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;

  public ProblemDetailAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) {
    Object errorCodeAttribute = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_CODE_ATTR);
    if (errorCodeAttribute instanceof ErrorCode errorCode) {
      write(response, errorCode);
      return;
    }
    write(response, ErrorCode.UNAUTHORIZED);
  }

  public void write(HttpServletResponse response, ErrorCode errorCode) {
    try {
      response.setStatus(errorCode.getHttpStatus().value());
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

      Map<String, Object> body =
          Map.of(
              "type", URI.create("about:blank").toString(),
              "title", errorCode.name(),
              "status", errorCode.getHttpStatus().value(),
              "detail", errorCode.getMessage(),
              "errorCode", errorCode.getCode(),
              "timestamp", Instant.now().toString());

      objectMapper.writeValue(response.getWriter(), body);
    } catch (Exception ignored) {
    }
  }
}
