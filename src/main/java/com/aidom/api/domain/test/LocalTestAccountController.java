package com.aidom.api.domain.test;

import static com.aidom.api.domain.test.LocalTestAccountCatalog.ACCOUNTS;

import com.aidom.api.domain.test.LocalTestAccountCatalog.LocalTestAccount;
import com.aidom.api.domain.test.LocalTestAccountCatalog.LocalTestChild;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequestMapping("/api/test/accounts")
public class LocalTestAccountController {

  @GetMapping
  public ResponseEntity<LocalTestAccountsResponse> getLocalTestAccounts() {
    return ResponseEntity.ok(
        new LocalTestAccountsResponse(
            "현재 백엔드는 로그인 토큰을 검증하지 않습니다. 아래 토큰은 프론트/수동 API 테스트용 고정 mock 값입니다.",
            ACCOUNTS.stream().map(this::toAccountResponse).toList()));
  }

  private LocalTestAccountResponse toAccountResponse(LocalTestAccount account) {
    return new LocalTestAccountResponse(
        account.userId(),
        account.name(),
        account.email(),
        account.provider().name(),
        account.providerId(),
        account.role().name(),
        account.status().name(),
        account.relation().name(),
        account.birthDate(),
        account.phone(),
        account.address(),
        account.city(),
        account.district(),
        account.addressDetail(),
        account.addressLat(),
        account.addressLng(),
        account.accessToken(),
        account.refreshToken(),
        "Bearer " + account.accessToken(),
        account.children().stream().map(this::toChildResponse).toList());
  }

  private LocalTestChildResponse toChildResponse(LocalTestChild child) {
    return new LocalTestChildResponse(
        child.childId(),
        child.name(),
        child.birthDate(),
        child.gender().name(),
        child.specialNote(),
        child.primary());
  }

  public record LocalTestAccountsResponse(String note, List<LocalTestAccountResponse> accounts) {}

  public record LocalTestAccountResponse(
      Long userId,
      String name,
      String email,
      String provider,
      String providerId,
      String role,
      String status,
      String relation,
      LocalDate birthDate,
      String phone,
      String address,
      String city,
      String district,
      String addressDetail,
      BigDecimal addressLat,
      BigDecimal addressLng,
      String accessToken,
      String refreshToken,
      String authorizationHeader,
      List<LocalTestChildResponse> children) {}

  public record LocalTestChildResponse(
      Long childId,
      String name,
      LocalDate birthDate,
      String gender,
      String specialNote,
      boolean primary) {}
}
