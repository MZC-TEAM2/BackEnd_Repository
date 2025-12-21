# Assignment API

> 과제 관리 API

## 목차

### 교수용
- [1. 과제 등록](#1-과제-등록)
- [2. 과제 수정](#2-과제-수정)
- [3. 과제 삭제](#3-과제-삭제)
- [4. 제출 목록 조회](#4-제출-목록-조회)
- [5. 과제 채점](#5-과제-채점)
- [6. 재제출 허용](#6-재제출-허용)

### 학생용
- [7. 과제 목록 조회](#7-과제-목록-조회)
- [8. 과제 상세 조회](#8-과제-상세-조회)
- [9. 과제 제출](#9-과제-제출)
- [10. 내 제출 조회](#10-내-제출-조회)

---

## 교수용 API

### 1. 과제 등록

새로운 과제를 등록합니다.

### Request
```
POST /api/v1/assignments
```

### Request Body
```json
{
  "courseId": 1,
  "title": "1주차 과제",
  "content": "과제 설명입니다.",
  "dueDate": "2025-03-15T23:59:59",
  "maxScore": 100,
  "attachmentIds": [1, 2]
}
```

### Response
```json
{
  "id": 1,
  "postId": 100,
  "courseId": 1,
  "courseName": "자료구조",
  "title": "1주차 과제",
  "content": "과제 설명입니다.",
  "dueDate": "2025-03-15T23:59:59",
  "maxScore": 100,
  "attachments": [...],
  "submissionCount": 0,
  "createdAt": "2025-03-01T10:00:00",
  "createdBy": 2024010001
}
```

---

### 2. 과제 수정

과제 정보를 수정합니다.

### Request
```
PUT /api/v1/assignments/{id}
```

### Request Body
```json
{
  "title": "수정된 과제 제목",
  "content": "수정된 과제 설명",
  "dueDate": "2025-03-20T23:59:59",
  "maxScore": 100
}
```

---

### 3. 과제 삭제

과제를 삭제합니다.

### Request
```
DELETE /api/v1/assignments/{id}
```

### Response
```
HTTP/1.1 204 No Content
```

---

### 4. 제출 목록 조회

과제에 대한 모든 제출물을 조회합니다.

### Request
```
GET /api/v1/assignments/{id}/submissions
```

### Response
```json
[
  {
    "submissionId": 1,
    "assignmentId": 1,
    "studentId": 2025010001,
    "studentNumber": "2025010001",
    "studentName": "홍길동",
    "content": "제출 내용",
    "attachments": [...],
    "submittedAt": "2025-03-10T15:30:00",
    "score": null,
    "feedback": null,
    "status": "SUBMITTED"
  },
  {
    "submissionId": 2,
    "assignmentId": 1,
    "studentId": 2025010002,
    "studentNumber": "2025010002",
    "studentName": "김철수",
    "submittedAt": "2025-03-12T10:00:00",
    "score": 85,
    "feedback": "잘 했습니다.",
    "status": "GRADED"
  }
]
```

### Status Types
| 상태 | 설명 |
|------|------|
| SUBMITTED | 제출됨 |
| GRADED | 채점 완료 |
| RESUBMISSION_ALLOWED | 재제출 허용됨 |

---

### 5. 과제 채점

제출된 과제를 채점합니다.

### Request
```
PUT /api/v1/assignments/submissions/{submissionId}/grade
```

### Request Body
```json
{
  "score": 85,
  "feedback": "잘 했습니다. 다음에는 코드 주석을 더 추가해주세요."
}
```

### Response
```json
{
  "submissionId": 1,
  "assignmentId": 1,
  "studentName": "홍길동",
  "score": 85,
  "maxScore": 100,
  "feedback": "잘 했습니다. 다음에는 코드 주석을 더 추가해주세요.",
  "gradedAt": "2025-03-15T14:00:00",
  "gradedBy": 2024010001,
  "status": "GRADED"
}
```

---

### 6. 재제출 허용

학생에게 과제 재제출을 허용합니다.

### Request
```
POST /api/v1/assignments/submissions/{submissionId}/allow-resubmission
```

### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| deadline | datetime | X | 재제출 마감일 (미지정 시 원래 마감일) |

### Response
```json
{
  "submissionId": 1,
  "status": "RESUBMISSION_ALLOWED",
  "resubmissionDeadline": "2025-03-20T23:59:59",
  "message": "재제출이 허용되었습니다."
}
```

---

## 학생용 API

### 7. 과제 목록 조회

과제 목록을 조회합니다.

### Request
```
GET /api/v1/assignments
```

### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| courseId | long | X | 강의 ID (미지정 시 전체) |

### Response
```json
[
  {
    "id": 1,
    "postId": 100,
    "courseId": 1,
    "courseName": "자료구조",
    "title": "1주차 과제",
    "dueDate": "2025-03-15T23:59:59",
    "maxScore": 100,
    "submissionCount": 25,
    "createdAt": "2025-03-01T10:00:00"
  }
]
```

---

### 8. 과제 상세 조회

과제 상세 정보를 조회합니다.

### Request
```
GET /api/v1/assignments/{id}
```

### Response
```json
{
  "id": 1,
  "postId": 100,
  "courseId": 1,
  "courseName": "자료구조",
  "title": "1주차 과제",
  "content": "과제 설명입니다.",
  "dueDate": "2025-03-15T23:59:59",
  "maxScore": 100,
  "attachments": [
    {
      "attachmentId": 1,
      "originalName": "과제안내.pdf",
      "downloadUrl": "/api/v1/attachments/1/download"
    }
  ],
  "createdAt": "2025-03-01T10:00:00"
}
```

---

### 9. 과제 제출

과제를 제출합니다.

### Request
```
POST /api/v1/assignments/{id}/submit
```

### Request Body
```json
{
  "content": "과제 제출 내용입니다.",
  "attachmentIds": [10, 11]
}
```

### Response
```json
{
  "submissionId": 1,
  "assignmentId": 1,
  "studentId": 2025010001,
  "content": "과제 제출 내용입니다.",
  "attachments": [...],
  "submittedAt": "2025-03-10T15:30:00",
  "status": "SUBMITTED"
}
```

### Error Response
| 상태 코드 | 메시지 |
|-----------|--------|
| 400 | 마감 기한이 지났습니다. |
| 400 | 이미 제출한 과제입니다. |

---

### 10. 내 제출 조회

내가 제출한 과제를 조회합니다.

### Request
```
GET /api/v1/assignments/{id}/my-submission
```

### Response
```json
{
  "submissionId": 1,
  "assignmentId": 1,
  "content": "과제 제출 내용입니다.",
  "attachments": [...],
  "submittedAt": "2025-03-10T15:30:00",
  "score": 85,
  "maxScore": 100,
  "feedback": "잘 했습니다.",
  "status": "GRADED",
  "gradedAt": "2025-03-15T14:00:00"
}
```

### Response (미제출 시)
```json
{
  "submissionId": null,
  "status": "NOT_SUBMITTED",
  "message": "아직 제출하지 않았습니다."
}
```
