package com.aidom.api.domain.test;

import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 전역 에러 핸들링(ProblemDetail) 동작 확인을 위한 테스트 컨트롤러입니다. */
@Profile({"local", "test"})
@RestController
@RequestMapping("/api/test/errors")
public class TestController {

  /** 1. 정상 응답 테스트 (200 OK)
   * 공통 래핑 DTO 없이 데이터를 그대로 반환하는 RESTful 방식.
   */
  @GetMapping("/success")
  public ResponseEntity<TestResponseDto> successResponse() {
    return ResponseEntity.ok(new TestResponseDto("AIDOM", 1));
  }

  /** 2. 커스텀 예외 테스트 (예: 404 Not Found)
   * 비즈니스 로직 처리 중 발생한 예외를 ErrorCode와 함께 던진다.
   */
  @GetMapping("/custom")
  public void throwCustomException() {
    throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
  }

  /**
   * 3. 유효성 검증 예외 테스트 (400 Bad Request)
   * @Valid 어노테이션을 통해 DTO 검증 실패 시 MethodArgumentNotValidException이 발생.
   */
  @PostMapping("/validation")
  public ResponseEntity<String> throwValidationException(
      @Valid @RequestBody TestRequestDto request) {
    return ResponseEntity.ok("검증 성공: " + request.name());
  }

  /** 4. 서버 내부 에러 테스트 (500 Internal Server Error)
   * 핸들링되지 않은 런타임 에러가 발생했을 때의 fallback 응답을 확인.
   */
  @GetMapping("/server")
  public void throwServerException() {
    throw new RuntimeException("개발자가 의도치 않은 런타임 에러가 발생했습니다");
  }


  public record TestResponseDto(String name, Integer version) {}

  public record TestRequestDto(
      @NotBlank(message = "이름은 필수 입력값입니다.") String name,
      @Min(value = 1, message = "나이는 1 이상이어야 합니다.") Integer age) {}
}
