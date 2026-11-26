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
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| MariaDB | localhost:3308 |

환경 변수를 변경하려면 `.env.example`을 `.env`로 복사한 뒤 값을 수정합니다. `.env`는 Git에 포함되지 않습니다.

현재 학습 단계에서는 `spring.jpa.hibernate.ddl-auto=create`를 사용합니다. 애플리케이션을 다시 실행할 때마다 기존 테이블과 데이터가 초기화됩니다.

## 동기 Blocking 기준 API

비교에 사용할 샘플 상품을 먼저 등록합니다.

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "한정판 키보드",
    "price": 150000,
    "stockQuantity": 100,
    "deliveryLeadTimeDays": 2
  }'
```

응답의 상품 ID로 동기 API를 호출합니다.

```bash
curl http://localhost:8080/api/v1/order-summaries/blocking/1
```

상품, 재고, 배송 정보를 각각 순차 조회하며 각 작업은 500ms의 Blocking I/O를 모사합니다. 따라서 워밍업 이후 기준 응답 시간은 약 1.5초입니다. 지연 시간은 `lab.blocking-delay` 설정으로 조정할 수 있습니다.

## Coroutine API

동일한 상품, 재고, 배송 정보를 Coroutine으로 동시에 조회합니다.

```bash
curl http://localhost:8080/api/v1/order-summaries/coroutine/1
```

JPA/JDBC와 `Thread.sleep()`은 Blocking 작업이므로 각 조회를 `Dispatchers.IO`에서 실행합니다. 세 작업이 동시에 실행되어 워밍업 이후 기준 응답 시간은 약 500ms입니다. Coroutine을 사용해도 JPA/JDBC 자체가 Non-blocking으로 바뀌는 것은 아닙니다.

## Virtual Thread API

동일한 Blocking 조회를 작업마다 생성한 Virtual Thread에서 동시에 실행합니다.

```bash
curl http://localhost:8080/api/v1/order-summaries/virtual-thread/1
```

Controller는 `CompletableFuture`를 반환하므로 Spring MVC 요청 스레드는 결과를 기다리며 점유되지 않습니다. 상품, 재고, 배송 조회는 이름이 `order-summary-vt-`로 시작하는 별도 Virtual Thread에서 실행되며, 워밍업 이후 기준 응답 시간은 약 500ms입니다.

비교용 ForkJoinPool API는 다음과 같이 호출합니다.

```bash
curl http://localhost:8080/api/v1/order-summaries/fork-join/1
```

`OrderSummaryThreadComparisonTest`는 두 실행 방식을 각각 10개 동시 요청으로 실행하고 전체 완료 시간과 사용된 스레드 이름을 출력합니다.

## Virtual Thread 프로필

기본 프로필은 플랫폼 스레드를 사용합니다. Virtual Thread를 활성화하려면 다음과 같이 실행합니다.

```bash
./gradlew bootRun --args='--spring.profiles.active=virtual'
```

Virtual Thread API는 활성 프로필과 관계없이 자체 Virtual Thread 실행기를 사용합니다. `virtual` 프로필은 Spring MVC를 포함한 애플리케이션 전체의 Blocking 작업을 Virtual Thread 기반으로 비교할 때 사용합니다.
