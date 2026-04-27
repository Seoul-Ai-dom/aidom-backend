-- service_type_code 컬럼 길이를 20 -> 30으로 변경
-- CHILDCARE_SHARING_CENTER (24자) 등 긴 코드값을 수용하기 위함
-- 운영 DB에서 ddl-auto=validate 사용 시, 이 스크립트를 먼저 실행해야 합니다.

ALTER TABLE facilities MODIFY COLUMN service_type_code VARCHAR(30) NOT NULL;
