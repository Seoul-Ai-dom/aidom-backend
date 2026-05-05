package com.aidom.api.global.security;

import com.aidom.api.global.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityProblemDetailWriter {

  private final ObjectMapper objectMapper;

  public SecurityProblemDetailWriter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public void write(HttpServletRequest request, HttpServletResponse response, ErrorCode errorCode) {
    try {
      ProblemDetail problemDetail =
          ProblemDetail.forStatusAndDetail(errorCode.getHttpStatus(), errorCode.getMessage());
      problemDetail.setType(URI.create("about:blank"));
      problemDetail.setTitle(errorCode.name());
      problemDetail.setProperty("errorCode", errorCode.getCode());

      response.setStatus(errorCode.getHttpStatus().value());
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
      objectMapper.writeValue(response.getWriter(), problemDetail);
    } catch (Exception e) {
      log.error(
          "Failed to write security ProblemDetail response. uri={}, errorCode={}",
          request.getRequestURI(),
          errorCode.name(),
          e);
      if (!response.isCommitted()) {
        try {
          response.sendError(errorCode.getHttpStatus().value(), errorCode.getMessage());
        } catch (IOException ioException) {
          log.error(
              "Failed to send fallback security error response. uri={}, errorCode={}",
              request.getRequestURI(),
              errorCode.name(),
              ioException);
        }
      }
    }
  }
}
