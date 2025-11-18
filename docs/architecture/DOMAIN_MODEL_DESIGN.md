# 도메인 모델 설계

예약 정보 관리 서비스의 도메인 모델 및 엔티티 설계를 문서화합니다.

---

## 목차

- [도메인 모델 개요](#도메인-모델-개요)
- [Entity 설계](#entity-설계)
- [Value Object 설계](#value-object-설계)
- [데이터베이스 스키마](#데이터베이스-스키마)
- [아키텍처 패턴](#아키텍처-패턴)

---

## 도메인 모델 개요

### Bounded Context

예약 정보 관리 서비스는 다음 책임을 가집니다:

```
┌──────────────────────────────────────────────────────────┐
│    Reservation Information Manager Service (이 서비스)    │
├──────────────────────────────────────────────────────────┤
│  Domain:                                                 │
│  - Reservation (예약 정보 저장 및 조회)                   │
│  - 예약자 정보 관리 (이름, 전화번호 암호화)               │
│  - 커서 기반 페이징                                       │
│                                                          │
│  External Dependencies:                                  │
│  - 예약 서비스 (ID 생성)                                  │
│  - 결제 서비스 (결제 완료 이벤트)                         │
│  - 장소/방 서비스 (메타데이터)                            │
│  - 블랙리스트 서비스 (블랙리스트 조회)                     │
└──────────────────────────────────────────────────────────┘
         ▲                                ▲
         │ ReservationPendingPaymentEvent │ ReservationConfirmedEvent
         │                                │
┌────────┴────────┐            ┌─────────┴────────┐
│ Reservation Svc │            │  Payment Service │
│  (외부 서비스)   │            │   (외부 서비스)   │
└─────────────────┘            └──────────────────┘
```

### 핵심 개념

**DDD (Domain-Driven Design):**
도메인 중심 설계로 비즈니스 로직을 도메인 레이어에 집중

**Entity:**
식별자를 가지며 생명주기 동안 추적되는 객체 (ReservationEntity)

**Value Object:**
식별자 없이 속성만으로 정의되는 불변 객체 (PeriodType, DateRange, ReservationCursor, PriceInfo)

**Hexagonal Architecture:**
Port and Adapter 패턴으로 외부 의존성 격리

---

## Entity 설계

### ReservationEntity

**패키지:** `adapter.out.persistence.reservation`

**책임:**
- 예약 정보 저장 및 조회
- 예약 상태 관리
- 예약자 개인정보 암호화 저장

**클래스 구조:**

```java
@Entity
@Table(name = "reservations")
public class ReservationEntity {

  @Id
  @Column(name = "reservation_id")
  private Long reservationId;  // 예약 서버에서 생성한 ID 사용

  @Column(name = "user_id")
  private Long userId;  // 사용자 ID (nullable)

  @Column(name = "place_id", nullable = false)
  private Long placeId;

  @Column(name = "room_id", nullable = false)
  private Long roomId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  private ReservationStatus status;

  @Column(name = "total_price", nullable = false)
  private Long totalPrice;

  @Column(name = "reservation_time_price", nullable = false)
  private Long reservationTimePrice;

  @Column(name = "deposit_price")
  private Long depositPrice;  // nullable

  @Column(name = "review_id")
  private Long reviewId;  // nullable

  @Column(name = "reservation_date", nullable = false)
  private LocalDate reservationDate;

  @Column(name = "reserver_name")
  private String reserverName;  // AES 암호화

  @Column(name = "reserver_phone")
  private String reserverPhone;  // AES 암호화

  @Column(name = "approved_at")
  private LocalDateTime approvedAt;  // nullable

  @Column(name = "approved_by")
  private Long approvedBy;  // 0: 시스템, 양수: 운영자 ID

  // JSON 저장 필드
  @Column(name = "product_data", columnDefinition = "jsonb")
  private String productData;

  @Column(name = "additional_info", columnDefinition = "jsonb")
  private String additionalInfo;

  @Column(name = "time_slots", columnDefinition = "jsonb")
  private String timeSlots;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // JPA 기본 생성자
  protected ReservationEntity() {}

  // Factory Method - 이벤트로부터 생성
  public static ReservationEntity createFromEvent(
      Long reservationId,
      Long placeId,
      Long roomId,
      Long userId,
      LocalDate reservationDate,
      List<String> startTimes,
      Long totalPrice,
      Long reservationTimePrice,
      Map<String, Object> productData,
      Map<String, Object> additionalInfo,
      String reserverName,
      String reserverPhone) {

    ReservationEntity entity = new ReservationEntity();
    entity.reservationId = reservationId;
    entity.userId = userId;
    entity.placeId = placeId;
    entity.roomId = roomId;
    entity.status = ReservationStatus.PENDING_PAYMENT;
    entity.reservationDate = reservationDate;
    entity.totalPrice = totalPrice;
    entity.reservationTimePrice = reservationTimePrice;
    entity.reserverName = reserverName;  // 암호화된 값
    entity.reserverPhone = reserverPhone;  // 암호화된 값
    entity.createdAt = LocalDateTime.now();
    entity.updatedAt = LocalDateTime.now();

    // JSON 직렬화 (구현 생략)
    entity.productData = JsonUtil.toJson(productData);
    entity.additionalInfo = JsonUtil.toJson(additionalInfo);
    entity.timeSlots = JsonUtil.toJson(startTimes);

    return entity;
  }

  // 도메인 로직: 예약자 정보 업데이트
  public void updateReserverInfo(String encryptedName, String encryptedPhone) {
    this.reserverName = encryptedName;
    this.reserverPhone = encryptedPhone;
    this.updatedAt = LocalDateTime.now();
  }

  // 도메인 로직: 예약 승인
  public void approve(Long approvedBy) {
    this.status = ReservationStatus.CONFIRMED;
    this.approvedAt = LocalDateTime.now();
    this.approvedBy = approvedBy;
    this.updatedAt = LocalDateTime.now();
  }

  // Getters
  // ...
}
```

**설계 포인트:**

1. **ID 전략:**
   - `@Id`만 사용 (`@GeneratedValue` 없음)
   - 예약 서비스에서 생성한 ID 재사용

2. **상태 관리:**
   - `ReservationStatus` enum: PENDING_PAYMENT, CONFIRMED, CANCELLED
   - 상태 전이는 도메인 메서드에서 처리

3. **개인정보 암호화:**
   - reserverName, reserverPhone은 AES 암호화하여 저장
   - 저장 전 암호화, 조회 후 복호화

4. **JSON 저장:**
   - productData, additionalInfo, timeSlots를 JSONB로 저장
   - 유연한 데이터 구조 지원

5. **Timestamp 관리:**
   - createdAt: 최초 생성 시각 (updatable = false)
   - approvedAt: 승인 시각
   - updatedAt: 마지막 수정 시각

---

### ReservationStatus (Enum)

**패키지:** `adapter.out.persistence.reservation`

```java
public enum ReservationStatus {
    PENDING_PAYMENT,   // 결제 대기
    CONFIRMED,         // 확정
    CANCELLED,         // 취소
    REFUNDED           // 환불 완료
}
```

---

## Value Object 설계

### PeriodType (Enum)

**패키지:** `domain.reservation.vo`

**목적:** 조회 기간 타입 정의 및 날짜 범위 계산

```java
public enum PeriodType {
    DAILY {
        @Override
        public DateRange calculateDateRange() {
            LocalDate today = LocalDate.now();
            return DateRange.of(today, today);
        }
    },
    WEEKLY {
        @Override
        public DateRange calculateDateRange() {
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(6);
            return DateRange.of(today, endDate);
        }
    },
    MONTHLY {
        @Override
        public DateRange calculateDateRange() {
            LocalDate today = LocalDate.now();
            LocalDate startOfMonth = today.withDayOfMonth(1);
            LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
            return DateRange.of(startOfMonth, endOfMonth);
        }
    };

    public abstract DateRange calculateDateRange();
}
```

**특징:**
- Strategy Pattern 적용
- 각 타입별로 날짜 범위 계산 로직 내장
- 불변성 보장

---

### DateRange

**패키지:** `domain.reservation.vo`

**목적:** 시작 날짜와 종료 날짜를 하나의 단위로 관리

```java
public class DateRange {

    private final LocalDate startDate;
    private final LocalDate endDate;

    private DateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ReservationException("날짜는 null일 수 없습니다");
        }
        if (startDate.isAfter(endDate)) {
            throw new ReservationException("시작 날짜는 종료 날짜보다 늦을 수 없습니다");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static DateRange of(LocalDate startDate, LocalDate endDate) {
        return new DateRange(startDate, endDate);
    }

    public boolean contains(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateRange dateRange = (DateRange) o;
        return Objects.equals(startDate, dateRange.startDate) &&
               Objects.equals(endDate, dateRange.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }
}
```

**특징:**
- 불변 객체 (final 필드)
- Factory Method로 검증 강제
- equals/hashCode 구현

---

### ReservationCursor

**패키지:** `domain.reservation.vo`

**목적:** 커서 기반 페이징의 커서 정보 관리

```java
public class ReservationCursor {

    private final LocalDate reservationDate;
    private final Long reservationId;

    private ReservationCursor(LocalDate reservationDate, Long reservationId) {
        if (reservationDate == null || reservationId == null) {
            throw new ReservationException("커서 정보는 null일 수 없습니다");
        }
        this.reservationDate = reservationDate;
        this.reservationId = reservationId;
    }

    public static ReservationCursor of(LocalDate reservationDate, Long reservationId) {
        return new ReservationCursor(reservationDate, reservationId);
    }

    public static ReservationCursor from(ReservationEntity entity) {
        return new ReservationCursor(
            entity.getReservationDate(),
            entity.getReservationId()
        );
    }

    // Base64 인코딩
    public String encode() {
        String json = String.format(
            "{\"reservationDate\":\"%s\",\"reservationId\":%d}",
            reservationDate.toString(),
            reservationId
        );
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    // Base64 디코딩
    public static ReservationCursor decode(String encodedCursor) {
        try {
            String json = new String(
                Base64.getDecoder().decode(encodedCursor),
                StandardCharsets.UTF_8
            );
            // JSON 파싱 (구현 생략)
            LocalDate date = parseDate(json);
            Long id = parseId(json);
            return ReservationCursor.of(date, id);
        } catch (Exception e) {
            throw new ReservationException("잘못된 커서 형식입니다", e);
        }
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public Long getReservationId() {
        return reservationId;
    }
}
```

**특징:**
- 복합 커서 (reservationDate, reservationId)
- Base64 인코딩/디코딩 지원
- Factory Method 패턴

---

### PriceInfo

**패키지:** `domain.reservation.vo`

**목적:** 가격 정보를 하나의 단위로 관리

```java
public class PriceInfo {

    private final Long totalPrice;
    private final Long reservationTimePrice;
    private final Long depositPrice;

    private PriceInfo(Long totalPrice, Long reservationTimePrice, Long depositPrice) {
        if (totalPrice == null || totalPrice < 0) {
            throw new ReservationException("총 가격은 0 이상이어야 합니다");
        }
        if (reservationTimePrice == null || reservationTimePrice < 0) {
            throw new ReservationException("예약 시간 가격은 0 이상이어야 합니다");
        }
        if (depositPrice != null && depositPrice < 0) {
            throw new ReservationException("보증금은 0 이상이어야 합니다");
        }
        this.totalPrice = totalPrice;
        this.reservationTimePrice = reservationTimePrice;
        this.depositPrice = depositPrice;
    }

    public static PriceInfo of(Long totalPrice, Long reservationTimePrice, Long depositPrice) {
        return new PriceInfo(totalPrice, reservationTimePrice, depositPrice);
    }

    public Long getTotalPrice() {
        return totalPrice;
    }

    public Long getReservationTimePrice() {
        return reservationTimePrice;
    }

    public Long getDepositPrice() {
        return depositPrice;
    }
}
```

**특징:**
- 불변 객체
- 가격 검증 로직 내장
- depositPrice는 nullable 허용

---

## 데이터베이스 스키마

### reservations 테이블

```sql
CREATE TABLE reservations (
  reservation_id         BIGINT        PRIMARY KEY,
  user_id                BIGINT,
  place_id               BIGINT        NOT NULL,
  room_id                BIGINT        NOT NULL,
  status                 VARCHAR(50)   NOT NULL,
  total_price            BIGINT        NOT NULL,
  reservation_time_price BIGINT        NOT NULL,
  deposit_price          BIGINT,
  review_id              BIGINT,
  reservation_date       DATE          NOT NULL,
  reserver_name          VARCHAR(255),  -- AES 암호화
  reserver_phone         VARCHAR(255),  -- AES 암호화
  approved_at            TIMESTAMP,
  approved_by            BIGINT,
  product_data           JSONB,
  additional_info        JSONB,
  time_slots             JSONB,
  created_at             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT chk_total_price CHECK (total_price >= 0),
  CONSTRAINT chk_reservation_time_price CHECK (reservation_time_price >= 0),
  CONSTRAINT chk_deposit_price CHECK (deposit_price IS NULL OR deposit_price >= 0)
);

-- 인덱스
CREATE INDEX idx_reservations_place_room
  ON reservations(place_id, room_id);

CREATE INDEX idx_reservations_date_status
  ON reservations(reservation_date, status);

-- 사용자별 예약 커서 기반 페이징 최적화 (V6)
CREATE INDEX idx_user_reservation_query
  ON reservations(user_id, reservation_date DESC, reservation_id DESC);

COMMENT ON INDEX idx_user_reservation_query IS
  '사용자별 예약 목록 조회 성능 최적화 인덱스 (커서 기반 페이징)';
```

**스키마 포인트:**

1. **단일 테이블 구조:**
   - Embedded/ElementCollection 없이 단순한 컬럼 구조
   - JSONB 활용으로 유연성 확보

2. **암호화 필드:**
   - reserver_name, reserver_phone은 암호화하여 저장
   - VARCHAR(255)로 암호화 문자열 저장

3. **복합 인덱스 (V6):**
   - user_id, reservation_date DESC, reservation_id DESC 순서
   - 커버링 인덱스로 테이블 스캔 최소화

4. **JSONB 활용:**
   - product_data: 상품 정보
   - additional_info: 추가 정보
   - time_slots: 예약 시간대

---

## 아키텍처 패턴

### Hexagonal Architecture (Port and Adapter)

```
┌─────────────────────────────────────────────────┐
│                   Domain Layer                  │
│  ┌───────────────────────────────────────────┐  │
│  │  ReservationEntity, Value Objects         │  │
│  │  (PeriodType, DateRange, Cursor, Price)   │  │
│  └───────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
         ▲                            ▲
         │                            │
┌────────┴──────┐            ┌───────┴────────┐
│  Application  │            │  Application   │
│  (Services)   │            │  (Ports)       │
└────────┬──────┘            └───────┬────────┘
         │                            │
         ▼                            ▼
┌─────────────────────────────────────────────────┐
│               Adapter Layer                     │
│  ┌──────────────┐        ┌──────────────────┐  │
│  │ Web (REST)   │        │  Persistence     │  │
│  │ Controllers  │        │  (JPA)           │  │
│  └──────────────┘        └──────────────────┘  │
│  ┌──────────────┐        ┌──────────────────┐  │
│  │ Messaging    │        │  External API    │  │
│  │ (Kafka)      │        │  (Blacklist)     │  │
│  └──────────────┘        └──────────────────┘  │
└─────────────────────────────────────────────────┘
```

**레이어별 책임:**

1. **Domain Layer:**
   - Entity, Value Object 정의
   - 비즈니스 규칙 구현
   - 외부 의존성 없음

2. **Application Layer:**
   - Use Case 구현 (Service)
   - Port 인터페이스 정의
   - 트랜잭션 관리

3. **Adapter Layer:**
   - Port 구현체
   - 외부 시스템 연동
   - 데이터 변환

---

## 참고 자료

- [ARCHITECTURE_DECISION.md](ARCHITECTURE_DECISION.md) - 아키텍처 결정사항
- [README.md](../../README.md#데이터베이스-스키마) - 데이터베이스 스키마 개요
- [docs/features/reservation/README.md](../features/reservation/README.md) - 예약 기능 상세

---

Last Updated: 2025-01-17