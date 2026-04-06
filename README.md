# AIDOM Backend

AIDOM 프로젝트의 백엔드 API 서버입니다.

## 기술 스택

- Java 17
- Spring Boot 3.4.4
- Spring Data JPA
- MySQL
- Gradle
- Docker

## 프로젝트 구조

```
src/main/java/com/aidom/api/
├── AidomBackendApplication.java    # 메인 진입점
└── global/
    ├── config/                     # 공통 설정 (Swagger, JPA)
    ├── error/                      # 글로벌 예외 처리
    └── common/                     # 공통 클래스 (BaseEntity)
```

도메인별 패키지는 개발 시 추가합니다. (예: `auth/`, `member/`)

## 로컬 개발 환경 세팅

### 1. 프로젝트 클론

```bash
git clone https://github.com/Seoul-Ai-dom/aidom-backend.git
cd aidom-backend
```

### 2. `.env` 파일 생성 (MySQL 설정)

프로젝트 루트에 `.env` 파일을 만들어주세요. (Git에 올라가지 않습니다)

```bash
# .env
MYSQL_DATABASE=aidom
MYSQL_USER=aidomuser
MYSQL_PASSWORD=aidomsecret
MYSQL_ROOT_PASSWORD=aidomverysecret
```

### 3. `application-local.properties` 생성

`src/main/resources/application-local.properties` 파일을 만들어주세요.

```properties
# Local Development - Docker Compose MySQL
spring.datasource.url=jdbc:mysql://localhost:3307/aidom
spring.datasource.username=aidomuser
spring.datasource.password=aidomsecret
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

> **참고:** 포트가 `3307`인 이유는 로컬에 다른 MySQL이 3306을 쓸 수 있기 때문입니다. 3306이 비어있다면 `compose.yaml`과 이 파일 모두 `3306`으로 변경해도 됩니다.

### 4. MySQL 컨테이너 실행

```bash
docker compose up -d
```

### 5. 애플리케이션 실행

```bash
./gradlew bootRun
```

실행 후 Swagger UI에서 API를 확인할 수 있습니다: http://localhost:8080/swagger-ui.html

## 주요 명령어

| 명령어 | 설명 |
|--------|------|
| `./gradlew bootRun` | 애플리케이션 실행 |
| `./gradlew build` | 빌드 + 테스트 |
| `./gradlew test` | 테스트만 실행 |
| `./gradlew spotlessApply` | 코드 포맷 자동 적용 |
| `./gradlew spotlessCheck` | 코드 포맷 검증 (CI에서 사용) |

## 코드 컨벤션

- **Spotless + Google Java Format**이 적용되어 있습니다.
- 커밋 전에 `./gradlew spotlessApply`를 실행해주세요.
- PR 시 CI가 자동으로 `spotlessCheck`를 검증합니다.

## 브랜치 전략 (Git-flow)

| 브랜치 | 용도 |
|--------|------|
| `main` | 운영 배포 브랜치 |
| `dev` | 개발 통합 브랜치 |
| `feature/*` | 새 기능 개발 |
| `fix/*` | 버그 수정 |
| `hotfix/*` | 운영 긴급 수정 |

## CI/CD

### CI (Pull Request)
PR을 `dev` 또는 `main`에 올리면 자동으로 실행됩니다.
- 코드 포맷 검증 (`spotlessCheck`)
- 빌드 + 테스트 (`build`)

### CD (배포)
`main`에 push되면 자동으로 배포됩니다.
1. JAR 빌드
2. Docker 이미지 빌드 → GHCR에 push
3. EC2에 SSH 접속 → Docker pull & run

### 배포에 필요한 GitHub Secrets

GitHub 레포 → **Settings → Secrets and variables → Actions**에서 등록:

| Secret | 설명 | 예시 |
|--------|------|------|
| `EC2_HOST` | EC2 퍼블릭 IP | `3.123.456.789` |
| `EC2_USER` | EC2 SSH 사용자 | `ubuntu` |
| `EC2_SSH_KEY` | EC2 SSH 프라이빗 키 | `-----BEGIN OPENSSH...` |
| `DB_URL` | 운영 DB JDBC URL | `jdbc:mysql://rds주소:3306/aidom` |
| `DB_USERNAME` | 운영 DB 사용자 | `admin` |
| `DB_PASSWORD` | 운영 DB 비밀번호 | `****` |

## 민감 정보 관리

아래 파일들은 `.gitignore`에 의해 Git에 올라가지 않습니다.

- `.env` — Docker Compose MySQL 비밀번호
- `src/main/resources/application-local.properties` — 로컬 DB 접속 정보
- `src/main/resources/application-prod.properties` — 운영 DB 접속 정보
