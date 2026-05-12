package com.aidom.api.domain.test;

import static com.aidom.api.domain.test.LocalTestAccountCatalog.ACCOUNTS;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile({"local", "test"})
@Component
@RequiredArgsConstructor
class LocalTestAccountSeeder implements ApplicationRunner {

  private final JdbcTemplate jdbcTemplate;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    LocalDateTime now = LocalDateTime.now();

    for (LocalTestAccountCatalog.LocalTestAccount account : ACCOUNTS) {
      upsertUser(account, now);

      for (LocalTestAccountCatalog.LocalTestChild child : account.children()) {
        upsertChild(account.userId(), child, now);
      }
    }
  }

  private void upsertUser(LocalTestAccountCatalog.LocalTestAccount account, LocalDateTime now) {
    if (exists("users", "user_id", account.userId())) {
      jdbcTemplate.update(
          """
          update users
             set updated_at = ?,
                 name = ?,
                 email = ?,
                 provider = ?,
                 provider_id = ?,
                 role = ?,
                 status = ?,
                 relation = ?,
                 birth_date = ?,
                 phone = ?,
                 address = ?,
                 city = ?,
                 district = ?,
                 address_detail = ?,
                 address_lat = ?,
                 address_lng = ?,
                 deleted_at = null
           where user_id = ?
          """,
          Timestamp.valueOf(now),
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
          account.userId());
      return;
    }

    jdbcTemplate.update(
        """
        insert into users (
            user_id,
            created_at,
            updated_at,
            name,
            email,
            provider,
            provider_id,
            role,
            status,
            relation,
            birth_date,
            phone,
            address,
            city,
            district,
            address_detail,
            address_lat,
            address_lng,
            deleted_at
        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, null)
        """,
        account.userId(),
        Timestamp.valueOf(now),
        Timestamp.valueOf(now),
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
        account.addressLng());
  }

  private void upsertChild(
      Long userId, LocalTestAccountCatalog.LocalTestChild child, LocalDateTime now) {
    if (exists("children", "child_id", child.childId())) {
      jdbcTemplate.update(
          """
          update children
             set updated_at = ?,
                 user_id = ?,
                 name = ?,
                 birth_date = ?,
                 gender = ?,
                 special_note = ?,
                 is_primary = ?,
                 deleted_at = null
           where child_id = ?
          """,
          Timestamp.valueOf(now),
          userId,
          child.name(),
          child.birthDate(),
          child.gender().name(),
          child.specialNote(),
          child.primary(),
          child.childId());
      return;
    }

    jdbcTemplate.update(
        """
        insert into children (
            child_id,
            created_at,
            updated_at,
            user_id,
            name,
            birth_date,
            gender,
            special_note,
            is_primary,
            deleted_at
        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, null)
        """,
        child.childId(),
        Timestamp.valueOf(now),
        Timestamp.valueOf(now),
        userId,
        child.name(),
        child.birthDate(),
        child.gender().name(),
        child.specialNote(),
        child.primary());
  }

  private boolean exists(String tableName, String idColumn, Long id) {
    Integer count =
        jdbcTemplate.queryForObject(
            "select count(*) from " + tableName + " where " + idColumn + " = ?", Integer.class, id);
    return count != null && count > 0;
  }
}
