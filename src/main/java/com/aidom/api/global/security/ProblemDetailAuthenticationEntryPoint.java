package com.aidom.api.global.security;

import com.aidom.api.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final SecurityProblemDetailWriter securityProblemDetailWriter;

  public ProblemDetailAuthenticationEntryPoint(
      SecurityProblemDetailWriter securityProblemDetailWriter) {
    this.securityProblemDetailWriter = securityProblemDetailWriter;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) {
    Object errorCodeAttribute = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_CODE_ATTR);
    if (errorCodeAttribute instanceof ErrorCode errorCode) {
      securityProblemDetailWriter.write(request, response, errorCode);
      return;
    }
    securityProblemDetailWriter.write(request, response, ErrorCode.UNAUTHORIZED);
  }
}
