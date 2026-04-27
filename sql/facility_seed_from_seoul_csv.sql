-- MySQL 8 import script for Aidom facility data
-- Run with:
--   mysql --local-infile=1 -h 127.0.0.1 -P 3307 -u aidomuser -p aidom < sql/facility_seed_from_seoul_csv.sql
--
-- Notes
-- 1) The app schema is inferred from JPA entities:
--    - facilities
--    - facility_external_info
--    - facility_stats
-- 2) service_type_code stores app enum codes, not the numeric CSV service code.
-- 3) service_type stores the enum description used by the app.
-- 4) Age assumptions are explicit below because some CSVs only expose broad categories.
-- 5) lat = Y coordinate, lng = X coordinate.

SET NAMES utf8mb4;

SET @INFANT_AGE_MIN = 0;
SET @INFANT_AGE_MAX = 6;
SET @ELEMENTARY_AGE_MIN = 6;
SET @ELEMENTARY_AGE_MAX = 12;
SET @YOUTH_AGE_MIN = 13;
SET @YOUTH_AGE_MAX = 18;

DROP TEMPORARY TABLE IF EXISTS stg_childcare_sharing_center;
CREATE TEMPORARY TABLE stg_childcare_sharing_center (
  facility_id VARCHAR(20),
  facility_name VARCHAR(255),
  src_service_code VARCHAR(20),
  src_service_name VARCHAR(100),
  district_code VARCHAR(10),
  district_name VARCHAR(50),
  age_group_code VARCHAR(20),
  age_group_name VARCHAR(50),
  x_coord VARCHAR(50),
  y_coord VARCHAR(50),
  zip_code VARCHAR(20),
  base_address TEXT,
  detail_address TEXT,
  has_site VARCHAR(5),
  site_url TEXT,
  is_free_flag VARCHAR(5),
  fee VARCHAR(50),
  weekday_start VARCHAR(20),
  weekday_end VARCHAR(20),
  time_type_code VARCHAR(20),
  time_type_name VARCHAR(100),
  slot1_start VARCHAR(20),
  slot1_end VARCHAR(20),
  slot2_start VARCHAR(20),
  slot2_end VARCHAR(20),
  slot3_start VARCHAR(20),
  slot3_end VARCHAR(20),
  saturday_open VARCHAR(5),
  saturday_start VARCHAR(20),
  saturday_end VARCHAR(20),
  created_at_raw VARCHAR(30),
  updated_at_raw VARCHAR(30)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOAD DATA LOCAL INFILE '/tmp/aidom_csv/서울시 공동육아나눔터 시설현황정보.csv'
INTO TABLE stg_childcare_sharing_center
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

DROP TEMPORARY TABLE IF EXISTS stg_shared_childcare;
CREATE TEMPORARY TABLE stg_shared_childcare (
  facility_id VARCHAR(20),
  facility_name VARCHAR(255),
  src_service_code VARCHAR(20),
  src_service_name VARCHAR(100),
  district_code VARCHAR(10),
  district_name VARCHAR(50),
  age_group_code VARCHAR(20),
  age_group_name VARCHAR(50),
  x_coord VARCHAR(50),
  y_coord VARCHAR(50),
  zip_code VARCHAR(20),
  base_address TEXT,
  detail_address TEXT,
  has_site VARCHAR(5),
  site_url TEXT,
  is_free_flag VARCHAR(5),
  fee VARCHAR(50),
  weekday_start VARCHAR(20),
  weekday_end VARCHAR(20),
  time_type_code VARCHAR(20),
  time_type_name VARCHAR(100),
  slot1_start VARCHAR(20),
  slot1_end VARCHAR(20),
  slot2_start VARCHAR(20),
  slot2_end VARCHAR(20),
  slot3_start VARCHAR(20),
  slot3_end VARCHAR(20),
  saturday_open VARCHAR(5),
  saturday_start VARCHAR(20),
  saturday_end VARCHAR(20),
  created_at_raw VARCHAR(30),
  updated_at_raw VARCHAR(30)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOAD DATA LOCAL INFILE '/tmp/aidom_csv/서울시 공동육아방 시설현황정보.csv'
INTO TABLE stg_shared_childcare
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

DROP TEMPORARY TABLE IF EXISTS stg_kium_center;
CREATE TEMPORARY TABLE stg_kium_center (
  facility_id VARCHAR(20),
  facility_name VARCHAR(255),
  src_service_code VARCHAR(20),
  src_service_name VARCHAR(100),
  district_code VARCHAR(10),
  district_name VARCHAR(50),
  age_group_code VARCHAR(20),
  age_group_name VARCHAR(50),
  x_coord VARCHAR(50),
  y_coord VARCHAR(50),
  zip_code VARCHAR(20),
  base_address TEXT,
  detail_address TEXT,
  established_on VARCHAR(20),
  is_free_flag VARCHAR(5),
  fee VARCHAR(50),
  area_pyeong VARCHAR(50),
  semester_start VARCHAR(20),
  semester_end VARCHAR(20),
  vacation_start VARCHAR(20),
  vacation_end VARCHAR(20),
  discretionary_start VARCHAR(20),
  discretionary_end VARCHAR(20),
  saturday_open VARCHAR(5),
  saturday_start VARCHAR(20),
  saturday_end VARCHAR(20),
  monthly_free_flag VARCHAR(5),
  monthly_copay VARCHAR(50),
  monthly_fee VARCHAR(50),
  daily_free_flag VARCHAR(5),
  daily_copay VARCHAR(50),
  daily_fee VARCHAR(50),
  capacity_regular VARCHAR(50),
  capacity_temporary VARCHAR(50),
  area_sqm VARCHAR(50),
  district_code_array TEXT,
  morning_care_flag VARCHAR(5),
  created_at_raw VARCHAR(30),
  updated_at_raw VARCHAR(30)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOAD DATA LOCAL INFILE '/tmp/aidom_csv/서울시 우리동네키움센터 시설현황정보.csv'
INTO TABLE stg_kium_center
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

DROP TEMPORARY TABLE IF EXISTS stg_child_center;
CREATE TEMPORARY TABLE stg_child_center (
  facility_id VARCHAR(20),
  facility_name VARCHAR(255),
  src_service_code VARCHAR(20),
  src_service_name VARCHAR(100),
  district_code VARCHAR(10),
  district_name VARCHAR(50),
  age_group_code VARCHAR(20),
  age_group_name VARCHAR(50),
  x_coord VARCHAR(50),
  y_coord VARCHAR(50),
  zip_code VARCHAR(20),
  base_address TEXT,
  detail_address TEXT,
  phone VARCHAR(50),
  fee VARCHAR(50),
  semester_start VARCHAR(20),
  semester_end VARCHAR(20),
  vacation_start VARCHAR(20),
  vacation_end VARCHAR(20),
  saturday_open VARCHAR(5),
  saturday_start VARCHAR(20),
  saturday_end VARCHAR(20),
  created_at_raw VARCHAR(30),
  updated_at_raw VARCHAR(30)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOAD DATA LOCAL INFILE '/tmp/aidom_csv/서울시 지역아동센터 시설현황정보.csv'
INTO TABLE stg_child_center
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

DROP TEMPORARY TABLE IF EXISTS stg_youth_academy;
CREATE TEMPORARY TABLE stg_youth_academy (
  facility_id VARCHAR(20),
  facility_name VARCHAR(255),
  src_service_code VARCHAR(20),
  src_service_name VARCHAR(100),
  district_code VARCHAR(10),
  district_name VARCHAR(50),
  x_coord VARCHAR(50),
  y_coord VARCHAR(50),
  zip_code VARCHAR(20),
  base_address TEXT,
  detail_address TEXT,
  site_url TEXT,
  is_free_flag VARCHAR(5),
  fee VARCHAR(50),
  facility_form_code VARCHAR(20),
  facility_form_name VARCHAR(50),
  semester_start VARCHAR(20),
  semester_end VARCHAR(20),
  vacation_start VARCHAR(20),
  vacation_end VARCHAR(20),
  saturday_open VARCHAR(5),
  saturday_start VARCHAR(20),
  saturday_end VARCHAR(20),
  created_at_raw VARCHAR(30),
  updated_at_raw VARCHAR(30)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOAD DATA LOCAL INFILE '/tmp/aidom_csv/서울시 청소년방과후아카데미 시설현황정보.csv'
INTO TABLE stg_youth_academy
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

DROP TEMPORARY TABLE IF EXISTS stg_kids_cafe;
CREATE TEMPORARY TABLE stg_kids_cafe (
  facility_id VARCHAR(20),
  created_at_raw VARCHAR(30),
  updated_at_raw VARCHAR(30),
  facility_name VARCHAR(255),
  src_service_code VARCHAR(20),
  src_service_name VARCHAR(100),
  district_code VARCHAR(10),
  district_name VARCHAR(50),
  admin_dong_code VARCHAR(20),
  admin_dong_name VARCHAR(50),
  x_coord VARCHAR(50),
  y_coord VARCHAR(50),
  zip_code VARCHAR(20),
  base_address TEXT,
  detail_address TEXT,
  open_date VARCHAR(20),
  phone VARCHAR(50),
  is_free_flag VARCHAR(5),
  operating_days TEXT,
  closed_days TEXT,
  capacity_regular VARCHAR(50),
  capacity_temporary VARCHAR(50),
  age_range VARCHAR(50),
  district_code_array TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOAD DATA LOCAL INFILE '/tmp/aidom_csv/서울형 키즈카페 시설현황정보.csv'
INTO TABLE stg_kids_cafe
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

INSERT INTO facilities (
  facility_id,
  facility_name,
  service_type_code,
  service_type,
  district_code,
  district_name,
  address,
  lat,
  lng,
  age_group,
  age_min,
  age_max,
  booking_required,
  is_free,
  fee,
  monthly_fee,
  capacity_regular,
  capacity_temporary,
  area_sqm,
  operating_days,
  closed_days,
  has_regular_program,
  has_regular_care,
  has_temporary_care,
  created_at,
  updated_at
)
SELECT
  src.facility_id,
  src.facility_name,
  src.service_type_code,
  src.service_type,
  src.district_code,
  src.district_name,
  src.address,
  src.lat,
  src.lng,
  src.age_group,
  src.age_min,
  src.age_max,
  src.booking_required,
  src.is_free,
  src.fee,
  src.monthly_fee,
  src.capacity_regular,
  src.capacity_temporary,
  src.area_sqm,
  src.operating_days,
  src.closed_days,
  src.has_regular_program,
  src.has_regular_care,
  src.has_temporary_care,
  src.created_at,
  src.updated_at
FROM (
  SELECT
    TRIM(facility_id) AS facility_id,
    TRIM(facility_name) AS facility_name,
    'CHILDCARE_SHARING_CENTER' AS service_type_code,
    '공동육아나눔터' AS service_type,
    TRIM(district_code) AS district_code,
    TRIM(district_name) AS district_name,
    TRIM(
      CONCAT(
        COALESCE(NULLIF(TRIM(base_address), ''), ''),
        CASE
          WHEN NULLIF(TRIM(detail_address), '') IS NOT NULL THEN CONCAT(' ', TRIM(detail_address))
          ELSE ''
        END
      )
    ) AS address,
    CAST(NULLIF(TRIM(y_coord), '') AS DECIMAL(10,7)) AS lat,
    CAST(NULLIF(TRIM(x_coord), '') AS DECIMAL(10,7)) AS lng,
    COALESCE(NULLIF(TRIM(age_group_name), ''), '영유아') AS age_group,
    @INFANT_AGE_MIN AS age_min,
    @INFANT_AGE_MAX AS age_max,
    0 AS booking_required,
    CASE
      WHEN UPPER(TRIM(is_free_flag)) = 'Y' THEN 1
      WHEN CAST(COALESCE(NULLIF(REPLACE(TRIM(fee), ',', ''), ''), '0') AS UNSIGNED) = 0 THEN 1
      ELSE 0
    END AS is_free,
    CAST(NULLIF(REPLACE(TRIM(fee), ',', ''), '') AS UNSIGNED) AS fee,
    NULL AS monthly_fee,
    NULL AS capacity_regular,
    NULL AS capacity_temporary,
    NULL AS area_sqm,
    CONCAT_WS(
      ', ',
      CASE
        WHEN NULLIF(TRIM(weekday_start), '') IS NOT NULL AND NULLIF(TRIM(weekday_end), '') IS NOT NULL
          THEN CONCAT('평일 ', TRIM(weekday_start), '-', TRIM(weekday_end))
      END,
      CASE
        WHEN UPPER(TRIM(saturday_open)) = 'Y'
             AND NULLIF(TRIM(saturday_start), '') IS NOT NULL
             AND NULLIF(TRIM(saturday_end), '') IS NOT NULL
          THEN CONCAT('토요일 ', TRIM(saturday_start), '-', TRIM(saturday_end))
      END
    ) AS operating_days,
    CASE
      WHEN UPPER(TRIM(saturday_open)) = 'Y' THEN '일요일'
      ELSE '토요일, 일요일'
    END AS closed_days,
    1 AS has_regular_program,
    0 AS has_regular_care,
    1 AS has_temporary_care,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS created_at,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(updated_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS updated_at
  FROM stg_childcare_sharing_center

  UNION ALL

  SELECT
    TRIM(facility_id) AS facility_id,
    TRIM(facility_name) AS facility_name,
    'SHARED_CHILDCARE' AS service_type_code,
    '공동육아방' AS service_type,
    TRIM(district_code) AS district_code,
    TRIM(district_name) AS district_name,
    TRIM(
      CONCAT(
        COALESCE(NULLIF(TRIM(base_address), ''), ''),
        CASE
          WHEN NULLIF(TRIM(detail_address), '') IS NOT NULL THEN CONCAT(' ', TRIM(detail_address))
          ELSE ''
        END
      )
    ) AS address,
    CAST(NULLIF(TRIM(y_coord), '') AS DECIMAL(10,7)) AS lat,
    CAST(NULLIF(TRIM(x_coord), '') AS DECIMAL(10,7)) AS lng,
    COALESCE(NULLIF(TRIM(age_group_name), ''), '영유아') AS age_group,
    @INFANT_AGE_MIN AS age_min,
    @INFANT_AGE_MAX AS age_max,
    0 AS booking_required,
    CASE
      WHEN UPPER(TRIM(is_free_flag)) = 'Y' THEN 1
      WHEN CAST(COALESCE(NULLIF(REPLACE(TRIM(fee), ',', ''), ''), '0') AS UNSIGNED) = 0 THEN 1
      ELSE 0
    END AS is_free,
    CAST(NULLIF(REPLACE(TRIM(fee), ',', ''), '') AS UNSIGNED) AS fee,
    NULL AS monthly_fee,
    NULL AS capacity_regular,
    NULL AS capacity_temporary,
    NULL AS area_sqm,
    CONCAT_WS(
      ', ',
      CASE
        WHEN NULLIF(TRIM(weekday_start), '') IS NOT NULL AND NULLIF(TRIM(weekday_end), '') IS NOT NULL
          THEN CONCAT('평일 ', TRIM(weekday_start), '-', TRIM(weekday_end))
      END,
      CASE
        WHEN UPPER(TRIM(saturday_open)) = 'Y'
             AND NULLIF(TRIM(saturday_start), '') IS NOT NULL
             AND NULLIF(TRIM(saturday_end), '') IS NOT NULL
          THEN CONCAT('토요일 ', TRIM(saturday_start), '-', TRIM(saturday_end))
      END
    ) AS operating_days,
    CASE
      WHEN UPPER(TRIM(saturday_open)) = 'Y' THEN '일요일'
      ELSE '토요일, 일요일'
    END AS closed_days,
    1 AS has_regular_program,
    0 AS has_regular_care,
    1 AS has_temporary_care,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS created_at,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(updated_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS updated_at
  FROM stg_shared_childcare

  UNION ALL

  SELECT
    TRIM(facility_id) AS facility_id,
    TRIM(facility_name) AS facility_name,
    'KIUM_CENTER' AS service_type_code,
    '우리동네키움센터' AS service_type,
    TRIM(district_code) AS district_code,
    TRIM(district_name) AS district_name,
    TRIM(
      CONCAT(
        COALESCE(NULLIF(TRIM(base_address), ''), ''),
        CASE
          WHEN NULLIF(TRIM(detail_address), '') IS NOT NULL THEN CONCAT(' ', TRIM(detail_address))
          ELSE ''
        END
      )
    ) AS address,
    CAST(NULLIF(TRIM(y_coord), '') AS DECIMAL(10,7)) AS lat,
    CAST(NULLIF(TRIM(x_coord), '') AS DECIMAL(10,7)) AS lng,
    COALESCE(NULLIF(TRIM(age_group_name), ''), '초등학생') AS age_group,
    @ELEMENTARY_AGE_MIN AS age_min,
    @ELEMENTARY_AGE_MAX AS age_max,
    0 AS booking_required,
    CASE
      WHEN UPPER(TRIM(monthly_free_flag)) = 'Y' AND UPPER(TRIM(daily_free_flag)) = 'Y' THEN 1
      WHEN CAST(COALESCE(NULLIF(REPLACE(TRIM(monthly_fee), ',', ''), ''), '0') AS UNSIGNED) = 0
           AND CAST(COALESCE(NULLIF(REPLACE(TRIM(daily_fee), ',', ''), ''), '0') AS UNSIGNED) = 0
        THEN 1
      ELSE 0
    END AS is_free,
    CAST(NULLIF(REPLACE(TRIM(daily_fee), ',', ''), '') AS UNSIGNED) AS fee,
    CAST(NULLIF(REPLACE(TRIM(monthly_fee), ',', ''), '') AS UNSIGNED) AS monthly_fee,
    CAST(NULLIF(REPLACE(TRIM(capacity_regular), ',', ''), '') AS UNSIGNED) AS capacity_regular,
    CAST(NULLIF(REPLACE(TRIM(capacity_temporary), ',', ''), '') AS UNSIGNED) AS capacity_temporary,
    COALESCE(
      CAST(NULLIF(REPLACE(TRIM(area_sqm), ',', ''), '') AS DECIMAL(10,2)),
      CAST(NULLIF(REPLACE(TRIM(area_pyeong), ',', ''), '') AS DECIMAL(10,2)) * 3.305785
    ) AS area_sqm,
    CONCAT_WS(
      ' / ',
      CASE
        WHEN NULLIF(TRIM(semester_start), '') IS NOT NULL AND NULLIF(TRIM(semester_end), '') IS NOT NULL
          THEN CONCAT('학기 ', TRIM(semester_start), '-', TRIM(semester_end))
      END,
      CASE
        WHEN NULLIF(TRIM(vacation_start), '') IS NOT NULL AND NULLIF(TRIM(vacation_end), '') IS NOT NULL
          THEN CONCAT('방학 ', TRIM(vacation_start), '-', TRIM(vacation_end))
      END,
      CASE
        WHEN NULLIF(TRIM(discretionary_start), '') IS NOT NULL
             AND NULLIF(TRIM(discretionary_end), '') IS NOT NULL
          THEN CONCAT('재량휴일 ', TRIM(discretionary_start), '-', TRIM(discretionary_end))
      END,
      CASE
        WHEN UPPER(TRIM(saturday_open)) = 'Y'
             AND NULLIF(TRIM(saturday_start), '') IS NOT NULL
             AND NULLIF(TRIM(saturday_end), '') IS NOT NULL
          THEN CONCAT('토요일 ', TRIM(saturday_start), '-', TRIM(saturday_end))
      END
    ) AS operating_days,
    CASE
      WHEN UPPER(TRIM(saturday_open)) = 'Y' THEN '일요일'
      ELSE '토요일, 일요일'
    END AS closed_days,
    1 AS has_regular_program,
    CASE
      WHEN CAST(COALESCE(NULLIF(REPLACE(TRIM(capacity_regular), ',', ''), ''), '0') AS UNSIGNED) > 0 THEN 1
      ELSE 0
    END AS has_regular_care,
    CASE
      WHEN CAST(COALESCE(NULLIF(REPLACE(TRIM(capacity_temporary), ',', ''), ''), '0') AS UNSIGNED) > 0 THEN 1
      ELSE 0
    END AS has_temporary_care,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS created_at,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(updated_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS updated_at
  FROM stg_kium_center

  UNION ALL

  SELECT
    TRIM(facility_id) AS facility_id,
    TRIM(facility_name) AS facility_name,
    'CHILD_CENTER' AS service_type_code,
    '지역아동센터' AS service_type,
    TRIM(district_code) AS district_code,
    TRIM(district_name) AS district_name,
    NULLIF(
      TRIM(
        CONCAT(
          COALESCE(NULLIF(TRIM(base_address), ''), ''),
          CASE
            WHEN NULLIF(TRIM(detail_address), '') IS NOT NULL THEN CONCAT(' ', TRIM(detail_address))
            ELSE ''
          END
        )
      ),
      ''
    ) AS address,
    CAST(NULLIF(TRIM(y_coord), '') AS DECIMAL(10,7)) AS lat,
    CAST(NULLIF(TRIM(x_coord), '') AS DECIMAL(10,7)) AS lng,
    COALESCE(NULLIF(TRIM(age_group_name), ''), '초등학생') AS age_group,
    @ELEMENTARY_AGE_MIN AS age_min,
    @ELEMENTARY_AGE_MAX AS age_max,
    0 AS booking_required,
    CASE
      WHEN CAST(COALESCE(NULLIF(REPLACE(TRIM(fee), ',', ''), ''), '0') AS UNSIGNED) = 0 THEN 1
      ELSE 0
    END AS is_free,
    CAST(NULLIF(REPLACE(TRIM(fee), ',', ''), '') AS UNSIGNED) AS fee,
    NULL AS monthly_fee,
    NULL AS capacity_regular,
    NULL AS capacity_temporary,
    NULL AS area_sqm,
    CONCAT_WS(
      ' / ',
      CASE
        WHEN NULLIF(TRIM(semester_start), '') IS NOT NULL AND NULLIF(TRIM(semester_end), '') IS NOT NULL
          THEN CONCAT('학기 ', TRIM(semester_start), '-', TRIM(semester_end))
      END,
      CASE
        WHEN NULLIF(TRIM(vacation_start), '') IS NOT NULL AND NULLIF(TRIM(vacation_end), '') IS NOT NULL
          THEN CONCAT('방학 ', TRIM(vacation_start), '-', TRIM(vacation_end))
      END,
      CASE
        WHEN UPPER(TRIM(saturday_open)) = 'Y'
             AND NULLIF(TRIM(saturday_start), '') IS NOT NULL
             AND NULLIF(TRIM(saturday_end), '') IS NOT NULL
          THEN CONCAT('토요일 ', TRIM(saturday_start), '-', TRIM(saturday_end))
      END
    ) AS operating_days,
    CASE
      WHEN UPPER(TRIM(saturday_open)) = 'Y' THEN '일요일'
      ELSE '토요일, 일요일'
    END AS closed_days,
    1 AS has_regular_program,
    1 AS has_regular_care,
    0 AS has_temporary_care,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS created_at,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(updated_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS updated_at
  FROM stg_child_center

  UNION ALL

  SELECT
    TRIM(facility_id) AS facility_id,
    TRIM(facility_name) AS facility_name,
    'YOUTH_ACADEMY' AS service_type_code,
    '청소년방과후아카데미' AS service_type,
    TRIM(district_code) AS district_code,
    TRIM(district_name) AS district_name,
    TRIM(
      CONCAT(
        COALESCE(NULLIF(TRIM(base_address), ''), ''),
        CASE
          WHEN NULLIF(TRIM(detail_address), '') IS NOT NULL THEN CONCAT(' ', TRIM(detail_address))
          ELSE ''
        END
      )
    ) AS address,
    CAST(NULLIF(TRIM(y_coord), '') AS DECIMAL(10,7)) AS lat,
    CAST(NULLIF(TRIM(x_coord), '') AS DECIMAL(10,7)) AS lng,
    '청소년' AS age_group,
    @YOUTH_AGE_MIN AS age_min,
    @YOUTH_AGE_MAX AS age_max,
    0 AS booking_required,
    CASE
      WHEN UPPER(TRIM(is_free_flag)) = 'Y' THEN 1
      WHEN CAST(COALESCE(NULLIF(REPLACE(TRIM(fee), ',', ''), ''), '0') AS UNSIGNED) = 0 THEN 1
      ELSE 0
    END AS is_free,
    CAST(NULLIF(REPLACE(TRIM(fee), ',', ''), '') AS UNSIGNED) AS fee,
    NULL AS monthly_fee,
    NULL AS capacity_regular,
    NULL AS capacity_temporary,
    NULL AS area_sqm,
    CONCAT_WS(
      ' / ',
      CASE
        WHEN NULLIF(TRIM(semester_start), '') IS NOT NULL AND NULLIF(TRIM(semester_end), '') IS NOT NULL
          THEN CONCAT('학기 ', TRIM(semester_start), '-', TRIM(semester_end))
      END,
      CASE
        WHEN NULLIF(TRIM(vacation_start), '') IS NOT NULL AND NULLIF(TRIM(vacation_end), '') IS NOT NULL
          THEN CONCAT('방학 ', TRIM(vacation_start), '-', TRIM(vacation_end))
      END,
      CASE
        WHEN UPPER(TRIM(saturday_open)) = 'Y'
             AND NULLIF(TRIM(saturday_start), '') IS NOT NULL
             AND NULLIF(TRIM(saturday_end), '') IS NOT NULL
          THEN CONCAT('토요일 ', TRIM(saturday_start), '-', TRIM(saturday_end))
      END
    ) AS operating_days,
    CASE
      WHEN UPPER(TRIM(saturday_open)) = 'Y' THEN '일요일'
      ELSE '토요일, 일요일'
    END AS closed_days,
    1 AS has_regular_program,
    1 AS has_regular_care,
    0 AS has_temporary_care,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS created_at,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(updated_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS updated_at
  FROM stg_youth_academy

  UNION ALL

  SELECT
    TRIM(facility_id) AS facility_id,
    TRIM(facility_name) AS facility_name,
    'KIDS_CAFE' AS service_type_code,
    '서울형키즈카페' AS service_type,
    TRIM(district_code) AS district_code,
    TRIM(district_name) AS district_name,
    TRIM(
      CONCAT(
        COALESCE(NULLIF(TRIM(base_address), ''), ''),
        CASE
          WHEN NULLIF(TRIM(detail_address), '') IS NOT NULL THEN CONCAT(' ', TRIM(detail_address))
          ELSE ''
        END
      )
    ) AS address,
    CAST(NULLIF(TRIM(y_coord), '') AS DECIMAL(10,7)) AS lat,
    CAST(NULLIF(TRIM(x_coord), '') AS DECIMAL(10,7)) AS lng,
    COALESCE(NULLIF(TRIM(age_range), ''), '영유아') AS age_group,
    COALESCE(
      CAST(REGEXP_SUBSTR(REPLACE(TRIM(age_range), ' ', ''), '[0-9]+', 1, 1) AS UNSIGNED),
      @INFANT_AGE_MIN
    ) AS age_min,
    COALESCE(
      CAST(REGEXP_SUBSTR(REPLACE(TRIM(age_range), ' ', ''), '[0-9]+', 1, 2) AS UNSIGNED),
      CAST(REGEXP_SUBSTR(REPLACE(TRIM(age_range), ' ', ''), '[0-9]+', 1, 1) AS UNSIGNED),
      @INFANT_AGE_MAX
    ) AS age_max,
    0 AS booking_required,
    CASE
      WHEN UPPER(TRIM(is_free_flag)) = 'Y' THEN 1
      ELSE 0
    END AS is_free,
    NULL AS fee,
    NULL AS monthly_fee,
    CAST(NULLIF(REPLACE(TRIM(capacity_regular), ',', ''), '') AS UNSIGNED) AS capacity_regular,
    CAST(NULLIF(REPLACE(TRIM(capacity_temporary), ',', ''), '') AS UNSIGNED) AS capacity_temporary,
    NULL AS area_sqm,
    NULLIF(TRIM(operating_days), '') AS operating_days,
    NULLIF(TRIM(closed_days), '') AS closed_days,
    1 AS has_regular_program,
    0 AS has_regular_care,
    0 AS has_temporary_care,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS created_at,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(updated_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS updated_at
  FROM stg_kids_cafe
) src
ON DUPLICATE KEY UPDATE
  facility_name = VALUES(facility_name),
  service_type_code = VALUES(service_type_code),
  service_type = VALUES(service_type),
  district_code = VALUES(district_code),
  district_name = VALUES(district_name),
  address = VALUES(address),
  lat = VALUES(lat),
  lng = VALUES(lng),
  age_group = VALUES(age_group),
  age_min = VALUES(age_min),
  age_max = VALUES(age_max),
  booking_required = VALUES(booking_required),
  is_free = VALUES(is_free),
  fee = VALUES(fee),
  monthly_fee = VALUES(monthly_fee),
  capacity_regular = VALUES(capacity_regular),
  capacity_temporary = VALUES(capacity_temporary),
  area_sqm = VALUES(area_sqm),
  operating_days = VALUES(operating_days),
  closed_days = VALUES(closed_days),
  has_regular_program = VALUES(has_regular_program),
  has_regular_care = VALUES(has_regular_care),
  has_temporary_care = VALUES(has_temporary_care),
  updated_at = VALUES(updated_at);

INSERT INTO facility_external_info (
  facility_id,
  phone,
  website,
  naver_hours,
  business_status,
  naver_address,
  fee_text,
  fee_image_url,
  thumbnail_url,
  synced_at
)
SELECT
  ext.facility_id,
  ext.phone,
  ext.website,
  NULL,
  NULL,
  NULL,
  NULL,
  NULL,
  NULL,
  ext.synced_at
FROM (
  SELECT
    TRIM(facility_id) AS facility_id,
    NULL AS phone,
    NULLIF(TRIM(site_url), '') AS website,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(updated_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS synced_at
  FROM stg_childcare_sharing_center

  UNION ALL

  SELECT
    TRIM(facility_id) AS facility_id,
    NULL AS phone,
    NULLIF(TRIM(site_url), '') AS website,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(updated_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS synced_at
  FROM stg_shared_childcare

  UNION ALL

  SELECT
    TRIM(facility_id) AS facility_id,
    NULL AS phone,
    NULL AS website,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(updated_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS synced_at
  FROM stg_kium_center

  UNION ALL

  SELECT
    TRIM(facility_id) AS facility_id,
    NULLIF(TRIM(phone), '') AS phone,
    NULL AS website,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(updated_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS synced_at
  FROM stg_child_center

  UNION ALL

  SELECT
    TRIM(facility_id) AS facility_id,
    NULL AS phone,
    NULLIF(TRIM(site_url), '') AS website,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(updated_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS synced_at
  FROM stg_youth_academy

  UNION ALL

  SELECT
    TRIM(facility_id) AS facility_id,
    NULLIF(TRIM(phone), '') AS phone,
    NULL AS website,
    COALESCE(
      STR_TO_DATE(LEFT(NULLIF(TRIM(updated_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      STR_TO_DATE(LEFT(NULLIF(TRIM(created_at_raw), ''), 19), '%Y-%m-%d %H:%i:%s'),
      NOW()
    ) AS synced_at
  FROM stg_kids_cafe
) ext
ON DUPLICATE KEY UPDATE
  phone = COALESCE(facility_external_info.phone, VALUES(phone)),
  website = COALESCE(facility_external_info.website, VALUES(website)),
  synced_at = GREATEST(facility_external_info.synced_at, VALUES(synced_at));

INSERT INTO facility_stats (
  facility_id,
  avg_rating,
  avg_rating_safety,
  avg_rating_cleanliness,
  avg_rating_management,
  avg_rating_kindness,
  review_count,
  updated_at
)
SELECT
  f.facility_id,
  0.0,
  0.0,
  0.0,
  0.0,
  0.0,
  0,
  NOW()
FROM facilities f
LEFT JOIN facility_stats s ON s.facility_id = f.facility_id
WHERE f.service_type_code IN (
  'CHILD_CENTER',
  'KIUM_CENTER',
  'KIDS_CAFE',
  'SHARED_CHILDCARE',
  'CHILDCARE_SHARING_CENTER',
  'YOUTH_ACADEMY'
)
  AND s.facility_id IS NULL;

SELECT service_type_code, COUNT(*) AS cnt
FROM facilities
WHERE service_type_code IN (
  'CHILD_CENTER',
  'KIUM_CENTER',
  'KIDS_CAFE',
  'SHARED_CHILDCARE',
  'CHILDCARE_SHARING_CENTER',
  'YOUTH_ACADEMY'
)
GROUP BY service_type_code
ORDER BY service_type_code;
