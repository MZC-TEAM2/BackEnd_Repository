# 블랙리스트 기능

공간 운영자의 블랙리스트 관리 및 자동 거절 기능을 설명합니다.

---

## 기능 개요

### 목적

공간 운영자가 등록한 블랙리스트 사용자의 예약을 결제 완료 후 자동으로 거절하여, 노쇼 등 문제 사용자를 차단합니다.

### 핵심 가치

- **자동화**: 수동 거절 불필요
- **즉시성**: 결제 완료 후 즉시 체크
- **유연성**: 영구 차단 또는 기간 제한 선택

---

## 도메인 모델

### BlackList Entity

```java
BlackListEntity
├─ BlackListKey (복합키)
│  ├─ placeId
│  └─ blackListUserId
├─ reason (사유, 최소 5자)
├─ registeredBy (등록자 ID, 공간 운영자)
├─ createdAt (등록 일시)
└─ expiredAt (만료 일시, nullable)
```

### 복합키 설계

**이유:**
- 특정 공간에서만 차단 (다른 공간은 이용 가능)
- placeId + userId 조합이 자연스러운 키

**장점:**
- 조회 성능 O(1)
- 중복 등록 자동 방지

---

## API 명세

### 1. 블랙리스트 등록

```http
POST /api/blacklist
Authorization: Bearer {token}
```

**요청 Body:**
```json
{
  "placeId": 100,
  "userId": 1001,
  "reason": "노쇼 3회 누적으로 인한 영구 차단",
  "expiredAt": null
}
```

**응답:**
```json
{
  "placeId": 100,
  "userId": 1001,
  "reason": "노쇼 3회 누적으로 인한 영구 차단",
  "registeredBy": 5001,
  "createdAt": "2025-01-10T14:00:00",
  "expiredAt": null,
  "isPermanent": true
}
```

**제약사항:**
- reason 최소 5자 이상
- registeredBy는 Gateway에서 전달된 운영자 ID
- expiredAt null = 영구 차단

---

### 2. 블랙리스트 조회

```http
GET /api/blacklist/places/{placeId}?page=0&size=20
```

**응답:**
```json
{
  "content": [
    {
      "userId": 1001,
      "reason": "노쇼 3회 누적",
      "registeredBy": 5001,
      "createdAt": "2025-01-10T14:00:00",
      "expiredAt": null,
      "isPermanent": true,
      "isExpired": false
    },
    {
      "userId": 1002,
      "reason": "예약 취소 반복",
      "registeredBy": 5001,
      "createdAt": "2025-01-05T10:00:00",
      "expiredAt": "2025-02-05T10:00:00",
      "isPermanent": false,
      "isExpired": false
    }
  ],
  "pageable": {...},
  "totalElements": 15
}
```

---

### 3. 블랙리스트 해제

```http
DELETE /api/blacklist/places/{placeId}/users/{userId}
Authorization: Bearer {token}
```

**응답:**
```json
{
  "message": "블랙리스트에서 해제되었습니다"
}
```

---

## 비즈니스 로직

### 만료 전략

**null = 영구 차단:**
```java
public boolean isExpired() {
  if (expiredAt == null) {
    return false;  // 영구 차단
  }
  return LocalDateTime.now().isAfter(expiredAt);
}

public boolean isPermanent() {
  return expiredAt == null;
}
```

**사용 예시:**
```java
// 영구 차단
BlackListEntity permanent = BlackListEntity.create(
    placeId, userId, "노쇼 3회", ownerId, null
);

// 30일 제한
BlackListEntity temporary = BlackListEntity.create(
    placeId, userId, "예약 취소 반복", ownerId,
    LocalDateTime.now().plusDays(30)
);
```

---

### 자동 거절 로직

**처리 시점:** 결제 완료 이벤트 수신 후

**처리 흐름:**
```java
@KafkaListener(topics = "payment.completed")
public void handlePaymentCompleted(PaymentCompletedEvent event) {
  // 1. 예약 상태 PENDING → PAID
  reservation.updateToPaid();

  // 2. 블랙리스트 체크
  BlackListKey key = BlackListKey.of(event.getPlaceId(), event.getBookerId());
  Optional<BlackListEntity> blackList = blackListRepository.findById(key);

  if (blackList.isPresent() && !blackList.get().isExpired()) {
    // 3. 블랙리스트: 자동 거절
    reservation.reject("블랙리스트 사용자");

    // 4. 거절 이벤트 발행 (환불 트리거)
    eventProducer.publishReservationRejected(
        reservationId, "블랙리스트 사용자"
    );

    log.info("블랙리스트 자동 거절: placeId={}, userId={}",
        event.getPlaceId(), event.getBookerId());
  } else {
    // 5. 정상: 예약 확정
    reservation.confirm();
  }
}
```

---

## 시스템 자동 등록 (향후 기능)

### 노쇼 패턴 감지

**조건:**
- 노쇼 3회 이상
- 최근 6개월 내

**처리:**
```java
public void checkNoShowPattern(Long userId) {
  List<ReservationEntity> reservations = repository
      .findByBookerIdAndStatusAndCreatedAtAfter(
          userId,
          ReservationStatus.REJECTED,
          LocalDateTime.now().minusMonths(6)
      );

  if (reservations.size() >= 3) {
    // 자동 블랙리스트 등록
    for (Long placeId : extractPlaceIds(reservations)) {
      BlackListEntity blackList = BlackListEntity.create(
          placeId,
          userId,
          "노쇼 3회 이상 감지 (시스템 자동 등록)",
          SYSTEM_USER_ID,
          LocalDateTime.now().plusMonths(6)  // 6개월 제한
      );
      blackListRepository.save(blackList);
    }
  }
}
```

---

### 예약 취소 패턴 감지

**조건:**
- 예약 후 1시간 이내 취소 5회 이상
- 최근 1개월 내

**처리:**
```java
public void checkCancellationPattern(Long userId) {
  List<ReservationEntity> cancellations = repository
      .findRecentCancellationsWithinOneHour(userId, 1);

  if (cancellations.size() >= 5) {
    // 경고 또는 30일 제한 블랙리스트
    // 구현 TBD
  }
}
```

---

## 제약사항

### 등록 권한

**공간 운영자만 가능:**
- Gateway에서 인증/인가 처리
- 이 서비스는 전달된 registeredBy 신뢰
- 운영자 검증은 Gateway 책임

### 충돌 처리

**이미 등록된 경우:**
```java
public void register(RegisterBlackListCommand command) {
  BlackListKey key = BlackListKey.of(command.getPlaceId(), command.getUserId());

  if (repository.existsById(key)) {
    throw new BlackListAlreadyExistsException(
        "이미 블랙리스트에 등록되어 있습니다"
    );
  }

  // 등록
  BlackListEntity blackList = BlackListEntity.create(...);
  repository.save(blackList);
}
```

---

## 데이터베이스 설계

### 테이블 구조

```sql
CREATE TABLE blacklist (
  place_id          BIGINT       NOT NULL,
  blacklist_user_id BIGINT       NOT NULL,
  reason            VARCHAR(500) NOT NULL,
  registered_by     BIGINT       NOT NULL,
  created_at        TIMESTAMP    NOT NULL,
  expired_at        TIMESTAMP,

  PRIMARY KEY (place_id, blacklist_user_id),

  CONSTRAINT chk_reason_length CHECK (LENGTH(reason) >= 5)
);

-- 만료 체크 최적화
CREATE INDEX idx_blacklist_expired_at
  ON blacklist(expired_at)
  WHERE expired_at IS NOT NULL;
```

---

## 성능 최적화

### 조회 성능

**복합키 인덱스 (자동):**
- PRIMARY KEY (place_id, blacklist_user_id)
- O(1) 조회 성능

**만료 체크 인덱스:**
- Partial Index (expired_at IS NOT NULL)
- 영구 차단 (null)은 인덱스 불필요

### 쿼리 예시

```java
// 블랙리스트 여부 확인 (O(1))
public boolean isBlackListed(Long placeId, Long userId) {
  return repository.findById(BlackListKey.of(placeId, userId))
      .map(blackList -> !blackList.isExpired())
      .orElse(false);
}

// 만료된 블랙리스트 정리 (배치 작업)
public void cleanupExpiredBlackLists() {
  List<BlackListEntity> expired = repository
      .findByExpiredAtBefore(LocalDateTime.now());

  repository.deleteAll(expired);
  log.info("만료된 블랙리스트 {} 건 삭제", expired.size());
}
```

---

## 운영 고려사항

### 배치 작업

**만료된 블랙리스트 정리:**
- 스케줄: 매일 새벽 3시
- 처리: 만료된 레코드 삭제
- 로깅: 삭제 건수 기록

**코드:**
```java
@Scheduled(cron = "0 0 3 * * *")
public void scheduledCleanup() {
  cleanupExpiredBlackLists();
}
```

---

### 모니터링

**메트릭:**
- 블랙리스트 등록 건수 (일별)
- 자동 거절 건수 (일별)
- 블랙리스트 조회 응답 시간

**알림:**
- 자동 거절 급증 시 (1시간에 10건 이상)
- 블랙리스트 조회 지연 (200ms 초과)

---

## 참고 자료

- [FLOW.md](../reservation/FLOW.md) - 블랙리스트 체크 프로세스
- [PROJECT_REQUIREMENTS.md](../../requirements/PROJECT_REQUIREMENTS.md)

---

Last Updated: 2025-01-12