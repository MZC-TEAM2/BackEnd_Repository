# Message API

> 메시지/대화 API

## 목차

### 대화방
- [1. 대화방 목록 조회](#1-대화방-목록-조회)
- [2. 대화방 생성/조회](#2-대화방-생성조회)
- [3. 대화방 상세 조회](#3-대화방-상세-조회)
- [4. 대화방 삭제](#4-대화방-삭제)
- [5. 대화방 읽음 처리](#5-대화방-읽음-처리)
- [6. 전체 읽지 않은 메시지 수 조회](#6-전체-읽지-않은-메시지-수-조회)

### 메시지
- [7. 메시지 발송](#7-메시지-발송)
- [8. 메시지 일괄 발송](#8-메시지-일괄-발송)
- [9. 메시지 목록 조회](#9-메시지-목록-조회)
- [10. 메시지 삭제](#10-메시지-삭제)
- [11. 메시지 읽음 처리](#11-메시지-읽음-처리)

### 실시간 알림
- [12. SSE 구독](#12-sse-구독)

---

## 대화방 API

### 1. 대화방 목록 조회

내 대화방 목록을 조회합니다.

### Request
```
GET /api/v1/conversations
```

### Headers
```
Authorization: Bearer {accessToken}
```

### Response
```json
[
  {
    "conversationId": 1,
    "otherUserId": 2025010002,
    "otherUserName": "김철수",
    "otherUserProfileImageUrl": "...",
    "otherUserType": "STUDENT",
    "lastMessage": "네, 알겠습니다!",
    "lastMessageAt": "2025-01-15T14:30:00",
    "unreadCount": 3
  }
]
```

---

### 2. 대화방 생성/조회

특정 사용자와의 대화방을 생성하거나 기존 대화방을 조회합니다.

### Request
```
POST /api/v1/conversations/with/{otherUserId}
```

### Path Parameters
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| otherUserId | long | 상대방 사용자 ID |

### Response
```json
{
  "conversationId": 1,
  "otherUserId": 2025010002,
  "otherUserName": "김철수",
  "otherUserProfileImageUrl": "...",
  "otherUserType": "STUDENT",
  "createdAt": "2025-01-15T10:00:00"
}
```

---

### 3. 대화방 상세 조회

대화방 상세 정보를 조회합니다.

### Request
```
GET /api/v1/conversations/{conversationId}
```

### Response
```json
{
  "conversationId": 1,
  "otherUserId": 2025010002,
  "otherUserName": "김철수",
  "otherUserProfileImageUrl": "...",
  "otherUserType": "STUDENT",
  "createdAt": "2025-01-15T10:00:00"
}
```

---

### 4. 대화방 삭제

대화방을 삭제합니다. (본인 기준으로만 삭제)

### Request
```
DELETE /api/v1/conversations/{conversationId}
```

### Response
```
HTTP/1.1 204 No Content
```

---

### 5. 대화방 읽음 처리

대화방의 모든 메시지를 읽음 처리합니다.

### Request
```
POST /api/v1/conversations/{conversationId}/read
```

### Response
```
HTTP/1.1 200 OK
```

---

### 6. 전체 읽지 않은 메시지 수 조회

모든 대화방의 읽지 않은 메시지 총 개수를 조회합니다.

### Request
```
GET /api/v1/conversations/unread-count
```

### Response
```json
5
```

---

## 메시지 API

### 7. 메시지 발송

메시지를 발송합니다.

### Request
```
POST /api/v1/messages
```

### Request Body
```json
{
  "conversationId": 1,
  "content": "안녕하세요!"
}
```

### Response
```json
{
  "messageId": 100,
  "conversationId": 1,
  "senderId": 2025010001,
  "senderName": "홍길동",
  "content": "안녕하세요!",
  "sentAt": "2025-01-15T14:30:00",
  "isRead": false
}
```

---

### 8. 메시지 일괄 발송

여러 사용자에게 동일한 메시지를 발송합니다.

### Request
```
POST /api/v1/messages/bulk
```

### Request Body
```json
{
  "receiverIds": [2025010002, 2025010003, 2025010004],
  "content": "공지사항입니다."
}
```

### Response
```json
[
  {
    "messageId": 101,
    "conversationId": 1,
    "receiverId": 2025010002,
    "content": "공지사항입니다.",
    "sentAt": "2025-01-15T14:30:00"
  },
  {
    "messageId": 102,
    "conversationId": 2,
    "receiverId": 2025010003,
    "content": "공지사항입니다.",
    "sentAt": "2025-01-15T14:30:00"
  }
]
```

---

### 9. 메시지 목록 조회

대화방의 메시지 목록을 조회합니다. (커서 기반 페이징)

### Request
```
GET /api/v1/messages/conversations/{conversationId}
```

### Query Parameters
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| cursor | long | X | - | 커서 (이전 응답의 nextCursor) |
| size | int | X | 20 | 페이지 크기 |

### Response
```json
{
  "messages": [
    {
      "messageId": 100,
      "senderId": 2025010001,
      "senderName": "홍길동",
      "senderProfileImageUrl": "...",
      "content": "안녕하세요!",
      "sentAt": "2025-01-15T14:30:00",
      "isRead": true,
      "isMine": true
    },
    {
      "messageId": 99,
      "senderId": 2025010002,
      "senderName": "김철수",
      "content": "네, 안녕하세요!",
      "sentAt": "2025-01-15T14:25:00",
      "isRead": true,
      "isMine": false
    }
  ],
  "nextCursor": 98,
  "hasNext": true
}
```

---

### 10. 메시지 삭제

메시지를 삭제합니다. (본인이 보낸 메시지만 삭제 가능)

### Request
```
DELETE /api/v1/messages/{messageId}
```

### Response
```
HTTP/1.1 204 No Content
```

---

### 11. 메시지 읽음 처리

대화방의 메시지들을 읽음 처리합니다.

### Request
```
POST /api/v1/messages/conversations/{conversationId}/read
```

### Response
```
HTTP/1.1 200 OK
```

---

## 실시간 알림 API

### 12. SSE 구독

Server-Sent Events를 구독하여 실시간 알림을 받습니다.

### Request
```
GET /api/v1/sse/subscribe
```

### Headers
```
Authorization: Bearer {accessToken}
Accept: text/event-stream
```

### Response (Event Stream)
```
event: message
data: {"type":"NEW_MESSAGE","conversationId":1,"messageId":100,"senderName":"홍길동","content":"안녕하세요!"}

event: notification
data: {"type":"ASSIGNMENT_DUE","title":"과제 마감 알림","content":"자료구조 과제 마감이 1시간 남았습니다."}
```

### Event Types
| 이벤트 | 설명 |
|--------|------|
| message | 새 메시지 도착 |
| notification | 시스템 알림 |
| read | 메시지 읽음 처리됨 |

### 연결 유지
- 기본 타임아웃: 30분
- 클라이언트는 연결이 끊어지면 자동 재연결 필요
- 주기적으로 heartbeat 이벤트 발송
