# 기술 스택 분석

예약 관리 서비스의 기술 스택 선정 이유와 대안 비교를 문서화합니다.

---

## 목차

- [기술 스택 개요](#기술-스택-개요)
- [언어 및 프레임워크](#언어-및-프레임워크)
- [데이터베이스](#데이터베이스)
- [메시징](#메시징)
- [빌드 및 도구](#빌드-및-도구)
- [운영 및 모니터링](#운영-및-모니터링)

---

## 기술 스택 개요

### 선정된 기술 스택

| 계층 | 기술 | 버전 |
|------|------|------|
| 언어 | Java | 17 LTS |
| 프레임워크 | Spring Boot | 3.5.7 |
| ORM | Spring Data JPA | 3.5.7 |
| 데이터베이스 | PostgreSQL | 16 |
| 메시징 | Apache Kafka | 3.6 |
| 마이그레이션 | Flyway | - |
| 빌드 | Gradle | 8.x |
| 테스트 | JUnit 5, Mockito, AssertJ | - |

### 선정 원칙

1. **안정성 (Stability)**: 프로덕션 검증된 기술
2. **생태계 (Ecosystem)**: 풍부한 라이브러리 및 커뮤니티
3. **호환성 (Compatibility)**: MSA 환경 지원
4. **유지보수성 (Maintainability)**: 장기 지원 (LTS)
5. **학습 곡선 (Learning Curve)**: 적절한 수준

---

## 언어 및 프레임워크

### Java 17 LTS

#### 대안 비교

| 기준 | Java 17 LTS | Java 21 LTS | Kotlin |
|------|-------------|-------------|--------|
| 안정성 | 5/5 (검증됨) | 4/5 (비교적 최신) | 4/5 |
| 생태계 | 5/5 | 5/5 | 3/5 |
| 성능 | 4/5 | 5/5 (Virtual Threads) | 4/5 |
| 호환성 | 5/5 | 4/5 | 4/5 (Java 호환) |
| 장기 지원 | 5/5 (2029까지) | 5/5 (2031까지) | 4/5 |
| 학습 곡선 | 5/5 | 5/5 | 3/5 |
| **총점** | **29/30** | **28/30** | **22/30** |

#### 최종 선택: Java 17 LTS

**선택 이유:**

1. **검증된 안정성**
   - 2021년 출시, 3년 이상 프로덕션 사용
   - 대부분의 라이브러리 지원
   - 버그 패치 충분히 적용됨

2. **장기 지원**
   - 2029년 9월까지 지원
   - 프로젝트 수명 충분히 커버

3. **최신 기능 활용**
   ```java
   // Records (Java 17)
   public record Money(BigDecimal amount) {
     public Money {
       if (amount.compareTo(BigDecimal.ZERO) < 0) {
         throw new IllegalArgumentException("금액은 0 이상");
       }
     }
   }

   // Sealed Classes
   public sealed interface PaymentResult
       permits Success, Failure { }

   // Pattern Matching for Switch
   String message = switch (status) {
     case PENDING -> "대기 중";
     case CONFIRMED -> "확정됨";
     default -> "알 수 없음";
   };
   ```

4. **Java 21 대비 장점**
   - 더 많은 프로덕션 사례
   - 라이브러리 호환성 확실
   - Virtual Threads는 현재 불필요 (I/O 집약 아님)

**Trade-off:**
- Java 21의 Virtual Threads 미사용 → 현재 성능 요구사항 충족
- Kotlin의 간결함 포기 → 팀 전체가 Java 익숙

---

### Spring Boot 3.5.7

#### 대안 비교

| 기준 | Spring Boot 3.x | Spring Boot 2.x | Micronaut |
|------|-----------------|-----------------|-----------|
| 최신 기능 | 5/5 | 3/5 | 5/5 |
| 생태계 | 5/5 | 5/5 | 3/5 |
| 성능 | 4/5 | 3/5 | 5/5 (AOT) |
| 커뮤니티 | 5/5 | 5/5 | 3/5 |
| 학습 곡선 | 4/5 | 5/5 | 3/5 |
| Java 17 지원 | 5/5 (Native) | 4/5 (Backport) | 5/5 |
| **총점** | **28/30** | **25/30** | **24/30** |

#### 최종 선택: Spring Boot 3.5.7

**선택 이유:**

1. **Java 17 최적화**
   - Java 17을 기본 요구사항으로 함
   - Virtual Threads 지원 (향후 활용 가능)

2. **최신 기능**
   - Spring Framework 6.x 기반
   - AOT (Ahead-of-Time) 컴파일 지원
   - Observability 개선 (Micrometer)

3. **안정성**
   - Spring Boot 3.x는 2022년 출시, 충분히 안정화
   - 대규모 마이그레이션 완료 (많은 기업)

4. **생태계**
   - Spring Data JPA, Spring Kafka 등 완벽 통합
   - 풍부한 문서 및 예제

**Trade-off:**
- Spring Boot 2.x 대비 마이그레이션 비용 → 신규 프로젝트라 무관
- Micronaut 대비 시작 속도 느림 → 현재 요구사항에 충분

---

### Spring Data JPA

#### 대안 비교

| 기준 | Spring Data JPA | MyBatis | jOOQ |
|------|-----------------|---------|------|
| 개발 속도 | 5/5 | 3/5 | 3/5 |
| 쿼리 복잡도 | 3/5 | 5/5 | 5/5 |
| 타입 안정성 | 4/5 | 2/5 | 5/5 |
| 학습 곡선 | 4/5 | 4/5 | 3/5 |
| 생태계 | 5/5 | 4/5 | 3/5 |
| **총점** | **21/25** | **18/25** | **19/25** |

#### 최종 선택: Spring Data JPA

**선택 이유:**

1. **개발 생산성**
   ```java
   // Repository 인터페이스만 정의
   public interface ReservationRepository
       extends JpaRepository<ReservationEntity, Long> {

     List<ReservationEntity> findByBookerId(Long bookerId);

     @Query("SELECT r FROM ReservationEntity r " +
            "WHERE r.placeId = :placeId AND r.status = :status")
     List<ReservationEntity> findByPlaceIdAndStatus(
         @Param("placeId") Long placeId,
         @Param("status") ReservationStatus status
     );
   }
   ```

2. **Hexagonal Architecture 적합**
   - Repository 인터페이스가 Port 역할
   - JPA 구현체가 Adapter

3. **현재 쿼리 복잡도 적정**
   - 단순 CRUD 위주
   - 복잡한 집계 쿼리 없음 (통계는 향후)

4. **Spring Boot 완벽 통합**
   - Auto Configuration
   - Transaction 관리

**Trade-off:**
- MyBatis 대비 복잡한 쿼리 작성 어려움 → Native Query로 해결
- jOOQ 대비 타입 안정성 낮음 → JPQL로 충분

---

## 데이터베이스

### PostgreSQL 16

#### 대안 비교

| 기준 | PostgreSQL | MySQL | MongoDB |
|------|------------|-------|---------|
| ACID 보장 | 5/5 | 5/5 | 3/5 |
| JSON 지원 | 5/5 (JSONB) | 4/5 | 5/5 |
| 복잡한 쿼리 | 5/5 | 4/5 | 3/5 |
| 성능 | 4/5 | 4/5 | 5/5 (읽기) |
| 생태계 | 5/5 | 5/5 | 4/5 |
| 라이선스 | 5/5 (MIT) | 4/5 (GPL) | 4/5 |
| **총점** | **29/30** | **26/30** | **24/30** |

#### 최종 선택: PostgreSQL 16

**선택 이유:**

1. **트랜잭션 무결성**
   - 강력한 ACID 보장
   - 예약 상태 전이에 중요

2. **JSONB 지원**
   ```sql
   -- 향후 확장 시 활용 가능
   ALTER TABLE reservations
     ADD COLUMN metadata JSONB;

   -- JSONB 쿼리
   SELECT * FROM reservations
   WHERE metadata->>'category' = 'premium';
   ```

3. **고급 기능**
   - Window Functions (통계 쿼리)
   - CTE (Common Table Expressions)
   - Full-Text Search

4. **확장성**
   - Partitioning (향후 대용량 데이터)
   - Replication (Master-Slave)

**Trade-off:**
- MySQL 대비 설정 복잡 → Docker Compose로 해결
- MongoDB 대비 스키마 변경 비용 → Flyway로 관리

---

### Flyway (DB 마이그레이션)

#### 대안 비교

| 기준 | Flyway | Liquibase |
|------|--------|-----------|
| 단순성 | 5/5 | 3/5 |
| SQL 직접 작성 | 5/5 | 4/5 (XML/YAML) |
| 롤백 지원 | 3/5 (Pro) | 5/5 |
| Spring Boot 통합 | 5/5 | 5/5 |
| **총점** | **18/20** | **17/20** |

#### 최종 선택: Flyway

**선택 이유:**

1. **단순성**
   ```
   src/main/resources/db/migration/
   ├─ V1__initial_schema.sql
   ├─ V2__add_blacklist_table.sql
   └─ V3__add_indexes.sql
   ```

2. **SQL 직접 작성**
   - XML/YAML 불필요
   - SQL 가독성 높음

3. **Spring Boot Auto Configuration**
   ```yaml
   spring:
     flyway:
       enabled: true
       locations: classpath:db/migration
   ```

**Trade-off:**
- Liquibase 대비 롤백 기능 약함 → 프로덕션에서 롤백 거의 안 함

---

## 메시징

### Apache Kafka 3.6

#### 대안 비교

| 기준 | Kafka | RabbitMQ | Redis Streams |
|------|-------|----------|---------------|
| 처리량 | 5/5 | 3/5 | 4/5 |
| 순서 보장 | 5/5 (Partition) | 4/5 | 5/5 |
| 영속성 | 5/5 | 4/5 | 3/5 |
| 확장성 | 5/5 | 3/5 | 3/5 |
| 운영 복잡도 | 3/5 | 4/5 | 5/5 |
| MSA 적합성 | 5/5 | 4/5 | 3/5 |
| **총점** | **28/30** | **22/30** | **23/30** |

#### 최종 선택: Apache Kafka 3.6

**선택 이유:**

1. **이벤트 소싱 적합**
   - 이벤트 영구 저장
   - 재처리 가능 (Consumer Group Offset 조정)

2. **순서 보장**
   ```java
   // Partition Key로 순서 보장
   @KafkaListener(topics = "payment.completed")
   public void handlePaymentCompleted(
       @Payload PaymentCompletedEvent event,
       @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
     // reservationId로 파티셔닝 → 같은 예약은 순서 보장
   }
   ```

3. **MSA 표준**
   - 대부분의 MSA에서 사용
   - 팀 간 통신 프로토콜 통일

4. **확장성**
   - Consumer Group으로 병렬 처리
   - Topic Partitioning

**Trade-off:**
- RabbitMQ 대비 운영 복잡 → Docker Compose로 완화
- Redis Streams 대비 무거움 → 장기적으로 유리

---

## 빌드 및 도구

### Gradle 8.x

#### 대안 비교

| 기준 | Gradle | Maven |
|------|--------|-------|
| 빌드 속도 | 5/5 (Incremental) | 3/5 |
| 유연성 | 5/5 (Groovy/Kotlin DSL) | 3/5 (XML) |
| 의존성 관리 | 5/5 | 5/5 |
| 학습 곡선 | 3/5 | 4/5 |
| 생태계 | 5/5 | 5/5 |
| **총점** | **23/25** | **20/25** |

#### 최종 선택: Gradle 8.x

**선택 이유:**

1. **빠른 빌드**
   - Incremental Compilation
   - Build Cache
   - Parallel Execution

2. **유연한 설정**
   ```groovy
   // build.gradle
   dependencies {
     implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
     implementation 'org.springframework.kafka:spring-kafka'

     // 조건부 의존성
     if (project.hasProperty('dev')) {
       runtimeOnly 'com.h2database:h2'
     } else {
       runtimeOnly 'org.postgresql:postgresql'
     }
   }
   ```

3. **Spring Boot 권장**
   - Spring Initializr 기본값
   - Spring Boot Gradle Plugin

**Trade-off:**
- Maven 대비 학습 곡선 높음 → 문서 충분

---

### 테스트 프레임워크

#### JUnit 5 + Mockito + AssertJ

**선택 이유:**

1. **JUnit 5**
   - 최신 버전 (Jupiter)
   - Parameterized Test, Nested Test 지원

2. **Mockito**
   - Port 인터페이스 Mocking
   ```java
   @Test
   void confirmReservation_ShouldCheckBlackList() {
     // Given
     when(blackListRepository.existsById(any()))
         .thenReturn(true);

     // When & Then
     assertThatThrownBy(() -> service.confirm(reservationId))
         .isInstanceOf(BlackListException.class);
   }
   ```

3. **AssertJ**
   - Fluent Assertion
   ```java
   assertThat(reservation.getStatus())
       .isEqualTo(ReservationStatus.CONFIRMED);

   assertThat(reservation.getProductSnapshots())
       .hasSize(2)
       .extracting("productName")
       .contains("빔프로젝터", "화이트보드");
   ```

---

## 운영 및 모니터링

### 현재 (v1.0)

| 항목 | 기술 | 상태 |
|------|------|------|
| 로깅 | SLF4J + Logback | 구현 |
| 헬스 체크 | Spring Actuator | 계획 |
| 메트릭 | - | 미정 |
| 분산 추적 | - | 미정 |

### 향후 (v2.0)

| 항목 | 후보 기술 |
|------|-----------|
| 메트릭 | Micrometer + Prometheus |
| 모니터링 | Grafana |
| 분산 추적 | Zipkin or Jaeger |
| 알림 | Slack Webhook |

---

## 버전 관리 전략

### 라이브러리 버전

**LTS 우선:**
- Java 17 LTS (2029까지)
- Spring Boot 3.x (2024년부터 메인스트림)

**안정화된 버전:**
- PostgreSQL 16 (2023년 출시, 1년 이상)
- Kafka 3.6 (2023년 출시)

**업그레이드 계획:**
- Minor 버전: 분기별 검토
- Major 버전: 연 1회 검토
- 보안 패치: 즉시 적용

---

## 의존성 관리

### Gradle Dependencies

```groovy
dependencies {
  // Spring Boot
  implementation 'org.springframework.boot:spring-boot-starter-web'
  implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
  implementation 'org.springframework.kafka:spring-kafka'

  // Database
  runtimeOnly 'org.postgresql:postgresql'
  implementation 'org.flywaydb:flyway-core'

  // Lombok (선택적)
  compileOnly 'org.projectlombok:lombok'
  annotationProcessor 'org.projectlombok:lombok'

  // Test
  testImplementation 'org.springframework.boot:spring-boot-starter-test'
  testImplementation 'org.springframework.kafka:spring-kafka-test'
  testImplementation 'org.mockito:mockito-core'
  testImplementation 'org.assertj:assertj-core'
}
```

---

## 라이선스 검토

| 기술 | 라이선스 | 상업적 사용 |
|------|----------|-------------|
| Java | GPL v2 + Classpath Exception | 가능 |
| Spring Boot | Apache 2.0 | 가능 |
| PostgreSQL | PostgreSQL License (MIT 유사) | 가능 |
| Kafka | Apache 2.0 | 가능 |
| Flyway | Apache 2.0 (Community) | 가능 |

**결론:** 모든 기술 스택이 상업적 사용 가능

---

## 참고 자료

- [ARCHITECTURE_DECISION.md](ARCHITECTURE_DECISION.md)
- [README.md - 기술 스택](../../README.md#기술-스택)

---

Last Updated: 2025-01-12