# Course API

> 강의 관리 API

## 목차

### 강의 조회 (공통)
- [1. 강의 목록 검색](#1-강의-목록-검색)
- [2. 강의 상세 조회](#2-강의-상세-조회)

### 교수 강의 관리
- [3. 강의 개설](#3-강의-개설)
- [4. 강의 수정](#4-강의-수정)
- [5. 강의 취소](#5-강의-취소)
- [6. 내 강의 목록 조회](#6-내-강의-목록-조회-교수)
- [7. 교수 강의 상세 조회](#7-교수-강의-상세-조회)

### 주차/콘텐츠 관리
- [8. 주차 목록 조회](#8-주차-목록-조회)
- [9. 주차 생성](#9-주차-생성)
- [10. 주차 수정](#10-주차-수정)
- [11. 주차 삭제](#11-주차-삭제)
- [12. 주차별 콘텐츠 목록 조회](#12-주차별-콘텐츠-목록-조회)
- [13. 콘텐츠 등록](#13-콘텐츠-등록)
- [14. 콘텐츠 수정](#14-콘텐츠-수정)
- [15. 콘텐츠 삭제](#15-콘텐츠-삭제)
- [16. 콘텐츠 단건 수정/삭제](#16-콘텐츠-단건-수정삭제)

---

## 1. 강의 목록 검색

개설된 강의를 검색합니다.

### Request
```
GET /api/v1/courses
```

### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| enrollmentPeriodId | long | O | 수강신청 기간 ID |
| keyword | string | X | 검색어 (강의명, 교수명) |
| departmentId | long | X | 학과 ID |
| courseType | int | X | 강의 유형 (1: 전공, 2: 교양) |
| credits | int | X | 학점 |
| page | int | X | 페이지 번호 (기본: 0) |
| size | int | X | 페이지 크기 (기본: 20) |
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
        "departmentName": "컴퓨터공학과",
        "credits": 3,
        "courseType": "전공필수",
        "schedule": "월 09:00-10:30, 수 09:00-10:30",
        "classroom": "공학관 301호",
        "currentEnrollment": 25,
        "maxEnrollment": 40
      }
    ],
    "totalPages": 5,
    "totalElements": 100,
    "currentPage": 0
  }
}
```

---

## 2. 강의 상세 조회

강의 상세 정보를 조회합니다.

### Request
```
GET /api/v1/courses/{courseId}
```

### Response
```json
{
  "success": true,
  "data": {
    "courseId": 1,
    "courseName": "자료구조",
    "professorName": "김교수",
    "professorId": 2024010001,
    "departmentName": "컴퓨터공학과",
    "credits": 3,
    "courseType": "전공필수",
    "description": "자료구조의 기본 개념과 알고리즘을 학습합니다.",
    "schedule": "월 09:00-10:30, 수 09:00-10:30",
    "classroom": "공학관 301호",
    "currentEnrollment": 25,
    "maxEnrollment": 40,
    "syllabus": "..."
  }
}
```

---

## 3. 강의 개설

새로운 강의를 개설합니다. (교수 전용)

### Request
```
POST /api/v1/professor/courses
```

### Request Body
```json
{
  "subjectId": 1,
  "academicTermId": 1,
  "maxEnrollment": 40,
  "schedule": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "09:00",
      "endTime": "10:30"
    }
  ],
  "classroom": "공학관 301호",
  "description": "강의 설명"
}
```

### Response
```json
{
  "success": true,
  "data": {
    "courseId": 1,
    "courseName": "자료구조",
    "message": "강의가 개설되었습니다."
  }
}
```

---

## 4. 강의 수정

강의 정보를 수정합니다. (교수 전용)

### Request
```
PUT /api/v1/professor/courses/{courseId}
```

### Request Body
```json
{
  "maxEnrollment": 50,
  "classroom": "공학관 401호",
  "description": "수정된 강의 설명"
}
```

### Response
```json
{
  "success": true,
  "data": {
    "courseId": 1,
    "courseName": "자료구조",
    "maxEnrollment": 50
  }
}
```

---

## 5. 강의 취소

개설한 강의를 취소합니다. (교수 전용)

### Request
```
DELETE /api/v1/professor/courses/{courseId}
```

### Response
```json
{
  "success": true,
  "data": null,
  "message": "강의가 취소되었습니다."
}
```

---

## 6. 내 강의 목록 조회 (교수)

교수가 개설한 강의 목록을 조회합니다.

### Request
```
GET /api/v1/professor/courses
```

### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| academicTermId | long | X | 학기 ID (미지정 시 전체) |

### Response
```json
{
  "success": true,
  "data": {
    "courses": [
      {
        "courseId": 1,
        "courseName": "자료구조",
        "credits": 3,
        "currentEnrollment": 25,
        "maxEnrollment": 40,
        "academicTermName": "2025학년도 1학기"
      }
    ]
  }
}
```

---

## 7. 교수 강의 상세 조회

교수용 강의 상세 정보를 조회합니다.

### Request
```
GET /api/v1/professor/courses/{courseId}
```

### Response
```json
{
  "success": true,
  "data": {
    "courseId": 1,
    "courseName": "자료구조",
    "credits": 3,
    "currentEnrollment": 25,
    "maxEnrollment": 40,
    "enrolledStudents": [...],
    "weeks": [...]
  }
}
```

---

## 8. 주차 목록 조회

강의의 주차 목록을 조회합니다.

### Request
```
GET /api/v1/professor/courses/{courseId}/weeks
```

### Response
```json
{
  "success": true,
  "data": [
    {
      "weekId": 1,
      "weekNumber": 1,
      "title": "오리엔테이션",
      "startDate": "2025-03-02",
      "endDate": "2025-03-08",
      "contentCount": 3
    }
  ]
}
```

---

## 9. 주차 생성

새로운 주차를 생성합니다. (교수 전용)

### Request
```
POST /api/v1/professor/courses/{courseId}/weeks
```

### Request Body
```json
{
  "weekNumber": 1,
  "title": "오리엔테이션",
  "startDate": "2025-03-02",
  "endDate": "2025-03-08"
}
```

### Response
```json
{
  "success": true,
  "data": {
    "weekId": 1,
    "weekNumber": 1,
    "title": "오리엔테이션"
  },
  "message": "주차가 생성되었습니다"
}
```

---

## 10. 주차 수정

주차 정보를 수정합니다. (교수 전용)

### Request
```
PUT /api/v1/professor/courses/{courseId}/weeks/{weekId}
```

### Request Body
```json
{
  "title": "수정된 제목",
  "startDate": "2025-03-02",
  "endDate": "2025-03-08"
}
```

---

## 11. 주차 삭제

주차를 삭제합니다. (교수 전용)

### Request
```
DELETE /api/v1/professor/courses/{courseId}/weeks/{weekId}
```

---

## 12. 주차별 콘텐츠 목록 조회

특정 주차의 콘텐츠 목록을 조회합니다.

### Request
```
GET /api/v1/professor/courses/{courseId}/weeks/{weekId}/contents
```

### Response
```json
{
  "success": true,
  "data": {
    "weekId": 1,
    "weekNumber": 1,
    "title": "오리엔테이션",
    "contents": [
      {
        "contentId": 1,
        "contentType": "VIDEO",
        "title": "강의 소개 영상",
        "duration": 600,
        "order": 1
      },
      {
        "contentId": 2,
        "contentType": "DOCUMENT",
        "title": "강의계획서",
        "order": 2
      }
    ]
  }
}
```

---

## 13. 콘텐츠 등록

주차에 콘텐츠를 등록합니다. (교수 전용)

### Request
```
POST /api/v1/professor/courses/{courseId}/weeks/{weekId}/contents
```

### Request Body
```json
{
  "contentType": "VIDEO",
  "title": "1주차 강의",
  "description": "강의 설명",
  "videoId": "abc123",
  "duration": 3600,
  "order": 1
}
```

### Content Types
| 타입 | 설명 |
|------|------|
| VIDEO | 영상 콘텐츠 |
| DOCUMENT | 문서/자료 |
| QUIZ | 퀴즈 |
| ASSIGNMENT | 과제 |

---

## 14. 콘텐츠 수정

콘텐츠를 수정합니다. (교수 전용)

### Request
```
PUT /api/v1/professor/courses/{courseId}/weeks/{weekId}/contents/{contentId}
```

---

## 15. 콘텐츠 삭제

콘텐츠를 삭제합니다. (교수 전용)

### Request
```
DELETE /api/v1/professor/courses/{courseId}/weeks/{weekId}/contents/{contentId}
```

---

## 16. 콘텐츠 단건 수정/삭제

콘텐츠 ID만으로 수정/삭제합니다. (교수 전용)

### 수정
```
PUT /api/v1/professor/contents/{contentId}
```

### 삭제
```
DELETE /api/v1/professor/contents/{contentId}
```
