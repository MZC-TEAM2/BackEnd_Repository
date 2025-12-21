# Notification API

> 알림 API (커서 기반 페이징)

## 목차
- [1. 알림 목록 조회](#1-알림-목록-조회)
- [2. 알림 상세 조회](#2-알림-상세-조회)
- [3. 읽지 않은 알림 개수 조회](#3-읽지-않은-알림-개수-조회)
- [4. 알림 읽음 처리](#4-알림-읽음-처리)
- [5. 모든 알림 읽음 처리](#5-모든-알림-읽음-처리)
- [6. 알림 삭제](#6-알림-삭제)
- [7. 읽은 알림 일괄 삭제](#7-읽은-알림-일괄-삭제)
- [8. 모든 알림 삭제](#8-모든-알림-삭제)

---

## 1. 알림 목록 조회

알림 목록을 조회합니다. (커서 기반 페이징)

### Request
```
GET /api/notifications
```

### Headers
```
Authorization: Bearer {accessToken}
```

### Query Parameters
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| cursor | long | X | - | 커서 (이전 응답의 nextCursor) |
| size | int | X | 20 | 페이지 크기 (최대 100) |
| unreadOnly | boolean | X | false | 읽지 않은 알림만 조회 |

### Response
```json
{
  "notifications": [
    {
      "notificationId": 100,
      "type": "ASSIGNMENT_DUE",
      "title": "과제 마감 알림",
      "content": "자료구조 1주차 과제 마감이 1시간 남았습니다.",
      "isRead": false,
      "createdAt": "2025-01-15T13:00:00",
      "link": "/courses/1/assignments/1",
      "metadata": {
        "courseId": 1,
        "assignmentId": 1
      }
    },
    {
      "notificationId": 99,
      "type": "NEW_MESSAGE",
      "title": "새 메시지",
      "content": "김교수님이 메시지를 보냈습니다.",
      "isRead": true,
      "createdAt": "2025-01-15T12:30:00",
      "link": "/messages/conversations/5"
    }
  ],
  "nextCursor": 98,
  "hasNext": true,
  "totalUnread": 5
}
```

### Notification Types
| 타입 | 설명 |
|------|------|
| ASSIGNMENT_DUE | 과제 마감 알림 |
| ASSIGNMENT_GRADED | 과제 채점 완료 |
| NEW_MESSAGE | 새 메시지 |
| COURSE_NOTICE | 강의 공지 |
| ENROLLMENT_SUCCESS | 수강신청 성공 |
| ENROLLMENT_FAILED | 수강신청 실패 |
| SYSTEM | 시스템 알림 |

---

## 2. 알림 상세 조회

알림 상세 정보를 조회합니다.

### Request
```
GET /api/notifications/{notificationId}
```

### Response
```json
{
  "notificationId": 100,
  "type": "ASSIGNMENT_DUE",
  "title": "과제 마감 알림",
  "content": "자료구조 1주차 과제 마감이 1시간 남았습니다.",
  "isRead": false,
  "createdAt": "2025-01-15T13:00:00",
  "link": "/courses/1/assignments/1",
  "metadata": {
    "courseId": 1,
    "courseName": "자료구조",
    "assignmentId": 1,
    "assignmentTitle": "1주차 과제"
  }
}
```

---

## 3. 읽지 않은 알림 개수 조회

읽지 않은 알림의 개수를 조회합니다.

### Request
```
GET /api/notifications/unread-count
```

### Response
```json
{
  "unreadCount": 5
}
```

---

## 4. 알림 읽음 처리

특정 알림을 읽음 처리합니다.

### Request
```
PATCH /api/notifications/{notificationId}/read
```

### Response
```json
{
  "notificationId": 100,
  "type": "ASSIGNMENT_DUE",
  "title": "과제 마감 알림",
  "isRead": true,
  "readAt": "2025-01-15T14:00:00"
}
```

---

## 5. 모든 알림 읽음 처리

모든 알림을 읽음 처리합니다.

### Request
```
PATCH /api/notifications/read-all
```

### Response
```json
{
  "success": true,
  "message": "모든 알림이 읽음 처리되었습니다.",
  "updatedCount": 5
}
```

---

## 6. 알림 삭제

특정 알림을 삭제합니다.

### Request
```
DELETE /api/notifications/{notificationId}
```

### Response
```json
{
  "success": true,
  "message": "알림이 삭제되었습니다."
}
```

---

## 7. 읽은 알림 일괄 삭제

읽은 알림을 일괄 삭제합니다.

### Request
```
DELETE /api/notifications/read
```

### Response
```json
{
  "success": true,
  "message": "읽은 알림이 삭제되었습니다.",
  "deletedCount": 10
}
```

---

## 8. 모든 알림 삭제

모든 알림을 삭제합니다.

### Request
```
DELETE /api/notifications/all
```

### Response
```json
{
  "success": true,
  "message": "모든 알림이 삭제되었습니다.",
  "deletedCount": 25
}
```
