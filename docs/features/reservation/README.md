# 예약 관리 기능

예약 생성, 조회, 승인 관리 기능을 설명합니다.

---

## 기능 개요

### 목적

예약 서비스로부터 받은 예약 정보를 저장하고, 다양한 조건으로 조회하며, 예약자 정보를 관리합니다.

### 핵심 기능

- **예약 생성**: 예약 ID 기반으로 예약자 정보 업데이트 (이름, 전화번호)
- **예약 조회**: 일/주/월 단위 또는 사용자별 예약 목록 조회
- **커서 기반 페이징**: 사용자별 예약 목록의 효율적인 페이징 처리
- **개인정보 보호**: 예약자 이름/전화번호 AES 암호화 저장
- **승인 관리**: 예약 승인 정보 추적 (승인 시각, 승인자)

---

## 도메인 모델

### Reservation Entity

```java
ReservationEntity
├─ reservationId (PK, BIGINT)           // 예약 서버 ID
├─ userId (BIGINT)                       // 사용자 ID
├─ placeId (BIGINT)                      // 장소 ID
├─ roomId (BIGINT)                       // 방 ID
├─ status (VARCHAR)                      // PENDING | PAID | CONFIRMED | REJECTED | REFUNDED
├─ totalPrice (BIGINT)                   // 총 가격
├─ reservationTimePrice (BIGINT)         // 예약 시간 가격
├─ depositPrice (BIGINT, nullable)       // 보증금
├─ reviewId (BIGINT, nullable)           // 리뷰 ID
├─ reservationDate (DATE)                // 예약 날짜
├─ reserverName (VARCHAR, encrypted)     // 예약자 이름 (AES 암호화)
├─ reserverPhone (VARCHAR, encrypted)    // 예약자 전화번호 (AES 암호화)
├─ approvedAt (TIMESTAMP, nullable)      // 승인 시각
├─ approvedBy (BIGINT, nullable)         // 승인자 (0: 시스템, 양수: 운영자 ID)
├─ createdAt (TIMESTAMP)
└─ updatedAt (TIMESTAMP)
```

### Value Objects

```java
PeriodType (enum)
├─ DAILY: 당일 기준 조회
├─ WEEKLY: 7일 기준 조회
└─ MONTHLY: 1개월 기준 조회

DateRange
├─ startDate (LocalDate)
└─ endDate (LocalDate)

ReservationCursor
├─ reservationDate (LocalDate)
└─ reservationId (Long)

PriceInfo
├─ totalPrice (Long)
├─ reservationTimePrice (Long)
└─ depositPrice (Long, nullable)
```

### 상태 전이

```
PENDING_PAYMENT ──► CONFIRMED
                    │
                    └──► CANCELLED
```

**전이 규칙:**
- PENDING_PAYMENT → CONFIRMED: 결제 완료 이벤트 수신 후 승인
- CONFIRMED → CANCELLED: 취소 요청 (향후 구현 예정)

---

## API 명세

### 1. 예약 생성 (예약자 정보 업데이트)

```http
POST /api/v1/reservations
Content-Type: application/json
```

**요청 Body:**
```json
{
  "reservationId": 123456,
  "reserverName": "홍길동",
  "reserverPhone": "010-1234-5678"
}
```

**응답:**
```json
{
  "reservationId": 123456,
  "message": "예약자 정보가 업데이트되었습니다."
}
```

**비즈니스 로직:**
- 예약자 이름과 전화번호를 AES 암호화하여 저장
- 이미 존재하는 예약 ID에 대해 예약자 정보 업데이트
- 존재하지 않는 예약 ID인 경우 404 예외 발생

---

### 2. 예약 상세 조회

```http
GET /api/v1/reservations/{id}
```

**응답:**
```json
{
  "reservationId": 123456,
  "userId": 1001,
  "placeId": 100,
  "roomId": 10,
  "status": "CONFIRMED",
  "totalPrice": 72000,
  "reservationTimePrice": 60000,
  "depositPrice": 10000,
  "reservationDate": "2025-01-15",
  "reserverName": "홍길동",
  "reserverPhone": "010-1234-5678",
  "approvedAt": "2025-01-15T10:30:00",
  "approvedBy": 0,
  "createdAt": "2025-01-15T10:00:00",
  "updatedAt": "2025-01-15T10:30:00"
}
```

**비즈니스 로직:**
- 예약자 이름/전화번호는 복호화하여 반환
- approvedBy가 0이면 시스템 자동 승인, 양수면 운영자 ID

---

### 3. 일간 예약 목록 조회

```http
GET /api/v1/reservations/daily?date=2025-01-15
```

**쿼리 파라미터:**
- date: 조회 날짜 (yyyy-MM-dd 형식, 필수)

**응답:**
```json
{
  "places": [
    {
      "placeId": 100,
      "placeName": "강남점",
      "rooms": [
        {
          "roomId": 10,
          "roomName": "회의실 A",
          "reservations": [
            {
              "reservationId": 123456,
              "userId": 1001,
              "status": "CONFIRMED",
              "reservationDate": "2025-01-15",
              "totalPrice": 72000
            }
          ]
        }
      ]
    }
  ]
}
```

---

### 4. 주간 예약 목록 조회

```http
GET /api/v1/reservations/weekly?startDate=2025-01-15
```

**쿼리 파라미터:**
- startDate: 주 시작 날짜 (yyyy-MM-dd 형식, 필수)

**응답 형식:** 일간 조회와 동일 (7일치 데이터)

---

### 5. 월간 예약 목록 조회

```http
GET /api/v1/reservations/monthly?yearMonth=2025-01
```

**쿼리 파라미터:**
- yearMonth: 조회 연월 (yyyy-MM 형식, 필수)

**응답 형식:** 일간 조회와 동일 (해당 월 전체 데이터)

---

### 6. 사용자별 예약 목록 (커서 기반 페이징)

```http
GET /api/v1/reservations/users/{userId}?period=DAILY&cursor=xxx&size=20&statuses=CONFIRMED,PENDING_PAYMENT
```

**경로 파라미터:**
- userId: 사용자 ID (필수)

**쿼리 파라미터:**
- period: 기간 타입 (DAILY, WEEKLY, MONTHLY, 필수)
- cursor: Base64 인코딩된 커서 (선택, 첫 페이지는 생략)
- size: 페이지 크기 (선택, 기본 20, 최대 100)
- statuses: 상태 필터 (선택, 다중 선택 가능)

**응답:**
```json
{
  "items": [
    {
      "reservationId": 123456,
      "placeId": 100,
      "roomId": 10,
      "status": "CONFIRMED",
      "totalPrice": 72000,
      "reservationDate": "2025-01-15",
      "reserverName": "홍길동",
      "reserverPhone": "010-1234-5678"
    }
  ],
  "nextCursor": "eyJyZXNlcnZhdGlvbkRhdGUiOiIyMDI1LTAxLTE1IiwicmVzZXJ2YXRpb25JZCI6MTIzNDU2fQ==",
  "hasNext": true,
  "size": 20
}
```

**커서 구조:**
- Base64 인코딩된 JSON: `{"reservationDate":"2025-01-15","reservationId":123456}`
- reservationDate DESC, reservationId DESC 순으로 정렬
- 복합 커서로 동일 날짜 내 중복 없는 페이징 보장

**성능 최적화:**
- 복합 인덱스 활용: `idx_user_reservation_query (user_id, reservation_date DESC, reservation_id DESC)`
- 커버링 인덱스로 테이블 풀 스캔 방지

---

## 이벤트 처리

### 수신 이벤트

#### ReservationPendingPaymentEvent

**Topic:** `reservation-pending-payment`

**Event Type:** `ReservationPendingPayment`

**Payload:**
```json
{
  "topic": "reservation-pending-payment",
  "eventType": "ReservationPendingPayment",
  "reservationId": 123456,
  "placeId": 100,
  "roomId": 10,
  "reservationDate": "2025-01-15",
  "productPriceDetails": [
    {
      "productId": 1,
      "productName": "빔프로젝터",
      "quantity": 1,
      "unitPrice": 30000,
      "totalPrice": 30000
    }
  ],
  "reservationTimePriceDetail": {
    "startTimes": ["11:00", "12:00", "13:00"],
    "totalReservationTimePrice": 60000
  },
  "totalPrice": 90000,
  "occurredAt": "2025-01-15T10:00:00",
  "reserverName": "홍길동",
  "reserverPhone": "01012345678"
}
```

**처리 로직:**
1. 멱등성 검사: 이미 처리된 예약인지 확인 (reservationId 기준)
2. 데이터 변환 및 검증
   - reservationDate: String → LocalDate
   - totalPrice: BigDecimal → Long
   - reservationTimePrice 계산
3. 상품 정보 JSON 직렬화하여 productData에 저장
4. ReservationEntity 생성 (PENDING_PAYMENT 상태)
5. 예약자 이름/전화번호 AES 암호화하여 저장
6. 데이터베이스에 저장

**비고:**
- userId는 이벤트에 포함되지 않음 (나중에 업데이트 필요)
- 중복 이벤트 수신 시 멱등성 보장 (이미 존재하면 스킵)

---

#### ReservationConfirmedEvent

**Topic:** `reservation-confirmed`

**Event Type:** `ReservationConfirmed`

**Payload:**
```json
{
  "topic": "reservation-confirmed",
  "eventType": "ReservationConfirmed",
  "reservationId": 123456,
  "occurredAt": "2025-01-15T11:00:00"
}
```

**처리 로직:**
1. ReservationEntity 조회
2. 상태 변경 (PENDING_PAYMENT → CONFIRMED)
3. approvedAt, approvedBy 업데이트 (시스템 자동 승인: approvedBy = 0)

**비고:**
- 블랙리스트 자동 거절 기능은 제거됨
- 블랙리스트 여부는 운영자 조회 시 응답에 포함되어 운영자가 판단

---

## 비즈니스 로직

### 1. 예약자 정보 암호화

```java
public class ReservationCommandService {

  private final AesEncryptor aesEncryptor;

  public CreateReservationResponse createReservation(CreateReservationRequest request) {
    // 예약자 정보 AES 암호화
    String encryptedName = aesEncryptor.encrypt(request.getReserverName());
    String encryptedPhone = aesEncryptor.encrypt(request.getReserverPhone());

    // 예약 업데이트
    ReservationEntity reservation = loadReservationPort
      .findByReservationId(request.getReservationId())
      .orElseThrow(() -> new ReservationNotFoundException(...));

    reservation.updateReserverInfo(encryptedName, encryptedPhone);
    saveReservationPort.save(reservation);

    return new CreateReservationResponse(reservation.getReservationId(), ...);
  }
}
```

**암호화 알고리즘:**
- AES-256-CBC 모드
- PKCS5Padding
- 환경 변수로부터 암호화 키 로드

---

### 2. 커서 기반 페이징

```java
public class ReservationQueryService {

  public UserReservationPageResponse getUserReservationsWithPaging(
      Long userId, ReservationQueryRequest request) {

    // 1. PeriodType으로 날짜 범위 계산
    DateRange dateRange = request.getPeriod().calculateDateRange();

    // 2. 커서 디코딩 (Base64 → ReservationCursor)
    ReservationCursor cursor = request.getCursor() != null
      ? ReservationCursor.decode(request.getCursor())
      : null;

    // 3. 데이터베이스 조회 (복합 인덱스 활용)
    List<ReservationEntity> reservations = repository
      .findByUserIdAndDateRangeWithCursor(
        userId, dateRange, cursor, request.getSize() + 1, request.getStatuses()
      );

    // 4. hasNext 판단 및 nextCursor 생성
    boolean hasNext = reservations.size() > request.getSize();
    if (hasNext) {
      reservations = reservations.subList(0, request.getSize());
    }

    String nextCursor = hasNext
      ? ReservationCursor.from(reservations.get(reservations.size() - 1)).encode()
      : null;

    return UserReservationPageResponse.of(reservations, nextCursor, hasNext, ...);
  }
}
```

**쿼리 최적화:**
- 복합 인덱스 활용: `(user_id, reservation_date DESC, reservation_id DESC)`
- 커서 기반으로 OFFSET 없이 효율적인 페이징
- 커버링 인덱스로 테이블 접근 최소화

---

### 3. 멱등성 보장

```java
public class ReservationPendingPaymentEventHandler {

  public void handle(ReservationPendingPaymentEvent event) {
    // 멱등성 검사
    if (loadReservationPort.existsByReservationId(event.getReservationId())) {
      logger.info("Reservation already exists, skipping");
      return;
    }

    // 예약 생성 로직
    ReservationEntity reservation = ReservationEntity.createFromEvent(...);
    saveReservationPort.save(reservation);
  }
}
```

**멱등성 전략:**
- reservationId 기준으로 중복 체크
- 이미 존재하면 처리 스킵
- Kafka 재전송 시 안전성 보장

---

## 제약사항

1. **예약 ID 생성 권한 없음**
   - 예약 서비스에서 생성한 ID를 이벤트로 받아 사용
   - 자체 ID 생성 불가

2. **이벤트 기반 데이터 동기화**
   - 예약 생성은 ReservationPendingPaymentEvent 수신 후 처리
   - 예약자 정보(userId)는 이벤트에 포함되지 않아 별도 업데이트 필요

3. **블랙리스트 표시**
   - 블랙리스트 여부는 운영자 조회 API 응답에 포함
   - 자동 거절하지 않고 운영자가 판단하도록 정보만 제공

4. **암호화 키 관리**
   - AES 암호화 키는 환경 변수로 관리
   - 키 유출 시 개인정보 노출 위험

---

## 참고 자료

- [FLOW.md](FLOW.md) - 예약 프로세스 흐름도
- [DOMAIN_MODEL_DESIGN.md](../../architecture/DOMAIN_MODEL_DESIGN.md)

---

Last Updated: 2025-01-17