package com.aidom.api.global.error;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  public record ValidationError(String field, String message) {}

  /**
   * @Valid 또는 @Validated에 의한 검증 실패 시 발생하는 예외를 가로채어 ProblemDetail 형식으로 반환 *
   */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {

    List<ValidationError> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
            .toList();

    // ProblemDetail 기본 설정
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(status, "요청 데이터에 유효하지 않은 값이 포함되어 있습니다.");

    // 표준 필드 커스텀 (type, title)
    problemDetail.setType(URI.create("https://aidom.kr/errors/validation-failed"));
    problemDetail.setTitle("입력값이 올바르지 않습니다");

    // 비표준 커스텀 필드 추가 (errorCode, timestamp, errors 배열)
    problemDetail.setProperty("errorCode", "VALIDATION_001");
    problemDetail.setProperty("timestamp", Instant.now()); // ISO-8601 포맷(Z)
    problemDetail.setProperty("errors", errors);

    return ResponseEntity.status(status).body(problemDetail);
  }

  @ExceptionHandler(CustomException.class)
  public ProblemDetail handleCustomException(CustomException e) {
    ErrorCode errorCode = e.getErrorCode();

    // HTTP 상태 코드와 메시지를 바탕으로 표준 ProblemDetail 객체 생성
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(errorCode.getHttpStatus(), errorCode.getMessage());

    // RFC 7807 표준 외에 클라이언트가 참고할 커스텀 속성(property) 추가
    problemDetail.setType(URI.create("about:blank"));
    problemDetail.setTitle(errorCode.name());
    problemDetail.setProperty("customErrorCode", errorCode.getCode());

    return problemDetail;
  }

  /** 핸들링되지 않은 최상위 예외를 처리합니다. (500 Error) */
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleAllUncaughtException(Exception e) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부에서 예기치 않은 오류가 발생했습니다.");
    problemDetail.setTitle(ErrorCode.INTERNAL_SERVER_ERROR.name());
    problemDetail.setProperty("customErrorCode", ErrorCode.INTERNAL_SERVER_ERROR.getCode());

    return problemDetail;
  }
}
