package com.aidom.api.domain.test.bootstrap;

import com.aidom.api.domain.user.enums.ParentRelation;
import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.domain.user.enums.Role;
import com.aidom.api.domain.user.enums.UserStatus;
import com.aidom.api.global.common.entity.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "app.ops.test-account")
public class TestAccountBootstrapProperties {

  private boolean enabled = false;

  @NotBlank private String requestId = "manual-bootstrap";

  @NotBlank private String email = "ops-test-parent@aidom.test";

  @NotBlank private String name = "운영 테스트 보호자";

  @NotNull private Provider provider = Provider.KAKAO;

  @NotBlank private String providerId = "ops-kakao-test-parent";

  @NotNull private Role role = Role.USER;

  @NotNull private UserStatus status = UserStatus.ACTIVE;

  @NotNull private ParentRelation relation = ParentRelation.MOTHER;

  @NotNull private LocalDate birthDate = LocalDate.of(1990, 1, 1);

  @NotBlank private String phone = "010-5555-0001";

  @NotBlank private String address = "서울특별시 강남구 테헤란로 212";

  @NotBlank private String city = "서울특별시";

  @NotBlank private String district = "강남구";

  private String addressDetail = "운영 테스트 계정";

  @NotNull private BigDecimal addressLat = new BigDecimal("37.4980950");

  @NotNull private BigDecimal addressLng = new BigDecimal("127.0276100");

  private boolean logRefreshToken = false;

  @Valid
  @Size(min = 1, max = 5)
  private List<ChildSpec> children =
      new ArrayList<>(
          List.of(
              new ChildSpec(
                  "테스트아동1", LocalDate.of(2018, 3, 14), Gender.FEMALE, "운영 추천/방문 테스트용", true),
              new ChildSpec(
                  "테스트아동2", LocalDate.of(2015, 9, 2), Gender.MALE, "운영 북마크/이용내역 테스트용", false)));

  @Getter
  @Setter
  public static class ChildSpec {

    @NotBlank private String name;
    @NotNull private LocalDate birthDate;
    @NotNull private Gender gender;
    private String specialNote;
    private boolean primary;

    public ChildSpec() {}

    public ChildSpec(
        String name, LocalDate birthDate, Gender gender, String specialNote, boolean primary) {
      this.name = name;
      this.birthDate = birthDate;
      this.gender = gender;
      this.specialNote = specialNote;
      this.primary = primary;
    }
  }
}
