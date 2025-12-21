# Board API

> 게시판 API (게시글, 댓글, 첨부파일)

## 목차

### 게시글
- [1. 게시글 생성](#1-게시글-생성)
- [2. 게시글 상세 조회](#2-게시글-상세-조회)
- [3. 게시글 목록 조회](#3-게시글-목록-조회)
- [4. 게시글 수정](#4-게시글-수정)
- [5. 게시글 삭제](#5-게시글-삭제)
- [6. 게시글 좋아요 토글](#6-게시글-좋아요-토글)
- [7. 게시글 좋아요 여부 조회](#7-게시글-좋아요-여부-조회)

### 댓글
- [8. 댓글 생성](#8-댓글-생성)
- [9. 댓글 목록 조회](#9-댓글-목록-조회)
- [10. 댓글 수정](#10-댓글-수정)
- [11. 댓글 삭제](#11-댓글-삭제)

### 첨부파일
- [12. 단일 파일 업로드](#12-단일-파일-업로드)
- [13. 다중 파일 업로드](#13-다중-파일-업로드)
- [14. 파일 다운로드](#14-파일-다운로드)
- [15. 첨부파일 삭제](#15-첨부파일-삭제)

---

## 게시판 타입

| 타입 | 설명 |
|------|------|
| notice | 공지사항 |
| free | 자유게시판 |
| qna | 질문게시판 |
| course | 강의 게시판 |

---

## 게시글 API

### 1. 게시글 생성

새로운 게시글을 작성합니다.

### Request
```
POST /api/v1/boards/{boardType}/posts
```

### Path Parameters
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| boardType | string | 게시판 타입 |

### Request Body
```json
{
  "title": "게시글 제목",
  "content": "게시글 내용",
  "hashtags": ["태그1", "태그2"],
  "attachmentIds": [1, 2, 3]
}
```

### Response
```json
{
  "postId": 1,
  "title": "게시글 제목",
  "content": "게시글 내용",
  "authorId": 2025010001,
  "authorName": "홍길동",
  "boardType": "free",
  "hashtags": ["태그1", "태그2"],
  "attachments": [...],
  "viewCount": 0,
  "likeCount": 0,
  "commentCount": 0,
  "createdAt": "2025-01-15T10:00:00"
}
```

---

### 2. 게시글 상세 조회

게시글 상세 정보를 조회합니다.

### Request
```
GET /api/v1/boards/{boardType}/posts/{id}
```

### Response
```json
{
  "postId": 1,
  "title": "게시글 제목",
  "content": "게시글 내용",
  "authorId": 2025010001,
  "authorName": "홍길동",
  "authorProfileImageUrl": "...",
  "boardType": "free",
  "hashtags": ["태그1", "태그2"],
  "attachments": [
    {
      "attachmentId": 1,
      "originalName": "file.pdf",
      "fileSize": 1024,
      "downloadUrl": "/api/v1/attachments/1/download"
    }
  ],
  "viewCount": 100,
  "likeCount": 10,
  "commentCount": 5,
  "isLiked": false,
  "isAuthor": true,
  "createdAt": "2025-01-15T10:00:00",
  "updatedAt": "2025-01-15T10:00:00"
}
```

---

### 3. 게시글 목록 조회

게시글 목록을 조회합니다. (검색, 해시태그 필터링 지원)

### Request
```
GET /api/v1/boards/{boardType}/posts
```

### Query Parameters
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| search | string | X | - | 검색어 (제목, 내용) |
| hashtag | string | X | - | 해시태그 필터 |
| page | int | X | 0 | 페이지 번호 |
| size | int | X | 20 | 페이지 크기 |
| sort | string | X | createdAt,DESC | 정렬 기준 |

### Response
```json
{
  "content": [
    {
      "postId": 1,
      "title": "게시글 제목",
      "authorName": "홍길동",
      "boardType": "free",
      "hashtags": ["태그1"],
      "viewCount": 100,
      "likeCount": 10,
      "commentCount": 5,
      "hasAttachment": true,
      "createdAt": "2025-01-15T10:00:00"
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "number": 0,
  "size": 20
}
```

---

### 4. 게시글 수정

게시글을 수정합니다.

### Request (JSON)
```
PUT /api/v1/boards/posts/{id}
Content-Type: application/json
```

### Request (Multipart - 파일 포함)
```
PUT /api/v1/boards/posts/{id}
Content-Type: multipart/form-data
```

### Request Body
```json
{
  "title": "수정된 제목",
  "content": "수정된 내용",
  "hashtags": ["태그1", "태그3"],
  "attachmentIds": [1, 4],
  "deleteAttachmentIds": [2, 3]
}
```

---

### 5. 게시글 삭제

게시글을 삭제합니다.

### Request
```
DELETE /api/v1/boards/posts/{id}
```

### Response
```
HTTP/1.1 204 No Content
```

---

### 6. 게시글 좋아요 토글

게시글 좋아요를 토글합니다.

### Request
```
POST /api/v1/boards/posts/{id}/like?userId={userId}
```

### Response
```json
{
  "liked": true,
  "message": "좋아요가 등록되었습니다."
}
```

---

### 7. 게시글 좋아요 여부 조회

게시글 좋아요 여부를 확인합니다.

### Request
```
GET /api/v1/boards/posts/{id}/liked?userId={userId}
```

### Response
```json
{
  "liked": true
}
```

---

## 댓글 API

### 8. 댓글 생성

댓글을 작성합니다. (대댓글 지원)

### Request
```
POST /api/v1/board/comments
```

### Request Body
```json
{
  "postId": 1,
  "content": "댓글 내용",
  "parentCommentId": null
}
```

> `parentCommentId`를 지정하면 대댓글로 작성됩니다.

### Response
```json
{
  "commentId": 1,
  "postId": 1,
  "content": "댓글 내용",
  "authorId": 2025010001,
  "authorName": "홍길동",
  "authorProfileImageUrl": "...",
  "parentCommentId": null,
  "depth": 0,
  "createdAt": "2025-01-15T10:30:00"
}
```

---

### 9. 댓글 목록 조회

게시글의 댓글 목록을 조회합니다.

### Request
```
GET /api/v1/board/comments?postId={postId}
```

### Response
```json
[
  {
    "commentId": 1,
    "content": "댓글 내용",
    "authorName": "홍길동",
    "authorProfileImageUrl": "...",
    "depth": 0,
    "createdAt": "2025-01-15T10:30:00",
    "replies": [
      {
        "commentId": 2,
        "content": "대댓글 내용",
        "authorName": "김철수",
        "depth": 1,
        "createdAt": "2025-01-15T10:35:00"
      }
    ]
  }
]
```

---

### 10. 댓글 수정

댓글을 수정합니다.

### Request
```
PUT /api/v1/board/comments/{id}
```

### Request Body
```json
{
  "content": "수정된 댓글 내용"
}
```

---

### 11. 댓글 삭제

댓글을 삭제합니다.

### Request
```
DELETE /api/v1/board/comments/{id}
```

### Response
```
HTTP/1.1 204 No Content
```

---

## 첨부파일 API

### 12. 단일 파일 업로드

파일을 업로드합니다.

### Request
```
POST /api/v1/attachments/upload
Content-Type: multipart/form-data
```

### Form Data
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| file | file | O | 업로드할 파일 |
| attachmentType | string | O | 첨부 유형 (POST, ASSIGNMENT, PROFILE) |

### Response
```json
{
  "status": "SUCCESS",
  "message": "파일이 업로드되었습니다.",
  "data": {
    "attachmentId": 1,
    "originalName": "document.pdf",
    "storedName": "uuid-document.pdf",
    "fileSize": 1024000,
    "mimeType": "application/pdf",
    "downloadUrl": "/api/v1/attachments/1/download"
  }
}
```

---

### 13. 다중 파일 업로드

여러 파일을 한 번에 업로드합니다.

### Request
```
POST /api/v1/attachments/upload/multiple
Content-Type: multipart/form-data
```

### Form Data
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| files | file[] | O | 업로드할 파일들 |
| attachmentType | string | O | 첨부 유형 |

### Response
```json
{
  "status": "SUCCESS",
  "message": "3개 파일이 업로드되었습니다.",
  "data": [
    {
      "attachmentId": 1,
      "originalName": "file1.pdf",
      ...
    }
  ]
}
```

---

### 14. 파일 다운로드

첨부파일을 다운로드합니다.

### Request
```
GET /api/v1/attachments/{attachmentId}/download
```

### Response
```
Content-Type: application/pdf
Content-Disposition: attachment; filename="document.pdf"

[Binary Data]
```

---

### 15. 첨부파일 삭제

첨부파일을 삭제합니다.

### Request
```
DELETE /api/v1/attachments/{attachmentId}
```

### Response
```json
{
  "status": "SUCCESS",
  "message": "파일이 삭제되었습니다."
}
```
