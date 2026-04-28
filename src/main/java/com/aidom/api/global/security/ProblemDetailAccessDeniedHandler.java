package com.aidom.api.global.security;

import com.aidom.api.global.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ProblemDetailAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  public ProblemDetailAccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException) {
    try {
      ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
      response.setStatus(errorCode.getHttpStatus().value());
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
