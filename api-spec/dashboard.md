# Dashboard API

> 대시보드 API (학생용)

## 목차
- [1. 미제출 과제 목록 조회](#1-미제출-과제-목록-조회)
- [2. 오늘의 강의 목록 조회](#2-오늘의-강의-목록-조회)
- [3. 최신 공지사항 목록 조회](#3-최신-공지사항-목록-조회)
- [4. 수강 현황 요약 조회](#4-수강-현황-요약-조회)

---

## 1. 미제출 과제 목록 조회

마감일이 임박한 미제출 과제 목록을 조회합니다.

### Request
```
GET /api/v1/dashboard/student/pending-assignments
```

### Headers
```
Authorization: Bearer {accessToken}
```

### Query Parameters
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| days | int | X | 7 | 마감일 기준 일수 (최대 30) |

### Response
```json
{
  "success": true,
  "data": [
    {
      "assignmentId": 1,
      "courseId": 1,
      "courseName": "자료구조",
      "title": "1주차 과제",
      "dueDate": "2025-01-20T23:59:59",
      "daysRemaining": 3,
      "isUrgent": true
    },
    {
      "assignmentId": 2,
      "courseId": 2,
      "courseName": "운영체제",
      "title": "프로세스 스케줄링 레포트",
      "dueDate": "2025-01-25T23:59:59",
      "daysRemaining": 8,
      "isUrgent": false
    }
  ],
  "count": 2
}
```

### Notes
- `isUrgent`: 마감까지 3일 이하이면 `true`
- `daysRemaining`: 마감까지 남은 일수 (음수면 마감 지남)

---

## 2. 오늘의 강의 목록 조회

오늘 수강해야 할 강의 목록을 조회합니다.

### Request
```
GET /api/v1/dashboard/student/today-courses
```

### Headers
```
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "success": true,
  "data": [
    {
      "courseId": 1,
      "courseName": "자료구조",
      "professorName": "김교수",
      "schedule": "09:00-10:30",
      "classroom": "공학관 301호",
      "weekNumber": 3,
      "weekTitle": "스택과 큐",
      "hasNewContent": true,
      "attendanceStatus": "NOT_ATTENDED"
    },
    {
      "courseId": 2,
      "courseName": "운영체제",
      "professorName": "이교수",
      "schedule": "13:00-14:30",
      "classroom": "공학관 401호",
      "weekNumber": 3,
      "weekTitle": "프로세스 관리",
      "hasNewContent": false,
      "attendanceStatus": "ATTENDED"
    }
  ],
  "count": 2
}
```

### Attendance Status
| 상태 | 설명 |
|------|------|
| NOT_ATTENDED | 미출석 |
| ATTENDED | 출석 완료 |
| IN_PROGRESS | 진행 중 |

---

## 3. 최신 공지사항 목록 조회

최신 공지사항을 조회합니다.

### Request
```
GET /api/v1/dashboard/student/notices
```

### Query Parameters
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| limit | int | X | 5 | 조회할 개수 (최대 10) |

### Response
```json
{
  "success": true,
  "data": [
    {
      "postId": 100,
      "title": "2025학년도 1학기 수강신청 안내",
      "authorName": "학사팀",
      "createdAt": "2025-01-10T10:00:00",
      "isImportant": true,
      "boardType": "notice"
    },
    {
      "postId": 99,
      "title": "도서관 운영시간 변경 안내",
      "authorName": "도서관",
      "createdAt": "2025-01-08T14:00:00",
      "isImportant": false,
      "boardType": "notice"
    }
  ],
  "count": 2
}
```

---

## 4. 수강 현황 요약 조회

현재 수강 중인 과목 수와 총 학점을 조회합니다.

### Request
```
GET /api/v1/dashboard/student/enrollment-summary
```

### Headers
```
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "success": true,
  "data": {
    "totalCourses": 5,
    "totalCredits": 15,
    "maxCredits": 21,
    "coursesByType": {
      "major": 3,
      "general": 2
    },
    "creditsByType": {
      "major": 9,
      "general": 6
    },
    "averageAttendanceRate": 92.5,
    "completedAssignments": 8,
    "pendingAssignments": 2
  }
}
```

### Response Fields
| 필드 | 설명 |
|------|------|
| totalCourses | 수강 중인 과목 수 |
| totalCredits | 현재 수강 학점 |
| maxCredits | 최대 수강 가능 학점 |
| coursesByType | 유형별 과목 수 |
| creditsByType | 유형별 학점 |
| averageAttendanceRate | 평균 출석률 (%) |
| completedAssignments | 완료한 과제 수 |
| pendingAssignments | 미완료 과제 수 |
