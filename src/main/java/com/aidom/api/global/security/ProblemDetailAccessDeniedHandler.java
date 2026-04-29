package com.aidom.api.global.security;

import com.aidom.api.global.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Slf4j
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
    } catch (Exception e) {
      log.error("Failed to write 403 ProblemDetail response. uri={}", request.getRequestURI(), e);
      if (!response.isCommitted()) {
        response.setStatus(ErrorCode.ACCESS_DENIED.getHttpStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        try {
          response.sendError(
              ErrorCode.ACCESS_DENIED.getHttpStatus().value(), ErrorCode.ACCESS_DENIED.getMessage());
        } catch (IOException ioException) {
          log.error(
              "Failed to send fallback 403 error response. uri={}", request.getRequestURI(), ioException);
        }
      }
    }
  }
}
