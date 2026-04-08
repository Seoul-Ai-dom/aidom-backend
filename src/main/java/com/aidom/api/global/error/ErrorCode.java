package com.aidom.api.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // Common
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
  INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C002", "요청 파라미터 또는 바디의 타입이 일치하지 않습니다."),
  MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "C003", "필수 요청 파라미터가 누락되었습니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C004", "지원하지 않는 HTTP 메서드입니다."),
  UNSUPPORTED_MEDIA_TYPE(
      HttpStatus.UNSUPPORTED_MEDIA_TYPE, "C005", "지원하지 않는 미디어 타입(Content-Type)입니다."),
  ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C006", "요청한 리소스를 찾을 수 없습니다."),
  INTERNAL_SERVER_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR, "C007", "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요."),

  // Auth
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "A004", "해당 리소스에 접근할 권한이 없습니다."),

  // Validation
  DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "V001", "이미 존재하는 데이터입니다."),
  BUSINESS_VALIDATION_ERROR(
      HttpStatus.UNPROCESSABLE_ENTITY, "V002", "비즈니스 규칙 위반 또는 처리할 수 없는 요청입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
