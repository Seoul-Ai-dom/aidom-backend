package com.aidom.api.domain.test;

import com.aidom.api.domain.user.enums.ParentRelation;
import com.aidom.api.domain.user.enums.Provider;
import com.aidom.api.domain.user.enums.Role;
import com.aidom.api.domain.user.enums.UserStatus;
import com.aidom.api.global.common.entity.Gender;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

final class LocalTestAccountCatalog {

  static final List<LocalTestAccount> ACCOUNTS =
      List.of(
          new LocalTestAccount(
              900001L,
              "local-parent-1-access-token",
              "local-parent-1-refresh-token",
              "김민서",
              "minseo.local@aidom.test",
              Provider.KAKAO,
              "local-kakao-900001",
              Role.USER,
              UserStatus.ACTIVE,
              ParentRelation.MOTHER,
              LocalDate.of(1991, 5, 12),
              "010-9000-0001",
              "서울특별시 강남구 테헤란로 212",
              "서울특별시",
              "강남구",
              "101동 1203호",
              new BigDecimal("37.4980950"),
              new BigDecimal("127.0276100"),
              List.of(
                  new LocalTestChild(
                      910001L, "김하윤", LocalDate.of(2018, 3, 14), Gender.FEMALE, "미술 프로그램 선호", true),
                  new LocalTestChild(
                      910002L,
                      "김도윤",
                      LocalDate.of(2015, 9, 2),
                      Gender.MALE,
                      "방과 후 돌봄 테스트용",
                      false))),
          new LocalTestAccount(
              900002L,
              "local-parent-2-access-token",
              "local-parent-2-refresh-token",
              "박준호",
              "junho.local@aidom.test",
              Provider.GOOGLE,
              "local-google-900002",
              Role.USER,
              UserStatus.ACTIVE,
              ParentRelation.FATHER,
              LocalDate.of(1988, 11, 21),
              "010-9000-0002",
              "서울특별시 송파구 올림픽로 300",
              "서울특별시",
              "송파구",
              "테스트 보호자 주소",
              new BigDecimal("37.5132612"),
              new BigDecimal("127.1028670"),
              List.of(
                  new LocalTestChild(
                      910003L,
                      "박서윤",
                      LocalDate.of(2019, 7, 8),
                      Gender.FEMALE,
                      "주말 체험형 프로그램 테스트용",
                      true))));

  private LocalTestAccountCatalog() {}

  record LocalTestAccount(
      Long userId,
      String accessToken,
      String refreshToken,
      String name,
      String email,
      Provider provider,
      String providerId,
      Role role,
      UserStatus status,
      ParentRelation relation,
      LocalDate birthDate,
      String phone,
      String address,
      String city,
      String district,
      String addressDetail,
      BigDecimal addressLat,
      BigDecimal addressLng,
      List<LocalTestChild> children) {}

  record LocalTestChild(
      Long childId,
      String name,
      LocalDate birthDate,
      Gender gender,
      String specialNote,
      boolean primary) {}
}
