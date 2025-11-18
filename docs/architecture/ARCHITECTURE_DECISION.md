# 아키텍처 설계 결정

예약 관리 서비스의 아키텍처 선택 과정과 설계 결정을 문서화합니다.

---

## 목차

- [의사결정 개요](#의사결정-개요)
- [아키텍처 패턴 선택](#아키텍처-패턴-선택)
- [도메인 모델 전략](#도메인-모델-전략)
- [데이터 저장 전략](#데이터-저장-전략)
- [이벤트 처리 전략](#이벤트-처리-전략)
- [통합 결정](#통합-결정)

---

## 의사결정 개요

### 의사결정 원칙

1. **실용주의 (Pragmatism)**: 이론보다 실제 효과 우선
2. **단순성 (Simplicity)**: 불필요한 복잡도 배제
3. **확장성 (Scalability)**: 향후 성장 대비
4. **유지보수성 (Maintainability)**: 1인 개발자 장기 운영 가능
5. **증명 가능성 (Provability)**: 모든 결정에 근거 제시

### 의사결정 프로세스

```
1. 문제 정의
   ↓
2. 대안 식별 (최소 3개)
   ↓
3. 평가 기준 설정
   ↓
4. 대안 비교 분석
   ↓
5. 최종 선택 및 근거 문서화
   ↓
6. Trade-off 명시
```

---

## 아키텍처 패턴 선택

### 문제 정의

예약 관리 서비스는 다음 특성을 가집니다:
- **비즈니스 로직 복잡도**: 중간 (상태 전이, 블랙리스트 체크)
- **외부 의존성**: 높음 (예약 서버, 쿠폰 서비스, 시간 관리 서버)
- **확장 가능성**: 높음 (통계 서버 분리 예정)
- **개발 리소스**: 1인 개발자
- **운영 기간**: 장기 (2년 이상)

**핵심 질문:**
> 단순성과 확장성을 어떻게 균형 잡을 것인가?

---

### 대안 분석

#### Option 1: Layered Architecture

**구조:**
```
Controller → Service → Repository → Entity
```

**장점:**
- 학습 곡선 낮음
- 빠른 개발 속도
- 직관적인 구조

**단점:**
- 계층 간 강한 결합
- 도메인 로직이 JPA에 의존
- 통계 서버 분리 시 리팩토링 비용 높음
- 외부 의존성 교체 어려움

**평가:**
| 기준 | 점수 | 설명 |
|------|------|------|
| 개발 속도 | 5/5 | 매우 빠름 |
| 테스트 용이성 | 2/5 | JPA Mock 필요 |
| 확장성 | 2/5 | 경계 불명확 |
| 1인 유지보수 | 5/5 | 매우 단순 |
| 장기 운영 대응 | 2/5 | 변경 비용 높음 |
| 도메인 순수성 | 1/5 | 기술 의존적 |
| **총점** | **17/30** | |

---

#### Option 2: 실용적 Hexagonal Architecture

**구조:**
```
Adapter (In/Out) → Application (Use Case) → Domain (경량)
```

**장점:**
- Port/Adapter로 경계 명확
- 통계 서버 분리 용이
- 외부 의존성 교체 가능
- 도메인 로직 독립 테스트

**단점:**
- 초기 구조 설계 시간 소요
- Mapper 코드 필요 (복잡한 경우만)
- 러닝 커브 존재

**평가:**
| 기준 | 점수 | 설명 |
|------|------|------|
| 개발 속도 | 4/5 | 초기 10% 느림 |
| 테스트 용이성 | 4/5 | Port Mock 가능 |
| 확장성 | 5/5 | Port 이동만으로 분리 |
| 1인 유지보수 | 4/5 | 구조 명확 |
| 장기 운영 대응 | 5/5 | 변경 영향 최소화 |
| 도메인 순수성 | 3/5 | 경량 도메인 |
| **총점** | **25/30** | |

---

#### Option 3: 완전한 DDD + Hexagonal

**구조:**
```
Adapter → Application → Domain (Aggregate, Value Object, Domain Event 등)
```

**장점:**
- 도메인 완전 격리
- 복잡한 비즈니스 로직 표현력 최고
- 레퍼런스 프로젝트와 일관성

**단점:**
- 개발 시간 2배 이상
- Mapper 코드 방대
- 단순한 로직에 오버엔지니어링
- 1인 개발자에게 부담

**평가:**
| 기준 | 점수 | 설명 |
|------|------|------|
| 개발 속도 | 2/5 | 매우 느림 |
| 테스트 용이성 | 5/5 | 완벽한 격리 |
| 확장성 | 5/5 | 최고 수준 |
| 1인 유지보수 | 2/5 | 복잡도 높음 |
| 장기 운영 대응 | 5/5 | 변경 영향 최소화 |
| 도메인 순수성 | 5/5 | 완전 독립 |
| **총점** | **24/30** | |

---

### 최종 선택: Option 2 (실용적 Hexagonal)

**선택 이유:**

1. **최고 점수 (25/30)**
   - 확장성과 유지보수성에서 5/5
   - 1인 개발 가능 (4/5)
   - 개발 속도 허용 가능 (4/5)

2. **통계 서버 분리 준비**
   ```
   현재: Reservation Manager (예약 + 통계)
   향후: Reservation Manager + Analytics Service

   분리 방법:
   - StatisticsPort 인터페이스를 Analytics Service로 이동
   - Kafka 이벤트 구독으로 데이터 동기화
   - 기존 코드 변경 최소화
   ```

3. **비즈니스 로직 복잡도 적정**
   - 상태 전이 로직: Domain으로 분리
   - 단순 검증: Entity에서 처리
   - 과도한 추상화 배제

4. **레퍼런스 일관성 유지**
   - 레퍼런스 프로젝트도 Hexagonal 사용
   - 코드 스타일 일관성
   - 팀 내 지식 공유 용이

**Trade-off 수용:**
- Layered 대비 초기 개발 10% 느림 → 장기 운영 시 회수
- 완전한 DDD 대비 도메인 순수성 낮음 → 현재 복잡도에 적합

---

### 의사결정 기록 (ADR-001)

**Status:** Accepted

**Context:**
1인 개발자가 장기 운영할 예약 관리 서비스의 아키텍처 선택

**Decision:**
실용적 Hexagonal Architecture 채택

**Consequences:**

**긍정적:**
- 통계 서버 분리 시 Port 이동만으로 가능
- 외부 서비스 교체 용이 (쿠폰 서비스 REST → gRPC)
- 도메인 로직 테스트 독립적

**부정적:**
- 초기 패키지 구조 설계 필요
- Port 인터페이스 정의 시간 소요
- 팀원 추가 시 교육 필요

---

## 도메인 모델 전략

### 문제 정의

**핵심 질문:**
> Domain 모델과 Persistence 모델을 얼마나 분리할 것인가?

**고려사항:**
- 비즈니스 로직 복잡도: 낮음~중간
- 상태 전이 로직: 있음 (PENDING → PAID → CONFIRMED)
- JPA 의존성 허용도: 낮음

---

### 대안 분석

#### Option A: 완전 분리

**구조:**
```java
// Domain Layer (순수 Java)
public class Reservation {
  private ReservationId id;
  private BookerId bookerId;
  private ReservationStatus status;

  public void confirm() {
    if (status != ReservationStatus.PAID) {
      throw new IllegalStateException("결제 완료 상태만 확정 가능");
    }
    this.status = ReservationStatus.CONFIRMED;
  }
}

// Persistence Layer (JPA)
@Entity
public class ReservationEntity {
  @Id private Long id;
  private Long bookerId;
  private String status;
}

// Mapper
public class ReservationMapper {
  public static Reservation toDomain(ReservationEntity entity) { }
  public static ReservationEntity toEntity(Reservation domain) { }
}
```

**장점:**
- Domain이 JPA 완전 독립
- 비즈니스 로직 테스트 용이
- 레퍼런스 일관성

**단점:**
- Mapper 코드 방대 (Embeddable, ElementCollection 등)
- 단순 CRUD에 오버헤드
- 개발 시간 증가

**평가:**
| 기준 | 점수 |
|------|------|
| 개발 속도 | 2/5 |
| 도메인 순수성 | 5/5 |
| 유지보수성 | 3/5 |
| **총점** | **10/15** |

---

#### Option B: 통합

**구조:**
```java
@Entity
public class ReservationEntity {
  @Id private Long id;
  private Long bookerId;

  @Enumerated(EnumType.STRING)
  private ReservationStatus status;

  // 도메인 로직 포함
  public void confirm() {
    if (status != ReservationStatus.PAID) {
      throw new IllegalStateException("결제 완료 상태만 확정 가능");
    }
    this.status = ReservationStatus.CONFIRMED;
  }
}
```

**장점:**
- 매우 단순
- Mapper 불필요
- 빠른 개발

**단점:**
- JPA 의존
- 테스트 어려움 (EntityManager Mock)
- Domain 로직이 기술에 오염

**평가:**
| 기준 | 점수 |
|------|------|
| 개발 속도 | 5/5 |
| 도메인 순수성 | 1/5 |
| 유지보수성 | 2/5 |
| **총점** | **8/15** |

---

#### Option C: 하이브리드 (선택)

**구조:**
```java
// Domain Layer (핵심 로직만)
public class Reservation {
  private ReservationStatus status;

  // 복잡한 비즈니스 로직만 Domain에
  public void confirm() {
    validateConfirmable();
    this.status = ReservationStatus.CONFIRMED;
  }

  private void validateConfirmable() {
    if (status != ReservationStatus.PAID) {
      throw new IllegalStateException("결제 완료 상태만 확정 가능");
    }
  }
}

// Persistence Layer (JPA + 간단한 검증)
@Entity
public class ReservationEntity {
  @Id private Long id;

  @Embedded
  private ReservationTimeInfo timeInfo;  // 간단한 검증만

  // 복잡한 로직은 Domain 위임
  public void confirm() {
    Reservation domain = toDomain();
    domain.confirm();
    this.status = domain.getStatus();
  }
}
```

**장점:**
- 핵심 로직만 분리 (실용적)
- 단순 검증은 Entity에서 처리
- Mapper 최소화

**단점:**
- 분리 기준 판단 필요
- 일관성 유지 주의

**평가:**
| 기준 | 점수 |
|------|------|
| 개발 속도 | 4/5 |
| 도메인 순수성 | 3/5 |
| 유지보수성 | 4/5 |
| **총점** | **11/15** |

---

### 최종 선택: Option C (하이브리드)

**선택 이유:**

1. **비즈니스 로직 복잡도 적정**
   - 상태 전이: Domain
   - 필드 검증: Entity
   - 만료 체크: Domain

2. **개발 효율성**
   - 모든 것을 분리할 필요 없음
   - 복잡해지면 점진적으로 Domain 이동

3. **분리 기준 명확화**
   ```
   Domain으로 분리:
   - 상태 전이 규칙
   - 비즈니스 정책 검증
   - 복잡한 계산 로직

   Entity에 유지:
   - 필드 null 체크
   - 범위 검증 (quantity > 0)
   - JPA 매핑
   ```

**Trade-off 수용:**
- 완전 분리 대비 순수성 낮음 → 현재 복잡도 적합
- 통합 대비 Mapper 필요 → 복잡한 Aggregate만

---

### 의사결정 기록 (ADR-002)

**Status:** Accepted

**Context:**
Domain 모델과 Persistence 모델의 분리 수준 결정

**Decision:**
하이브리드 방식 채택 (핵심 로직만 Domain 분리)

**Consequences:**

**긍정적:**
- 개발 속도 유지
- 핵심 로직 테스트 가능
- 점진적 개선 가능

**부정적:**
- 분리 기준 판단 필요
- 팀원 간 기준 공유 필요

---

## 데이터 저장 전략

### 문제 정의

**핵심 질문:**
> 가격 정보를 얼마나 상세히 저장할 것인가?

**배경:**
- 부분 환불 가능성 (상품만, 시간만)
- 통계 분석 요구사항
- 저장 공간 vs 분석 능력

---

### 대안 분석

#### Option A: 상세 Breakdown

**저장 데이터:**
```java
ReservationEntity
├─ timeSlotTotalPrice (시간 총액)
├─ productTotalPrice (상품 총액)
├─ baseTotalPrice (할인 전)
├─ totalDiscountAmount (할인액)
└─ finalTotalPrice (최종)

+ TimeSlotPriceSnapshots (ElementCollection)
  - { startTime: "11:00", price: 10000 }
  - { startTime: "12:00", price: 15000 }
```

**장점:**
- 부분 환불 정밀 처리
- 시간대별 매출 분석
- 할인 효과 분석

**단점:**
- 저장 공간 3배
- 초기 개발 복잡
- 예약 서버에서 상세 데이터 제공 필요

**평가:**
| 기준 | 점수 |
|------|------|
| 개발 속도 | 2/5 |
| 분석 능력 | 5/5 |
| 저장 효율 | 2/5 |
| **총점** | **9/15** |

---

#### Option B: 단순 버전 (선택)

**저장 데이터:**
```java
ReservationEntity
└─ totalPrice (최종 금액만)

ReservationTimeInfo
└─ startTimes: ["11:00", "12:00"] (가격 정보 없음)

ProductSnapshot
├─ unitPrice
└─ totalPrice (unitPrice × quantity)
```

**장점:**
- 매우 단순
- 빠른 개발
- 저장 공간 효율

**단점:**
- 부분 환불 시 계산 필요
- 시간대별 분석 제한

**평가:**
| 기준 | 점수 |
|------|------|
| 개발 속도 | 5/5 |
| 분석 능력 | 2/5 |
| 저장 효율 | 5/5 |
| **총점** | **12/15** |

---

### 최종 선택: Option B (단순 버전)

**선택 이유:**

1. **YAGNI 원칙**
   - 현재 요구사항: 총액만 필요
   - 부분 환불: 명세 없음
   - 시간대 분석: 우선순위 낮음

2. **나중에 추가 가능**
   ```sql
   -- V2 마이그레이션 (필요 시)
   ALTER TABLE reservations
     ADD COLUMN time_slot_total_price DECIMAL(10,2),
     ADD COLUMN product_total_price DECIMAL(10,2);

   -- 기존 데이터 근사치로 채우기
   UPDATE reservations
     SET time_slot_total_price = total_price * 0.7,
         product_total_price = total_price * 0.3;
   ```

3. **초기 개발 집중**
   - 핵심 기능 빠르게 완성
   - 사용자 피드백 수집
   - 실제 니즈 확인 후 확장

**Trade-off 수용:**
- 상세 분석 제한 → 초기엔 불필요
- 마이그레이션 비용 → 허용 가능

---

### 의사결정 기록 (ADR-003)

**Status:** Accepted

**Context:**
가격 정보 저장 수준 결정

**Decision:**
단순 버전 채택 (totalPrice만, 향후 확장 가능)

**Consequences:**

**긍정적:**
- 빠른 MVP 출시
- 저장 공간 효율
- 개발 복잡도 최소

**부정적:**
- 부분 환불 시 계산 필요
- 향후 마이그레이션 필요 가능

---

## 이벤트 처리 전략

### 멱등성 보장

**문제:**
Kafka 이벤트 중복 수신 가능성

**현재 전략 (v1.0):**
```java
@KafkaListener(topics = "reservation.created")
public void handleReservationCreated(ReservationCreatedEvent event) {
  Long reservationId = event.getReservationId();

  // 중복 체크: 이미 존재하면 무시
  if (repository.existsById(reservationId)) {
    log.warn("Duplicate event: {}", reservationId);
    return;
  }

  // 저장
  Reservation reservation = Reservation.from(event);
  repository.save(reservation);
}
```

**향후 개선 (v2.0):**
- 이벤트 ID 기반 중복 처리
- Outbox Pattern 적용

---

## 통합 결정

### 최종 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                      Adapter Layer                              │
│                                                                 │
│  Kafka Consumer ──┐                       ┌── JPA Repository   │
│  REST Controller ─┼───► Use Case ◄────────┼── Kafka Producer   │
│                   │     Service           │                     │
│                   └─────────┬─────────────┘                     │
│                             │                                   │
│                   ┌─────────▼─────────┐                         │
│                   │  Domain (경량)     │                         │
│                   │  - 상태 전이 로직   │                         │
│                   │  - 만료 체크       │                         │
│                   └───────────────────┘                         │
└─────────────────────────────────────────────────────────────────┘

ADR-001: 실용적 Hexagonal
ADR-002: 하이브리드 Domain/Entity
ADR-003: 단순 가격 저장
```

### 설계 원칙 준수

| 원칙 | 적용 방법 | 증거 |
|------|-----------|------|
| SRP | Use Case별 Service 분리 | CreateReservationUseCase, ConfirmReservationUseCase |
| OCP | Port 인터페이스로 확장 | CouponServicePort → REST/gRPC 교체 가능 |
| LSP | Strategy Pattern 미적용 | 현재 복잡도 불필요 |
| ISP | Use Case별 Port 분리 | 클라이언트별 인터페이스 |
| DIP | Domain이 Port 정의 | ReservationRepository in application/ |

---

## 참고 자료

### 관련 문서

- [PROJECT_REQUIREMENTS.md](../requirements/PROJECT_REQUIREMENTS.md)
- [DOMAIN_MODEL_DESIGN.md](DOMAIN_MODEL_DESIGN.md)
- [README.md - 아키텍처](../../README.md#아키텍처)

### 외부 참조

- Clean Architecture (Robert C. Martin)
- Hexagonal Architecture (Alistair Cockburn)
- Domain-Driven Design (Eric Evans)

---

## 구현 노트 (2025-01-17)

이 문서는 초기 아키텍처 의사결정을 기록한 것입니다. 실제 구현에서 일부 변경된 사항들:

### 1. 도메인 모델 전략

**초기 계획:** 하이브리드 방식 (Domain과 Entity 분리)

**실제 구현:** Entity 중심 설계
- ReservationEntity에 도메인 로직 포함
- Value Objects (PeriodType, DateRange, ReservationCursor, PriceInfo)는 별도 분리
- Mapper 최소화로 개발 속도 향상
- DDD 원칙은 Value Object를 통해 적용

### 2. 데이터 저장 전략

**초기 계획:** Embedded + ElementCollection

**실제 구현:** 단일 테이블 + JSONB
- product_data, additional_info, time_slots를 JSONB로 저장
- 유연성 증대 및 스키마 변경 최소화
- PostgreSQL JSONB 인덱싱 활용 가능

### 3. 개인정보 보호

**추가 구현:** AES 암호화
- reserver_name, reserver_phone 필드 AES-256-CBC 암호화
- 환경 변수로 암호화 키 관리
- 조회 시 자동 복호화

### 4. 커서 기반 페이징

**추가 구현:** 복합 커서 페이징
- ReservationCursor VO (reservationDate + reservationId)
- Base64 인코딩/디코딩
- V6 복합 인덱스 최적화

### 5. 블랙리스트 처리

**초기 계획:** 자동 거절 및 환불 이벤트 발행

**실제 구현:** 조회 정보만 제공
- 블랙리스트 자동 거절 기능 제거
- 운영자 조회 API에서 블랙리스트 여부 표시
- 운영자가 수동으로 판단 및 처리

이러한 변경사항들은 실제 개발 과정에서 발견된 요구사항과 효율성을 반영한 것입니다.

---

Last Updated: 2025-01-17
