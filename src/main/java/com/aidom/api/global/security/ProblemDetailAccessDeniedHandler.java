package com.aidom.api.global.security;

import com.aidom.api.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ProblemDetailAccessDeniedHandler implements AccessDeniedHandler {

  private final SecurityProblemDetailWriter securityProblemDetailWriter;

  public ProblemDetailAccessDeniedHandler(SecurityProblemDetailWriter securityProblemDetailWriter) {
    this.securityProblemDetailWriter = securityProblemDetailWriter;
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException) {
    securityProblemDetailWriter.write(request, response, ErrorCode.ACCESS_DENIED);
  }
}
