# Enrollment API

> 수강신청 API

## 목차

### 기간 조회
- [1. 현재 활성 기간 조회](#1-현재-활성-기간-조회)

### 강의 조회
- [2. 수강신청용 강의 목록 조회](#2-수강신청용-강의-목록-조회)

### 수강신청
- [3. 일괄 수강신청](#3-일괄-수강신청)
- [4. 일괄 수강신청 취소](#4-일괄-수강신청-취소)
- [5. 내 수강신청 목록 조회](#5-내-수강신청-목록-조회)

### 장바구니
- [6. 장바구니 조회](#6-장바구니-조회)
- [7. 장바구니 일괄 추가](#7-장바구니-일괄-추가)
- [8. 장바구니 일괄 삭제](#8-장바구니-일괄-삭제)
- [9. 장바구니 전체 비우기](#9-장바구니-전체-비우기)

---

## 1. 현재 활성 기간 조회

현재 활성화된 수강신청 기간을 조회합니다.

### Request
```
GET /api/v1/enrollments/periods/current
```

### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| type | string | X | 기간 타입 (기본: ENROLLMENT) |

### Period Types
| 타입 | 설명 |
|------|------|
| ENROLLMENT | 수강신청 기간 |
| COURSE_REGISTRATION | 강의 등록 기간 |
| ADJUSTMENT | 수강 정정 기간 |
| CANCELLATION | 수강 취소 기간 |

### Response
```json
{
  "success": true,
  "data": {
    "periodId": 1,
    "periodType": "ENROLLMENT",
    "academicTermId": 1,
    "academicTermName": "2025학년도 1학기",
    "startDate": "2025-02-01T09:00:00",
    "endDate": "2025-02-10T18:00:00",
    "isActive": true
  }
}
```

---

## 2. 수강신청용 강의 목록 조회

수강신청 가능한 강의 목록을 조회합니다.

### Request
```
GET /api/v1/enrollments/courses
```

### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| enrollmentPeriodId | long | O | 수강신청 기간 ID |
| keyword | string | X | 검색어 |
| departmentId | long | X | 학과 ID |
| courseType | int | X | 강의 유형 |
| credits | int | X | 학점 |
| page | int | X | 페이지 번호 |
| size | int | X | 페이지 크기 |
| sort | string | X | 정렬 기준 |

### Response
```json
{
  "success": true,
  "data": {
    "courses": [
      {
        "courseId": 1,
        "courseName": "자료구조",
        "professorName": "김교수",
        "credits": 3,
        "currentEnrollment": 25,
        "maxEnrollment": 40,
        "isInCart": false,
        "isEnrolled": false
      }
    ],
    "totalPages": 5,
    "totalElements": 100
  }
}
```

---

## 3. 일괄 수강신청

여러 강의를 한 번에 수강신청합니다.

### Request
```
POST /api/v1/enrollments/bulk
```

### Request Body
```json
{
  "courseIds": [1, 2, 3]
}
```

### Response
```json
{
  "success": true,
  "data": {
    "results": [
      {
        "courseId": 1,
        "courseName": "자료구조",
        "success": true,
        "enrollmentId": 100
      },
      {
        "courseId": 2,
        "courseName": "운영체제",
        "success": false,
        "reason": "정원 초과"
      }
    ],
    "summary": {
      "totalAttempted": 3,
      "successCount": 2,
      "failedCount": 1
    }
  },
  "message": "2개 과목 수강신청 완료, 1개 과목 실패"
}
```

---

## 4. 일괄 수강신청 취소

수강신청을 일괄 취소합니다.

### Request
```
DELETE /api/v1/enrollments/bulk
```

### Request Body
```json
{
  "enrollmentIds": [100, 101]
}
```

### Response
```json
{
  "success": true,
  "data": {
    "results": [
      {
        "enrollmentId": 100,
        "courseName": "자료구조",
        "success": true
      }
    ],
    "summary": {
      "totalAttempted": 2,
      "successCount": 2,
      "failedCount": 0
    }
  },
  "message": "2개 과목 취소 완료"
}
```

---

## 5. 내 수강신청 목록 조회

현재 수강신청한 강의 목록을 조회합니다.

### Request
```
GET /api/v1/enrollments/my
```

### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| enrollmentPeriodId | long | X | 수강신청 기간 ID |

### Response
```json
{
  "success": true,
  "data": {
    "enrollments": [
      {
        "enrollmentId": 100,
        "courseId": 1,
        "courseName": "자료구조",
        "professorName": "김교수",
        "credits": 3,
        "schedule": "월 09:00-10:30",
        "enrolledAt": "2025-02-01T09:30:00"
      }
    ],
    "totalCredits": 15,
    "totalCourses": 5
  }
}
```

---

## 6. 장바구니 조회

장바구니에 담긴 강의 목록을 조회합니다.

### Request
```
GET /api/v1/carts
```

### Response
```json
{
  "success": true,
  "data": {
    "cartItems": [
      {
        "cartId": 1,
        "courseId": 1,
        "courseName": "자료구조",
        "professorName": "김교수",
        "credits": 3,
        "currentEnrollment": 25,
        "maxEnrollment": 40,
        "addedAt": "2025-01-30T10:00:00"
      }
    ],
    "totalCredits": 9,
    "totalItems": 3
  }
}
```

---

## 7. 장바구니 일괄 추가

여러 강의를 장바구니에 추가합니다.

### Request
```
POST /api/v1/carts/bulk
```

### Request Body
```json
{
  "courseIds": [1, 2, 3]
}
```

### Response
```json
{
  "success": true,
  "data": {
    "results": [
      {
        "courseId": 1,
        "courseName": "자료구조",
        "success": true,
        "cartId": 1
      }
    ],
    "summary": {
      "successCount": 3,
      "failedCount": 0
    }
  }
}
```

---

## 8. 장바구니 일괄 삭제

장바구니에서 여러 항목을 삭제합니다.

### Request
```
DELETE /api/v1/carts/bulk
```

### Request Body
```json
{
  "cartIds": [1, 2]
}
```

### Response
```json
{
  "success": true,
  "data": {
    "results": [
      {
        "cartId": 1,
        "success": true
      }
    ],
    "summary": {
      "successCount": 2,
      "failedCount": 0
    }
  }
}
```

---

## 9. 장바구니 전체 비우기

장바구니를 전체 비웁니다.

### Request
```
DELETE /api/v1/carts
```

### Response
```json
{
  "success": true,
  "data": {
    "summary": {
      "successCount": 5,
      "failedCount": 0
    }
  }
}
```
