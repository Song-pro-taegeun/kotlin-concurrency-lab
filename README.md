# kotlin-concurrency-lab

Kotlin Coroutines와 Java Virtual Threads를 중심으로 동시성 처리 방식을 학습하고 비교하는 Spring Boot 프로젝트입니다.

## 기술 스택

- Java 21
- Kotlin 2.3.21
- Spring Boot 4.1.3 / Spring MVC
- Kotlin Coroutines
- Spring Data JPA
- OpenFeign QueryDSL 7.7 + KSP
- MariaDB 11.4.13 LTS
- springdoc-openapi / Swagger UI

## 로컬 실행

애플리케이션은 로컬에서 실행하고 MariaDB만 Docker Compose로 실행합니다.

```bash
docker compose up -d mariadb
./gradlew bootRun
```

기본 접속 정보:

| 구분 | 주소 |
| --- | --- |
| API 상태 확인 | http://localhost:8080/api/v1/health |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| MariaDB | localhost:3308 |

환경 변수를 변경하려면 `.env.example`을 `.env`로 복사한 뒤 값을 수정합니다. `.env`는 Git에 포함되지 않습니다.

## Virtual Thread 프로필

기본 프로필은 플랫폼 스레드를 사용합니다. Virtual Thread를 활성화하려면 다음과 같이 실행합니다.

```bash
./gradlew bootRun --args='--spring.profiles.active=virtual'
```

두 실행 방식에서 동일한 API의 처리량, 지연 시간, 스레드 수를 비교할 예정입니다.
