package com.aidom.api.global.error;

import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(
      CustomException e, HttpServletRequest request) {
    ErrorResponse response =
        new ErrorResponse(
            request.getRequestURI(), e.getTitle(), e.getHttpStatus().value(), e.getMessage());
    return ResponseEntity.status(e.getHttpStatus()).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException e, HttpServletRequest request) {
    String detail =
        e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
    ErrorResponse response =
        new ErrorResponse(request.getRequestURI(), "Validation Failed", 400, detail);
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
    ErrorResponse response =
        new ErrorResponse(request.getRequestURI(), "Internal Server Error", 500, e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
