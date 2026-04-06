package com.aidom.api.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

  private final String title;
  private final HttpStatus httpStatus;

  public CustomException(String title, HttpStatus httpStatus, String detail) {
    super(detail);
    this.title = title;
    this.httpStatus = httpStatus;
  }
}
