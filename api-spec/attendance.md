# Attendance API

> 출석 관리 API

## 목차

### 학생용
- [1. 내 전체 출석 현황 조회](#1-내-전체-출석-현황-조회)
- [2. 특정 강의 출석 현황 조회](#2-특정-강의-출석-현황-조회)
- [3. 주차별 출석 상세 조회](#3-주차별-출석-상세-조회)

### 교수용
- [4. 강의 전체 출석 현황 조회](#4-강의-전체-출석-현황-조회)
- [5. 학생별 출석 목록 조회](#5-학생별-출석-목록-조회)
- [6. 주차별 학생 출석 현황 조회](#6-주차별-학생-출석-현황-조회)

---

## 학생용 API

### 1. 내 전체 출석 현황 조회

수강 중인 모든 강의의 출석 현황을 조회합니다.

### Request
```
GET /api/v1/attendance/my
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
      "totalWeeks": 15,
      "attendedWeeks": 10,
      "attendanceRate": 66.7,
      "status": "NORMAL"
    },
    {
      "courseId": 2,
      "courseName": "운영체제",
      "professorName": "이교수",
      "totalWeeks": 15,
      "attendedWeeks": 8,
      "attendanceRate": 53.3,
      "status": "WARNING"
    }
  ]
}
```

### Status Types
| 상태 | 설명 |
|------|------|
| NORMAL | 정상 출석 |
| WARNING | 경고 (출석률 낮음) |
| FAIL | 출석 미달 |

---

### 2. 특정 강의 출석 현황 조회

특정 강의의 상세 출석 현황을 조회합니다.

### Request
```
GET /api/v1/attendance/courses/{courseId}
```

### Path Parameters
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| courseId | long | 강의 ID |

### Response
```json
{
  "success": true,
  "data": {
    "courseId": 1,
    "courseName": "자료구조",
    "professorName": "김교수",
    "totalWeeks": 15,
    "attendedWeeks": 10,
    "attendanceRate": 66.7,
    "weekAttendances": [
      {
        "weekId": 1,
        "weekNumber": 1,
        "title": "오리엔테이션",
        "attended": true,
        "attendedAt": "2025-03-03T09:15:00",
        "totalContents": 3,
        "completedContents": 3,
        "completionRate": 100.0
      },
      {
        "weekId": 2,
        "weekNumber": 2,
        "title": "배열과 연결리스트",
        "attended": false,
        "totalContents": 4,
        "completedContents": 1,
        "completionRate": 25.0
      }
    ]
  }
}
```

---

### 3. 주차별 출석 상세 조회

특정 주차의 출석 상세 정보를 조회합니다.

### Request
```
GET /api/v1/attendance/courses/{courseId}/weeks/{weekId}
```

### Path Parameters
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| courseId | long | 강의 ID |
| weekId | long | 주차 ID |

### Response
```json
{
  "success": true,
  "data": {
    "weekId": 1,
    "weekNumber": 1,
    "title": "오리엔테이션",
    "attended": true,
    "attendedAt": "2025-03-03T09:15:00",
    "totalContents": 3,
    "completedContents": 3,
    "completionRate": 100.0,
    "contents": [
      {
        "contentId": 1,
        "contentType": "VIDEO",
        "title": "강의 소개",
        "completed": true,
        "completedAt": "2025-03-03T09:15:00",
        "watchedDuration": 600,
        "totalDuration": 600
      }
    ]
  }
}
```

---

## 교수용 API

### 4. 강의 전체 출석 현황 조회

강의의 전체 출석 통계를 조회합니다.

### Request
```
GET /api/v1/professor/courses/{courseId}/attendance
```

### Path Parameters
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| courseId | long | 강의 ID |

### Response
```json
{
  "success": true,
  "data": {
    "courseId": 1,
    "courseName": "자료구조",
    "totalStudents": 40,
    "totalWeeks": 15,
    "averageAttendanceRate": 85.5,
    "weeklyStats": [
      {
        "weekId": 1,
        "weekNumber": 1,
        "title": "오리엔테이션",
        "attendedCount": 38,
        "attendanceRate": 95.0
      }
    ],
    "statusSummary": {
      "normal": 35,
      "warning": 3,
      "fail": 2
    }
  }
}
```

---

### 5. 학생별 출석 목록 조회

강의에 등록된 학생들의 출석 현황을 조회합니다.

### Request
```
GET /api/v1/professor/courses/{courseId}/attendance/students
```

### Response
```json
{
  "success": true,
  "data": [
    {
      "studentId": 2025010001,
      "studentNumber": "2025010001",
      "studentName": "홍길동",
      "departmentName": "컴퓨터공학과",
      "attendedWeeks": 10,
      "totalWeeks": 15,
      "attendanceRate": 66.7,
      "status": "NORMAL"
    }
  ]
}
```

---

### 6. 주차별 학생 출석 현황 조회

특정 주차의 학생별 출석 현황을 조회합니다.

### Request
```
GET /api/v1/professor/courses/{courseId}/weeks/{weekId}/attendance
```

### Path Parameters
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| courseId | long | 강의 ID |
| weekId | long | 주차 ID |

### Response
```json
{
  "success": true,
  "data": [
    {
      "studentId": 2025010001,
      "studentNumber": "2025010001",
      "studentName": "홍길동",
      "attended": true,
      "attendedAt": "2025-03-03T09:15:00",
      "completedContents": 3,
      "totalContents": 3,
      "completionRate": 100.0
    },
    {
      "studentId": 2025010002,
      "studentNumber": "2025010002",
      "studentName": "김철수",
      "attended": false,
      "completedContents": 0,
      "totalContents": 3,
      "completionRate": 0.0
    }
  ]
}
```
