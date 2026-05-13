-- OAuth2 로그인 직후 생성되는 사용자는 ONBOARDING 상태이며,
-- 생년월일/연락처/거주지 등 상세 프로필은 온보딩 API에서 나중에 입력됩니다.
-- 운영 DB에서 ddl-auto=validate 사용 시, 이 스크립트를 먼저 실행해야 합니다.

ALTER TABLE users MODIFY COLUMN relation VARCHAR(20) NULL;
ALTER TABLE users MODIFY COLUMN birth_date DATE NULL;
ALTER TABLE users MODIFY COLUMN phone VARCHAR(20) NULL;
ALTER TABLE users MODIFY COLUMN address VARCHAR(200) NULL;
ALTER TABLE users MODIFY COLUMN city VARCHAR(50) NULL;
ALTER TABLE users MODIFY COLUMN district VARCHAR(50) NULL;
ALTER TABLE users MODIFY COLUMN address_detail VARCHAR(200) NULL;
ALTER TABLE users MODIFY COLUMN address_lat DECIMAL(10, 7) NULL;
ALTER TABLE users MODIFY COLUMN address_lng DECIMAL(10, 7) NULL;
