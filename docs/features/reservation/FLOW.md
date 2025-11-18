# 예약 프로세스 흐름

예약 생성부터 확정까지의 전체 프로세스를 설명합니다.

---

## 전체 프로세스

### 1. 자동 거절 프로세스 (블랙리스트)

```
┌────────────┐     ┌──────────────┐     ┌─────────────┐     ┌───────────┐
│  사용자     │     │  예약 서비스  │     │  결제 서비스 │     │ 이 서비스  │
└─────┬──────┘     └──────┬───────┘     └──────┬──────┘     └─────┬─────┘
      │                   │                     │                  │
      │ 1. 예약 요청       │                     │                  │
      │──────────────────>│                     │                  │
      │                   │                     │                  │
      │                   │ 2. 예약 생성        │                  │
      │                   │    (슬롯 점유)      │                  │
      │                   │                     │                  │
      │                   │ 3. ReservationCreatedEvent             │
      │                   │──────────────────────────────────────>│
      │                   │                                        │
      │                   │                                4. 저장  │
      │                   │                                (PENDING)│
      │                   │                                        │
      │ 5. 결제 요청       │                                        │
      │──────────────────────────────────────>│                  │
      │                                        │                  │
      │                                        │ 6. 결제 완료      │
      │                                        │                  │
      │                                        │ 7. PaymentCompletedEvent
      │                                        │─────────────────>│
      │                                        │                  │
      │                                        │          8. 블랙리스트 체크
      │                                        │             (placeId + bookerId)
      │                                        │                  │
      │                                        │          [블랙리스트인 경우]
      │                                        │          9. 상태 전환
      │                                        │             PAID → REJECTED
      │                                        │                  │
      │                                        │<─────────10. ReservationRejectedEvent
      │                                        │                  │
      │                                        │ 11. 환불 처리     │
      │                                        │                  │
      │<─────────────────12. 거절 통지         │                  │
      │                                        │                  │
      │                                        │          [정상인 경우]
      │                                        │          9. 상태 전환
      │                                        │             PAID → CONFIRMED
      │                                        │                  │
      │<─────────────────13. 확정 통지         │                  │
```

### 2. 수동 거절 프로세스 (운영자)

```
┌────────────┐     ┌──────────────┐     ┌─────────────┐     ┌───────────┐
│ 공간 운영자 │     │  예약 서비스  │     │  결제 서비스 │     │ 이 서비스  │
└─────┬──────┘     └──────┬───────┘     └──────┬──────┘     └─────┬─────┘
      │                   │                     │                  │
      │  (예약이 이미 CONFIRMED 상태)            │                  │
      │                   │                     │                  │
      │ 1. 거절 요청       │                     │                  │
      │   (사유 포함)      │                     │                  │
      │──────────────────>│                     │                  │
      │                   │                     │                  │
      │                   │ 2. 권한 검증        │                  │
      │                   │   (운영자 확인)      │                  │
      │                   │                     │                  │
      │                   │ 3. ReservationRejectedByOwnerEvent     │
      │                   │──────────────────────────────────────>│
      │                   │                                        │
      │                   │                                4. 상태 전환
      │                   │                          CONFIRMED → REJECTED
      │                   │                                        │
      │                   │<─────────5. ReservationRejectedEvent───│
      │                   │                                        │
      │                   │ 6. 슬롯 해제          │                  │
      │                   │   (Hard Lock → 해제)  │                  │
      │                   │                     │                  │
      │                   │─────────────────────>│                  │
      │                   │ 7. 환불 요청          │                  │
      │                   │                     │                  │
      │                   │                     │ 8. 환불 처리      │
      │                   │                     │                  │
      │<─────────────────9. 거절 통지         │                  │
      │                   │                     │                  │
      │<──────────────────────────────────────10. 환불 완료 통지   │
```

---

## 상세 단계

### Step 1-3: 예약 생성

**예약 서비스 책임:**
- 슬롯 가용성 확인
- 슬롯 점유 (Soft Lock)
- 예약 ID 생성
- 이벤트 발행

**이벤트:**
```json
{
  "eventType": "ReservationCreated",
  "reservationId": 123456,
  "bookerId": 1001,
  "placeId": 100,
  "roomId": 10,
  "reservationDate": "2025-01-15",
  "startTimes": ["11:00", "12:00", "13:00"],
  "products": [
    {
      "productId": 1,
      "productName": "빔프로젝터",
      "quantity": 1,
      "unitPrice": 30000
    }
  ],
  "totalPrice": 80000
}
```

---

### Step 4: 예약 저장 (PENDING)

**이 서비스 처리:**

```java
@KafkaListener(topics = "reservation.created")
public void handleReservationCreated(ReservationCreatedEvent event) {
  // 1. 중복 체크
  if (repository.existsById(event.getReservationId())) {
    log.warn("Duplicate event: {}", event.getReservationId());
    return;
  }

  // 2. 쿠폰 검증 (동기 호출)
  List<CouponSnapshot> coupons = validateCoupons(event.getCouponIds());

  // 3. ReservationEntity 생성
  ReservationEntity reservation = ReservationEntity.create(
      event.getReservationId(),
      event.getBookerId(),
      event.getPlaceId(),
      event.getRoomId(),
      ReservationTimeInfo.of(event.getReservationDate(), event.getStartTimes()),
      toProductSnapshots(event.getProducts()),
      coupons,
      event.getTotalPrice()
  );

  // 4. 저장 (상태: PENDING)
  repository.save(reservation);
}
```

---

### Step 5-7: 결제 완료

**결제 서비스 책임:**
- PG사 결제 처리
- 결제 완료 이벤트 발행

**이벤트:**
```json
{
  "eventType": "PaymentCompleted",
  "reservationId": 123456,
  "bookerId": 1001,
  "placeId": 100,
  "paymentId": 789,
  "paidAmount": 80000
}
```

---

### Step 8-10: 블랙리스트 체크 및 자동 거절

**이 서비스 처리:**

```java
@KafkaListener(topics = "payment.completed")
public void handlePaymentCompleted(PaymentCompletedEvent event) {
  Long reservationId = event.getReservationId();
  Long bookerId = event.getBookerId();
  Long placeId = event.getPlaceId();

  // 1. 예약 조회
  ReservationEntity reservation = repository.findById(reservationId)
      .orElseThrow(() -> new ReservationNotFoundException(reservationId));

  // 2. 상태 전환: PENDING → PAID
  reservation.updateToPaid();
  repository.save(reservation);

  // 3. 블랙리스트 체크
  boolean isBlackListed = blackListService.isBlackListed(placeId, bookerId);

  if (isBlackListed) {
    // 4-1. 블랙리스트: 자동 거절
    reservation.reject("블랙리스트 사용자");
    repository.save(reservation);

    // 5. 거절 이벤트 발행 (환불 트리거)
    ReservationRejectedEvent rejectedEvent = ReservationRejectedEvent.builder()
        .reservationId(reservationId)
        .rejectedAt(LocalDateTime.now())
        .reason("블랙리스트 사용자")
        .build();
    eventProducer.publishReservationRejected(rejectedEvent);

    log.info("예약 자동 거절: reservationId={}, bookerId={}", reservationId, bookerId);
  } else {
    // 4-2. 정상: 예약 확정
    reservation.confirm();
    repository.save(reservation);

    log.info("예약 확정: reservationId={}", reservationId);
  }
}
```

---

### Step 11-13: 후속 처리

**거절된 경우:**
1. 결제 서비스: 환불 처리
2. 예약 서비스: 슬롯 해제 (Soft Lock → 해제)
3. 알림 서비스: 사용자에게 거절 통지

**확정된 경우:**
1. 예약 서비스: 슬롯 확정 (Soft Lock → Hard Lock)
2. 알림 서비스: 사용자에게 확정 통지

---

### 운영자 수동 거절 처리

**처리 시점:** 예약이 CONFIRMED 상태일 때 운영자가 거절 요청

**이 서비스 처리:**

```java
@KafkaListener(topics = "reservation.rejected.by.owner")
public void handleReservationRejectedByOwner(ReservationRejectedByOwnerEvent event) {
  Long reservationId = event.getReservationId();
  String rejectionReason = event.getReason();

  // 1. 예약 조회
  ReservationEntity reservation = repository.findById(reservationId)
      .orElseThrow(() -> new ReservationNotFoundException(reservationId));

  // 2. 상태 검증: CONFIRMED 상태만 거절 가능
  if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
    log.warn("CONFIRMED 상태가 아닌 예약 거절 시도: reservationId={}, status={}",
        reservationId, reservation.getStatus());
    throw new InvalidReservationStateException(
        "CONFIRMED 상태만 거절 가능합니다: " + reservation.getStatus()
    );
  }

  // 3. 상태 전환: CONFIRMED → REJECTED
  reservation.rejectByOwner(rejectionReason, event.getRejectedBy());
  repository.save(reservation);

  // 4. 거절 이벤트 발행 (환불 트리거)
  ReservationRejectedEvent rejectedEvent = ReservationRejectedEvent.builder()
      .reservationId(reservationId)
      .rejectedAt(LocalDateTime.now())
      .reason(rejectionReason)
      .rejectedBy(event.getRejectedBy())
      .build();
  eventProducer.publishReservationRejected(rejectedEvent);

  log.info("예약 수동 거절: reservationId={}, rejectedBy={}, reason={}",
      reservationId, event.getRejectedBy(), rejectionReason);
}
```

**Entity 메서드:**

```java
public void rejectByOwner(String reason, Long rejectedBy) {
  if (this.status != ReservationStatus.CONFIRMED) {
    throw new IllegalStateException(
        "CONFIRMED 상태만 거절 가능합니다: " + this.status
    );
  }
  this.status = ReservationStatus.REJECTED;
  this.rejectedAt = LocalDateTime.now();
  this.rejectedReason = reason;
  this.rejectedBy = rejectedBy;
  this.updatedAt = LocalDateTime.now();
}
```

**환불 처리:**
- 결제 서비스가 `ReservationRejectedEvent`를 수신하여 자동 환불
- 환불 금액: 원결제 금액 전액
- 환불 수수료: 정책에 따라 차감 (예: 예약 시간 24시간 이내 거절 시 10% 수수료)

---

## 예외 처리

### 쿠폰 서비스 장애

```java
private List<CouponSnapshot> validateCoupons(List<Long> couponIds) {
  List<CouponSnapshot> coupons = new ArrayList<>();

  for (Long couponId : couponIds) {
    try {
      CouponValidationResult result = couponService.validate(couponId);
      if (result.isValid()) {
        coupons.add(CouponSnapshot.from(result));
      }
    } catch (Exception e) {
      log.warn("쿠폰 검증 실패, 쿠폰 없이 진행: couponId={}", couponId, e);
      // 쿠폰 없이 진행
    }
  }

  return coupons;
}
```

**결과:** 쿠폰 없이 예약 저장 (사용자 경험 저하는 있지만 예약은 진행)

---

### 이벤트 순서 뒤바뀜

**문제:**
```
PaymentCompletedEvent 먼저 수신
  ↓
ReservationCreatedEvent 나중 수신
```

**현재 대응:**
- PaymentCompleted 처리 시 예약 없으면 예외 발생
- ReservationCreated 이벤트 재처리 (Retry)

**향후 개선:**
- 이벤트 순서 검증 로직
- 이벤트 버퍼링 (순서대로 처리)

---

### 이벤트 중복 수신

**대응:**
```java
// 중복 체크: 이미 존재하면 무시
if (repository.existsById(event.getReservationId())) {
  log.warn("Duplicate event: {}", event.getReservationId());
  return;
}
```

---

## 상태 전이 규칙

### 허용되는 전이

| 현재 상태 | 다음 상태 | 트리거 |
|----------|----------|--------|
| PENDING | PAID | PaymentCompletedEvent |
| PAID | CONFIRMED | 블랙리스트 통과 |
| PAID | REJECTED | 블랙리스트 감지 |
| CONFIRMED | REJECTED | 운영자 수동 거절 |
| CONFIRMED | REFUNDED | ReservationRefundEvent |

### 금지되는 전이

- PENDING → CONFIRMED (결제 필수)
- REJECTED → CONFIRMED (거절 후 확정 불가)
- REFUNDED → CONFIRMED (환불 후 확정 불가)

**검증 코드:**
```java
public void confirm() {
  if (this.status != ReservationStatus.PAID) {
    throw new IllegalStateException(
        "PAID 상태만 CONFIRMED로 전환 가능합니다: " + this.status
    );
  }
  this.status = ReservationStatus.CONFIRMED;
  this.acceptedAt = LocalDateTime.now();
  this.updatedAt = LocalDateTime.now();
}
```

---

## 성능 고려사항

### 블랙리스트 조회 최적화

```sql
-- 복합키 인덱스 (자동 생성)
PRIMARY KEY (place_id, blacklist_user_id)

-- 만료 체크 인덱스
CREATE INDEX idx_blacklist_expired_at
  ON blacklist(expired_at)
  WHERE expired_at IS NOT NULL;
```

**쿼리:**
```java
public boolean isBlackListed(Long placeId, Long userId) {
  return repository.findById(BlackListKey.of(placeId, userId))
      .map(blackList -> !blackList.isExpired())
      .orElse(false);
}
```

**시간 복잡도:** O(1) - 복합키 인덱스

---

## 참고 자료

- [README.md](README.md) - 예약 관리 기능 개요
- [PROJECT_REQUIREMENTS.md](../../requirements/PROJECT_REQUIREMENTS.md)

---

Last Updated: 2025-01-12