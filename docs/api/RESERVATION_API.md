# 예약 관리 API 명세

예약 생성, 조회, 승인 관리 API 명세서입니다.

---

## 목차

1. [공통 사항](#공통-사항)
2. [예약 생성](#1-예약-생성)
3. [예약 상세 조회](#2-예약-상세-조회)
4. [일간 예약 목록 조회](#3-일간-예약-목록-조회)
5. [주간 예약 목록 조회](#4-주간-예약-목록-조회)
6. [월간 예약 목록 조회](#5-월간-예약-목록-조회)
7. [사용자별 예약 목록 조회](#6-사용자별-예약-목록-조회)
8. [에러 응답](#에러-응답)

---

## 공통 사항

### Base URL

```
http://localhost:8080
```

### 공통 헤더

```http
Content-Type: application/json
Accept: application/json
```

### 공통 응답 코드

| 상태 코드 | 설명 |
|---------|------|
| 200 OK | 요청 성공 |
| 201 Created | 리소스 생성 성공 |
| 400 Bad Request | 잘못된 요청 (유효성 검증 실패) |
| 404 Not Found | 리소스를 찾을 수 없음 |
| 500 Internal Server Error | 서버 내부 오류 |

### 날짜/시간 형식

- 날짜: `yyyy-MM-dd` (예: `2025-01-17`)
- 연월: `yyyy-MM` (예: `2025-01`)
- 날짜시간: ISO 8601 형식 (예: `2025-01-17T14:30:00`)

---

## 1. 예약 생성

예약 ID 기반으로 예약자 정보(이름, 전화번호)를 업데이트합니다.

### HTTP 요청

```http
POST /api/v1/reservations
```

### 요청 본문

```json
{
  "reservationId": 123456,
  "reserverName": "홍길동",
  "reserverPhone": "010-1234-5678"
}
```

#### 필드 설명

| 필드 | 타입 | 필수 | 설명 | 제약사항 |
|-----|------|-----|------|---------|
| reservationId | Long | O | 예약 ID | 양수, 예약 서버에서 발급한 ID |
| reserverName | String | O | 예약자 이름 | 1-50자, 공백 허용 |
| reserverPhone | String | O | 예약자 전화번호 | 10-13자, 숫자와 하이픈만 허용 |

### 응답

#### 성공 응답 (200 OK)

```json
{
  "reservationId": 123456,
  "message": "예약자 정보가 업데이트되었습니다."
}
```

#### 응답 필드

| 필드 | 타입 | 설명 |
|-----|------|------|
| reservationId | Long | 업데이트된 예약 ID |
| message | String | 성공 메시지 |

### 에러 응답

#### 404 Not Found - 예약을 찾을 수 없음

```json
{
  "error": "RESERVATION_NOT_FOUND",
  "message": "예약을 찾을 수 없습니다.",
  "reservationId": 123456
}
```

#### 400 Bad Request - 유효성 검증 실패

```json
{
  "error": "VALIDATION_ERROR",
  "message": "입력값이 유효하지 않습니다.",
  "fields": [
    {
      "field": "reserverName",
      "message": "예약자 이름은 1-50자여야 합니다."
    }
  ]
}
```

### 비즈니스 로직

1. 예약 ID로 예약 조회
2. 예약이 존재하지 않으면 404 예외 발생
3. 예약자 이름과 전화번호를 AES-256-CBC로 암호화
4. 암호화된 데이터를 데이터베이스에 저장
5. 성공 응답 반환

### 예제

#### cURL

```bash
curl -X POST http://localhost:8080/api/v1/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "reservationId": 123456,
    "reserverName": "홍길동",
    "reserverPhone": "010-1234-5678"
  }'
```

---

## 2. 예약 상세 조회

예약 ID로 예약 상세 정보를 조회합니다.

### HTTP 요청

```http
GET /api/v1/reservations/{id}
```

### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|-----|------|
| id | Long | O | 예약 ID |

### 응답

#### 성공 응답 (200 OK)

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
  "reviewId": null,
  "reservationDate": "2025-01-15",
  "reserverName": "홍길동",
  "reserverPhone": "010-1234-5678",
  "approvedAt": "2025-01-15T10:30:00",
  "approvedBy": 0,
  "createdAt": "2025-01-15T10:00:00",
  "updatedAt": "2025-01-15T10:30:00"
}
```

#### 응답 필드

| 필드 | 타입 | 필수 | 설명 |
|-----|------|-----|------|
| reservationId | Long | O | 예약 ID |
| userId | Long | X | 사용자 ID (null 가능) |
| placeId | Long | O | 장소 ID |
| roomId | Long | O | 방 ID |
| status | String | O | 예약 상태 (PENDING_PAYMENT, CONFIRMED, CANCELLED) |
| totalPrice | Long | O | 총 가격 (원) |
| reservationTimePrice | Long | O | 예약 시간 가격 (원) |
| depositPrice | Long | X | 보증금 (원, null 가능) |
| reviewId | Long | X | 리뷰 ID (null 가능) |
| reservationDate | String | O | 예약 날짜 (yyyy-MM-dd) |
| reserverName | String | X | 예약자 이름 (복호화됨, null 가능) |
| reserverPhone | String | X | 예약자 전화번호 (복호화됨, null 가능) |
| approvedAt | String | X | 승인 시각 (ISO 8601, null 가능) |
| approvedBy | Long | X | 승인자 (0: 시스템, 양수: 운영자 ID, null 가능) |
| createdAt | String | O | 생성 시각 (ISO 8601) |
| updatedAt | String | O | 수정 시각 (ISO 8601) |

### 에러 응답

#### 404 Not Found

```json
{
  "error": "RESERVATION_NOT_FOUND",
  "message": "예약을 찾을 수 없습니다.",
  "reservationId": 123456
}
```

### 비즈니스 로직

1. 예약 ID로 예약 조회
2. 예약이 존재하지 않으면 404 예외 발생
3. 예약자 이름과 전화번호를 AES 복호화
4. 응답 DTO로 변환하여 반환

### 예제

#### cURL

```bash
curl -X GET http://localhost:8080/api/v1/reservations/123456
```

---

## 3. 일간 예약 목록 조회

특정 날짜의 예약 목록을 장소/방별로 그룹화하여 조회합니다.

### HTTP 요청

```http
GET /api/v1/reservations/daily?date={date}
```

### 쿼리 파라미터

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|-----|------|-----|
| date | String | O | 조회 날짜 (yyyy-MM-dd) | 2025-01-17 |

### 응답

#### 성공 응답 (200 OK)

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
              "reserverName": "홍길동",
              "startTimes": ["11:00", "12:00", "13:00"],
              "reservationDate": "2025-01-17",
              "status": "CONFIRMED",
              "needsApproval": false,
              "isBlacklisted": false
            },
            {
              "reservationId": 123457,
              "reserverName": "김철수",
              "startTimes": ["14:00", "15:00"],
              "reservationDate": "2025-01-17",
              "status": "PENDING",
              "needsApproval": true,
              "isBlacklisted": true
            }
          ]
        },
        {
          "roomId": 11,
          "roomName": "회의실 B",
          "reservations": []
        }
      ]
    }
  ]
}
```

#### 응답 필드

| 필드 | 타입 | 설명 |
|-----|------|------|
| places | Array | 장소 목록 |
| places[].placeId | Long | 장소 ID |
| places[].rooms | Array | 방 목록 |
| places[].rooms[].roomId | Long | 방 ID |
| places[].rooms[].reservations | Array | 예약 목록 |
| places[].rooms[].reservations[].reservationId | Long | 예약 ID |
| places[].rooms[].reservations[].reserverName | String | 예약자 이름 (복호화됨) |
| places[].rooms[].reservations[].startTimes | Array[String] | 시작 시간 목록 (HH:mm 형식) |
| places[].rooms[].reservations[].reservationDate | String | 예약 날짜 (yyyy-MM-dd) |
| places[].rooms[].reservations[].status | String | 예약 상태 (PENDING, CONFIRMED, CANCELLED) |
| places[].rooms[].reservations[].needsApproval | Boolean | 승인 필요 여부 (PENDING=true, 그 외=false) |
| places[].rooms[].reservations[].isBlacklisted | Boolean | 블랙리스트 여부 (어드민용, 수동 승인 판단 참고) |

### 에러 응답

#### 400 Bad Request - 잘못된 날짜 형식

```json
{
  "error": "INVALID_DATE_FORMAT",
  "message": "날짜 형식이 올바르지 않습니다. yyyy-MM-dd 형식을 사용하세요.",
  "date": "2025-13-01"
}
```

### 비즈니스 로직

1. 날짜 파라미터 검증 (yyyy-MM-dd 형식)
2. 해당 날짜의 모든 예약 조회
3. 장소별, 방별로 그룹화
4. 장소별로 블랙리스트 배치 조회 (N+1 문제 방지)
5. 예약자 정보 복호화 및 블랙리스트 여부 포함
6. 응답 DTO로 변환하여 반환

**참고**: `isBlacklisted`는 어드민이 수동으로 승인/거절 판단 시 참고하는 정보입니다. 자동 거절하지 않습니다.

### 예제

#### cURL

```bash
curl -X GET "http://localhost:8080/api/v1/reservations/daily?date=2025-01-17"
```

---

## 4. 주간 예약 목록 조회

특정 주의 예약 목록을 장소/방별로 그룹화하여 조회합니다. (7일치)

### HTTP 요청

```http
GET /api/v1/reservations/weekly?startDate={startDate}
```

### 쿼리 파라미터

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|-----|------|-----|
| startDate | String | O | 주 시작 날짜 (yyyy-MM-dd) | 2025-01-15 |

### 응답

#### 성공 응답 (200 OK)

일간 조회와 동일한 형식이지만 7일치 데이터를 포함합니다.

```json
{
  "places": [
    {
      "placeId": 100,
      "rooms": [
        {
          "roomId": 10,
          "reservations": [
            {
              "reservationId": 123456,
              "reserverName": "홍길동",
              "startTimes": ["11:00", "12:00"],
              "reservationDate": "2025-01-15",
              "status": "CONFIRMED",
              "needsApproval": false,
              "isBlacklisted": false
            },
            {
              "reservationId": 123457,
              "reserverName": "김철수",
              "startTimes": ["14:00"],
              "reservationDate": "2025-01-16",
              "status": "PENDING",
              "needsApproval": true,
              "isBlacklisted": true
            },
            {
              "reservationId": 123458,
              "reserverName": "이영희",
              "startTimes": ["10:00", "11:00"],
              "reservationDate": "2025-01-17",
              "status": "CONFIRMED",
              "needsApproval": false,
              "isBlacklisted": false
            }
          ]
        }
      ]
    }
  ],
  "totalCount": 3,
  "period": "WEEKLY"
}
```

#### 응답 필드

| 필드 | 타입 | 설명 |
|-----|------|------|
| places | Array | 장소 목록 |
| places[].placeId | Long | 장소 ID |
| places[].rooms | Array | 방 목록 |
| places[].rooms[].roomId | Long | 방 ID |
| places[].rooms[].reservations | Array | 예약 목록 |
| places[].rooms[].reservations[].reservationId | Long | 예약 ID |
| places[].rooms[].reservations[].reserverName | String | 예약자 이름 (복호화됨) |
| places[].rooms[].reservations[].startTimes | Array[String] | 시작 시간 목록 (HH:mm 형식) |
| places[].rooms[].reservations[].reservationDate | String | 예약 날짜 (yyyy-MM-dd) |
| places[].rooms[].reservations[].status | String | 예약 상태 (PENDING, CONFIRMED, CANCELLED) |
| places[].rooms[].reservations[].needsApproval | Boolean | 승인 필요 여부 (PENDING=true, 그 외=false) |
| places[].rooms[].reservations[].isBlacklisted | Boolean | 블랙리스트 여부 (어드민용, 수동 승인 판단 참고) |
| totalCount | Integer | 총 예약 건수 |
| period | String | 조회 기간 타입 (WEEKLY) |

### 에러 응답

#### 400 Bad Request

```json
{
  "error": "INVALID_DATE_FORMAT",
  "message": "날짜 형식이 올바르지 않습니다. yyyy-MM-dd 형식을 사용하세요.",
  "startDate": "2025-13-01"
}
```

### 비즈니스 로직

1. 시작 날짜 파라미터 검증 (yyyy-MM-dd 형식)
2. 종료 날짜 계산 (시작 날짜 + 6일)
3. 날짜 범위 내 모든 예약 조회
4. 장소별, 방별로 그룹화
5. 장소별로 블랙리스트 배치 조회 (N+1 문제 방지)
6. 예약자 정보 복호화 및 블랙리스트 여부 포함
7. 응답 DTO로 변환하여 반환

**참고**: `isBlacklisted`는 어드민이 수동으로 승인/거절 판단 시 참고하는 정보입니다. 자동 거절하지 않습니다.

### 예제

#### cURL

```bash
curl -X GET "http://localhost:8080/api/v1/reservations/weekly?startDate=2025-01-15"
```

---

## 5. 월간 예약 목록 조회

특정 월의 예약 목록을 장소/방별로 그룹화하여 조회합니다.

### HTTP 요청

```http
GET /api/v1/reservations/monthly?yearMonth={yearMonth}
```

### 쿼리 파라미터

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|-----|------|-----|
| yearMonth | String | O | 조회 연월 (yyyy-MM) | 2025-01 |

### 응답

#### 성공 응답 (200 OK)

일간 조회와 동일한 형식이지만 해당 월 전체 데이터를 포함합니다.

```json
{
  "places": [
    {
      "placeId": 100,
      "rooms": [
        {
          "roomId": 10,
          "reservations": [
            {
              "reservationId": 123456,
              "reserverName": "홍길동",
              "startTimes": ["11:00", "12:00"],
              "reservationDate": "2025-01-05",
              "status": "CONFIRMED",
              "needsApproval": false,
              "isBlacklisted": false
            },
            {
              "reservationId": 123457,
              "reserverName": "김철수",
              "startTimes": ["14:00", "15:00"],
              "reservationDate": "2025-01-12",
              "status": "PENDING",
              "needsApproval": true,
              "isBlacklisted": true
            },
            {
              "reservationId": 123458,
              "reserverName": "이영희",
              "startTimes": ["09:00"],
              "reservationDate": "2025-01-20",
              "status": "CONFIRMED",
              "needsApproval": false,
              "isBlacklisted": false
            }
          ]
        }
      ]
    }
  ],
  "totalCount": 3,
  "period": "MONTHLY"
}
```

#### 응답 필드

| 필드 | 타입 | 설명 |
|-----|------|------|
| places | Array | 장소 목록 |
| places[].placeId | Long | 장소 ID |
| places[].rooms | Array | 방 목록 |
| places[].rooms[].roomId | Long | 방 ID |
| places[].rooms[].reservations | Array | 예약 목록 |
| places[].rooms[].reservations[].reservationId | Long | 예약 ID |
| places[].rooms[].reservations[].reserverName | String | 예약자 이름 (복호화됨) |
| places[].rooms[].reservations[].startTimes | Array[String] | 시작 시간 목록 (HH:mm 형식) |
| places[].rooms[].reservations[].reservationDate | String | 예약 날짜 (yyyy-MM-dd) |
| places[].rooms[].reservations[].status | String | 예약 상태 (PENDING, CONFIRMED, CANCELLED) |
| places[].rooms[].reservations[].needsApproval | Boolean | 승인 필요 여부 (PENDING=true, 그 외=false) |
| places[].rooms[].reservations[].isBlacklisted | Boolean | 블랙리스트 여부 (어드민용, 수동 승인 판단 참고) |
| totalCount | Integer | 총 예약 건수 |
| period | String | 조회 기간 타입 (MONTHLY) |

### 에러 응답

#### 400 Bad Request

```json
{
  "error": "INVALID_YEAR_MONTH_FORMAT",
  "message": "연월 형식이 올바르지 않습니다. yyyy-MM 형식을 사용하세요.",
  "yearMonth": "2025-13"
}
```

### 비즈니스 로직

1. 연월 파라미터 검증 (yyyy-MM 형식)
2. 해당 월의 시작일과 종료일 계산
3. 날짜 범위 내 모든 예약 조회
4. 장소별, 방별로 그룹화
5. 장소별로 블랙리스트 배치 조회 (N+1 문제 방지)
6. 예약자 정보 복호화 및 블랙리스트 여부 포함
7. 응답 DTO로 변환하여 반환

**참고**: `isBlacklisted`는 어드민이 수동으로 승인/거절 판단 시 참고하는 정보입니다. 자동 거절하지 않습니다.

### 예제

#### cURL

```bash
curl -X GET "http://localhost:8080/api/v1/reservations/monthly?yearMonth=2025-01"
```

---

## 6. 사용자별 예약 목록 조회

특정 사용자의 예약 목록을 커서 기반 페이징으로 조회합니다.

### HTTP 요청

```http
GET /api/v1/reservations/users/{userId}?period={period}&cursor={cursor}&size={size}&statuses={statuses}
```

### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|-----|------|
| userId | Long | O | 사용자 ID |

### 쿼리 파라미터

| 파라미터 | 타입 | 필수 | 설명 | 기본값 | 제약사항 |
|---------|------|-----|------|--------|---------|
| period | String | O | 기간 타입 | - | DAILY, WEEKLY, MONTHLY |
| cursor | String | X | Base64 인코딩된 커서 | null | 첫 페이지는 생략 |
| size | Integer | X | 페이지 크기 | 20 | 1-100 |
| statuses | String | X | 상태 필터 (쉼표 구분) | 모든 상태 | PENDING_PAYMENT, CONFIRMED, CANCELLED |

### 응답

#### 성공 응답 (200 OK)

```json
{
  "items": [
    {
      "reservationId": 123456,
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
      "createdAt": "2025-01-15T10:00:00"
    },
    {
      "reservationId": 123455,
      "placeId": 100,
      "roomId": 11,
      "status": "CONFIRMED",
      "totalPrice": 50000,
      "reservationTimePrice": 50000,
      "depositPrice": null,
      "reservationDate": "2025-01-14",
      "reserverName": "홍길동",
      "reserverPhone": "010-1234-5678",
      "approvedAt": "2025-01-14T11:00:00",
      "approvedBy": 0,
      "createdAt": "2025-01-14T10:30:00"
    }
  ],
  "nextCursor": "eyJyZXNlcnZhdGlvbkRhdGUiOiIyMDI1LTAxLTE0IiwicmVzZXJ2YXRpb25JZCI6MTIzNDU1fQ==",
  "hasNext": true,
  "size": 20
}
```

#### 응답 필드

| 필드 | 타입 | 설명 |
|-----|------|------|
| items | Array | 예약 항목 목록 |
| items[].reservationId | Long | 예약 ID |
| items[].placeId | Long | 장소 ID |
| items[].roomId | Long | 방 ID |
| items[].status | String | 예약 상태 |
| items[].totalPrice | Long | 총 가격 (원) |
| items[].reservationTimePrice | Long | 예약 시간 가격 (원) |
| items[].depositPrice | Long | 보증금 (원, null 가능) |
| items[].reservationDate | String | 예약 날짜 (yyyy-MM-dd) |
| items[].reserverName | String | 예약자 이름 (복호화됨) |
| items[].reserverPhone | String | 예약자 전화번호 (복호화됨) |
| items[].approvedAt | String | 승인 시각 (ISO 8601, null 가능) |
| items[].approvedBy | Long | 승인자 (0: 시스템, 양수: 운영자 ID, null 가능) |
| items[].createdAt | String | 생성 시각 (ISO 8601) |
| nextCursor | String | 다음 페이지 커서 (Base64 인코딩, null이면 마지막 페이지) |
| hasNext | Boolean | 다음 페이지 존재 여부 |
| size | Integer | 요청한 페이지 크기 |

### 커서 구조

커서는 다음 JSON을 Base64로 인코딩한 문자열입니다:

```json
{
  "reservationDate": "2025-01-14",
  "reservationId": 123455
}
```

- `reservationDate`: 마지막 항목의 예약 날짜
- `reservationId`: 마지막 항목의 예약 ID
- 정렬 순서: `reservationDate DESC, reservationId DESC`

### 에러 응답

#### 400 Bad Request - 잘못된 period 값

```json
{
  "error": "INVALID_PERIOD_TYPE",
  "message": "기간 타입이 올바르지 않습니다. DAILY, WEEKLY, MONTHLY 중 하나를 사용하세요.",
  "period": "YEARLY"
}
```

#### 400 Bad Request - 잘못된 커서

```json
{
  "error": "INVALID_CURSOR",
  "message": "커서 형식이 올바르지 않습니다.",
  "cursor": "invalid-cursor"
}
```

#### 400 Bad Request - 잘못된 size

```json
{
  "error": "INVALID_PAGE_SIZE",
  "message": "페이지 크기는 1-100 사이여야 합니다.",
  "size": 200
}
```

#### 404 Not Found - 사용자 없음

```json
{
  "error": "USER_NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다.",
  "userId": 9999
}
```

### 비즈니스 로직

1. 사용자 ID 존재 여부 검증
2. period 타입으로 날짜 범위 계산
   - DAILY: 오늘
   - WEEKLY: 오늘부터 7일
   - MONTHLY: 이번 달
3. cursor 디코딩 (Base64 → JSON)
4. 데이터베이스 조회 (복합 인덱스 활용)
   - 조건: user_id, 날짜 범위, cursor, statuses
   - 정렬: reservation_date DESC, reservation_id DESC
   - 개수: size + 1 (hasNext 판단용)
5. hasNext 판단 및 nextCursor 생성
6. 예약자 정보 복호화
7. 응답 반환

### 성능 최적화

- 복합 인덱스 활용: `idx_user_reservation_query (user_id, reservation_date DESC, reservation_id DESC)`
- 커서 기반 페이징으로 OFFSET 없이 효율적인 페이징
- 커버링 인덱스로 테이블 접근 최소화

### 예제

#### cURL - 첫 페이지

```bash
curl -X GET "http://localhost:8080/api/v1/reservations/users/1001?period=MONTHLY&size=20"
```

#### cURL - 다음 페이지

```bash
curl -X GET "http://localhost:8080/api/v1/reservations/users/1001?period=MONTHLY&cursor=eyJyZXNlcnZhdGlvbkRhdGUiOiIyMDI1LTAxLTE0IiwicmVzZXJ2YXRpb25JZCI6MTIzNDU1fQ==&size=20"
```

#### cURL - 상태 필터링

```bash
curl -X GET "http://localhost:8080/api/v1/reservations/users/1001?period=WEEKLY&statuses=CONFIRMED,PENDING_PAYMENT&size=20"
```

---

## 에러 응답

### 공통 에러 형식

```json
{
  "error": "ERROR_CODE",
  "message": "사람이 읽을 수 있는 에러 메시지",
  "field": "추가 정보 (선택)"
}
```

### 에러 코드 목록

| 에러 코드 | HTTP 상태 | 설명 |
|----------|----------|------|
| RESERVATION_NOT_FOUND | 404 | 예약을 찾을 수 없음 |
| USER_NOT_FOUND | 404 | 사용자를 찾을 수 없음 |
| VALIDATION_ERROR | 400 | 유효성 검증 실패 |
| INVALID_DATE_FORMAT | 400 | 잘못된 날짜 형식 |
| INVALID_YEAR_MONTH_FORMAT | 400 | 잘못된 연월 형식 |
| INVALID_PERIOD_TYPE | 400 | 잘못된 기간 타입 |
| INVALID_CURSOR | 400 | 잘못된 커서 형식 |
| INVALID_PAGE_SIZE | 400 | 잘못된 페이지 크기 |
| INTERNAL_SERVER_ERROR | 500 | 서버 내부 오류 |

---

## 부록

### PeriodType 날짜 범위 계산

| Period Type | 시작 날짜 | 종료 날짜 |
|-------------|---------|----------|
| DAILY | 오늘 | 오늘 |
| WEEKLY | 오늘 | 오늘 + 6일 |
| MONTHLY | 이번 달 1일 | 이번 달 마지막 일 |

### 예약 상태 전이

```
PENDING_PAYMENT → CONFIRMED
                   │
                   └→ CANCELLED
```

- PENDING_PAYMENT: 결제 대기 중
- CONFIRMED: 예약 확정 (결제 완료)
- CANCELLED: 예약 취소

### 승인자 (approvedBy) 값

| 값 | 설명 |
|----|------|
| 0 | 시스템 자동 승인 |
| 양수 | 운영자 ID (수동 승인) |
| null | 미승인 (PENDING_PAYMENT 상태) |

---

Last Updated: 2025-01-17