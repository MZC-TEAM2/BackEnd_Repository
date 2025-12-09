# Board Domain API 명세서

## 📌 개요
LMS 게시판 시스템의 REST API 명세서입니다.

**Base URL:** `http://localhost:8080/api/v1`

**인증:** JWT Bearer Token 필요 (일부 조회 API 제외)

---

## 🔧 공통 응답 형식

### 성공 응답
```json
{
  "status": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다",
  "data": { /* 실제 데이터 */ }
}
```

### 에러 응답
```json
{
  "status": "ERROR",
  "message": "에러 메시지",
  "errorCode": "BOARD_NOT_FOUND",
  "timestamp": "2024-12-04T10:00:00"
}
```

---

## 📝 1. 게시글 기본 CRUD API

### 1.1 게시글 목록 조회
**GET** `/boards/{boardType}/posts`

#### Parameters
| 이름 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| boardType | String | ✅ | 게시판 유형 | FREE, NOTICE, QUESTION |
| page | Integer | ❌ | 페이지 번호 (0부터 시작) | 0 |
| size | Integer | ❌ | 페이지 크기 (기본값: 20) | 10 |
| sort | String | ❌ | 정렬 기준 | createdAt,desc |
| search | String | ❌ | 검색어 (제목+내용) | "Spring Boot" |

#### 게시판 유형 (BoardType)
- `NOTICE` - 학교 공지사항
- `FREE` - 자유 게시판
- `QUESTION` - 질문 게시판  
- `DISCUSSION` - 토론 게시판
- `PROFESSOR` - 교수 게시판 (교수만 접근)
- `STUDENT` - 학생 게시판 (학생만 접근)
- `DEPARTMENT` - 학과 게시판
- `CONTEST` - 공모전 게시판
- `CAREER` - 취업 게시판
- `ASSIGNMENT` - 과제 게시판
- `EXAM` - 시험 게시판
- `QUIZ` - 퀴즈 게시판
- `STUDY_RECRUITMENT` - 스터디모집 게시판

#### 요청 예시
```http
GET /api/boards/FREE/posts?page=0&size=10&sort=createdAt,desc&search=자바
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "message": "게시글 목록 조회 성공",
  "data": {
    "content": [
      {
        "id": 123,
        "title": "Spring Boot 질문있습니다",
        "content": "안녕하세요, Spring Boot 관련해서...",
        "authorId": 456,
        "authorName": "김학생",
        "isAnonymous": false,
        "postType": "FREE",
        "categoryName": "자유게시판",
        "viewCount": 45,
        "likeCount": 8,
        "commentCount": 3,
        "hasAttachments": true,
        "hashtags": ["#스프링부트", "#백엔드"],
        "createdAt": "2024-12-04T09:30:00",
        "updatedAt": "2024-12-04T10:15:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "totalElements": 156,
      "totalPages": 16
    }
  }
}
```

---

### 1.2 게시글 상세 조회
**GET** `/boards/{boardType}/posts/{postId}`

#### Parameters
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| boardType | String | ✅ | 게시판 유형 |
| postId | Long | ✅ | 게시글 ID |

#### 요청 예시
```http
GET /api/boards/FREE/posts/123
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "message": "게시글 조회 성공",
  "data": {
    "id": 123,
    "title": "Spring Boot 질문있습니다",
    "content": "안녕하세요, Spring Boot 관련해서 궁금한 점이 있어서 질문드립니다...",
    "authorId": 456,
    "authorName": "김학생",
    "isAnonymous": false,
    "postType": "FREE",
    "categoryId": 1,
    "categoryName": "자유게시판",
    "courseId": null,
    "departmentId": null,
    "viewCount": 46,
    "likeCount": 8,
    "commentCount": 3,
    "isLikedByCurrentUser": false,
    "isBookmarkedByCurrentUser": true,
    "hashtags": [
      {
        "id": 10,
        "name": "스프링부트",
        "displayName": "#스프링부트",
        "color": "#007bff"
      }
    ],
    "attachments": [
      {
        "id": 789,
        "originalName": "error_screenshot.png",
        "fileSize": 1024000,
        "attachmentType": "POST_CONTENT",
        "downloadUrl": "/api/attachments/789/download"
      }
    ],
    "createdAt": "2024-12-04T09:30:00",
    "updatedAt": "2024-12-04T10:15:00"
  }
}
```

---

### 1.3 게시글 작성
**POST** `/boards/{boardType}/posts`

#### Parameters
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| boardType | String | ✅ | 게시판 유형 |

#### 요청 Body
```json
{
  "title": "새로운 게시글 제목",
  "content": "게시글 내용입니다. 마크다운 형식 지원합니다.",
  "isAnonymous": false,
  "courseId": null,
  "departmentId": null,
  "hashtags": ["스프링부트", "백엔드"],
  "attachmentIds": [101, 102]
}
```

#### 요청 필드 설명
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | String | ✅ | 제목 (1-255자) |
| content | String | ✅ | 내용 (마크다운 지원) |
| isAnonymous | Boolean | ❌ | 익명 작성 여부 (기본: false) |
| courseId | Long | ❌ | 강의 ID (질문 게시판용) |
| departmentId | Long | ❌ | 학과 ID (학과 게시판용) |
| hashtags | String[] | ❌ | 해시태그 목록 |
| attachmentIds | Long[] | ❌ | 첨부파일 ID 목록 |

#### 요청 예시
```http
POST /api/boards/FREE/posts
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "title": "Spring Boot 질문있습니다",
  "content": "안녕하세요, Spring Boot 관련해서...",
  "isAnonymous": false,
  "hashtags": ["스프링부트", "백엔드"]
}
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "message": "게시글이 성공적으로 작성되었습니다",
  "data": {
    "id": 124,
    "title": "Spring Boot 질문있습니다",
    "authorId": 456,
    "createdAt": "2024-12-04T11:00:00"
  }
}
```

---

### 1.4 게시글 수정
**PUT** `/boards/{boardType}/posts/{postId}`

#### Parameters
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| boardType | String | ✅ | 게시판 유형 |
| postId | Long | ✅ | 게시글 ID |

#### 요청 Body
```json
{
  "title": "수정된 게시글 제목",
  "content": "수정된 게시글 내용",
  "hashtags": ["스프링부트", "JPA"],
  "attachmentIds": [101, 103]
}
```

#### 권한
- 작성자 본인만 수정 가능
- 관리자는 모든 게시글 수정 가능

#### 요청 예시
```http
PUT /api/boards/FREE/posts/123
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "title": "Spring Boot JPA 질문 (수정)",
  "content": "수정된 내용입니다...",
  "hashtags": ["스프링부트", "JPA"]
}
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "message": "게시글이 성공적으로 수정되었습니다",
  "data": {
    "id": 123,
    "updatedAt": "2024-12-04T11:30:00"
  }
}
```

---

### 1.5 게시글 삭제
**DELETE** `/boards/{boardType}/posts/{postId}`

#### Parameters
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| boardType | String | ✅ | 게시판 유형 |
| postId | Long | ✅ | 게시글 ID |

#### 권한
- 작성자 본인만 삭제 가능
- 관리자는 모든 게시글 삭제 가능

#### 삭제 방식
- **Soft Delete**: `is_deleted = true` + `deleted_at` 시간 기록 (하이브리드 방식)
- 삭제된 게시글은 일반 조회에서 제외 (`is_deleted = false` 조건)
- 관리자는 삭제된 게시글 복구 가능
- **성능 최적화**: `is_deleted` boolean으로 빠른 필터링

#### 요청 예시
```http
DELETE /api/boards/FREE/posts/123
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "message": "게시글이 성공적으로 삭제되었습니다",
  "data": {
    "id": 123,
    "isDeleted": true,
    "deletedAt": "2024-12-04T12:00:00"
  }
}
```

---

## 💬 2. 댓글 시스템 API

### 2.1 댓글 목록 조회
**GET** `/boards/{boardType}/posts/{postId}/comments`

#### Parameters
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| boardType | String | ✅ | 게시판 유형 |
| postId | Long | ✅ | 게시글 ID |
| page | Integer | ❌ | 페이지 번호 (기본값: 0) |
| size | Integer | ❌ | 페이지 크기 (기본값: 50) |
| sort | String | ❌ | 정렬 기준 (createdAt,asc) |

#### 요청 예시
```http
GET /api/boards/FREE/posts/123/comments?page=0&size=20&sort=createdAt,asc
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "message": "댓글 목록 조회 성공",
  "data": {
    "content": [
      {
        "id": 456,
        "postId": 123,
        "authorId": 789,
        "authorName": "김학생",
        "content": "좋은 정보 감사합니다!",
        "depth": 0,
        "parentCommentId": null,
        "isAnonymous": false,
        "likeCount": 3,
        "isLikedByCurrentUser": false,
        "hasAttachments": false,
        "createdAt": "2024-12-04T10:00:00",
        "updatedAt": null,
        "replies": [
          {
            "id": 457,
            "parentCommentId": 456,
            "authorName": "박교수",
            "content": "추가 자료도 업로드했습니다.",
            "depth": 1,
            "isAnonymous": false,
            "createdAt": "2024-12-04T10:30:00"
          }
        ]
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "totalElements": 12,
      "totalPages": 1
    }
  }
}
```

---

### 2.2 댓글 작성
**POST** `/boards/{boardType}/posts/{postId}/comments`

#### Parameters
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| boardType | String | ✅ | 게시판 유형 |
| postId | Long | ✅ | 게시글 ID |

#### 요청 Body
```json
{
  "content": "댓글 내용입니다. 마크다운도 지원합니다.",
  "parentCommentId": null,
  "isAnonymous": false,
  "attachmentIds": [201, 202]
}
```

#### 요청 필드 설명
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| content | String | ✅ | 댓글 내용 (1-1000자) |
| parentCommentId | Long | ❌ | 부모 댓글 ID (대댓글용) |
| isAnonymous | Boolean | ❌ | 익명 댓글 여부 (기본: false) |
| attachmentIds | Long[] | ❌ | 첨부파일 ID 목록 |

#### 권한 체크
- 해당 게시판에 댓글 작성 권한이 있는 사용자만 가능
- 학교 공지사항은 댓글 작성 불가

#### 요청 예시
```http
POST /api/boards/FREE/posts/123/comments
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "content": "좋은 정보 감사합니다!",
  "isAnonymous": false
}
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "message": "댓글이 성공적으로 작성되었습니다",
  "data": {
    "id": 458,
    "postId": 123,
    "authorId": 789,
    "depth": 0,
    "createdAt": "2024-12-04T11:00:00"
  }
}
```

---

### 2.3 대댓글 작성
**POST** `/boards/{boardType}/posts/{postId}/comments/{parentCommentId}/replies`

#### Parameters
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| boardType | String | ✅ | 게시판 유형 |
| postId | Long | ✅ | 게시글 ID |
| parentCommentId | Long | ✅ | 부모 댓글 ID |

#### 요청 Body
```json
{
  "content": "대댓글 내용입니다.",
  "isAnonymous": false
}
```

#### 제약사항
- 대댓글의 depth는 최대 1로 제한 (대대댓글 불가)
- 삭제된 댓글에는 대댓글 작성 불가

#### 요청 예시
```http
POST /api/boards/FREE/posts/123/comments/456/replies
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "content": "@김학생 추가 설명드리자면...",
  "isAnonymous": false
}
```

---

### 2.4 댓글 수정
**PUT** `/boards/{boardType}/posts/{postId}/comments/{commentId}`

#### Parameters
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| boardType | String | ✅ | 게시판 유형 |
| postId | Long | ✅ | 게시글 ID |
| commentId | Long | ✅ | 댓글 ID |

#### 요청 Body
```json
{
  "content": "수정된 댓글 내용입니다.",
  "attachmentIds": [201, 203]
}
```

#### 권한
- 댓글 작성자 본인만 수정 가능
- 관리자는 모든 댓글 수정 가능

#### 요청 예시
```http
PUT /api/boards/FREE/posts/123/comments/456
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "content": "수정된 댓글 내용입니다."
}
```

---

### 2.5 댓글 삭제
**DELETE** `/boards/{boardType}/posts/{postId}/comments/{commentId}`

#### Parameters
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| boardType | String | ✅ | 게시판 유형 |
| postId | Long | ✅ | 게시글 ID |
| commentId | Long | ✅ | 댓글 ID |

#### 삭제 정책
- **Soft Delete**: `is_deleted = true` + `deleted_at` 시간 기록 (하이브리드 방식)
- **대댓글이 있는 댓글**: "삭제된 댓글입니다" 표시, 구조 유지
- **대댓글이 없는 댓글**: 완전히 숨김 처리
- **성능 최적화**: `is_deleted` boolean으로 빠른 필터링

#### 권한
- 댓글 작성자 본인만 삭제 가능
- 관리자는 모든 댓글 삭제 가능

#### 요청 예시
```http
DELETE /api/boards/FREE/posts/123/comments/456
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "message": "댓글이 성공적으로 삭제되었습니다",
  "data": {
    "id": 456,
    "isDeleted": true,
    "deletedAt": "2024-12-04T12:00:00"
  }
}
```

---

## 📎 3. 첨부파일 시스템 API

### 3.1 파일 업로드
**POST** `/attachments/upload`

#### Content-Type
`multipart/form-data`

#### Form Data
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| file | File | ✅ | 업로드할 파일 |
| attachmentType | String | ✅ | 첨부 유형 (POST_CONTENT/POST_BOTTOM/COMMENT) |
| description | String | ❌ | 파일 설명 |

#### 첨부파일 유형 (AttachmentType)
- `POST_CONTENT` - 게시글 본문 이미지
- `POST_BOTTOM` - 게시글 하단 첨부파일  
- `COMMENT` - 댓글 첨부파일

#### 파일 제한사항
- **최대 파일 크기**: 50MB
- **허용 이미지**: jpg, jpeg, png, gif, webp
- **허용 문서**: pdf, doc, docx, ppt, pptx, xls, xlsx, hwp
- **허용 압축**: zip, rar, 7z
- **허용 코드**: txt, md, java, js, py, cpp, c, h

#### 요청 예시
```http
POST /api/attachments/upload
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="file"; filename="screenshot.png"
Content-Type: image/png

[파일 바이너리 데이터]
--boundary
Content-Disposition: form-data; name="attachmentType"

POST_CONTENT
--boundary--
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "message": "파일이 성공적으로 업로드되었습니다",
  "data": {
    "id": 301,
    "originalName": "screenshot.png",
    "storedName": "2024/12/04/uuid-generated-name.png",
    "filePath": "/uploads/2024/12/04/uuid-generated-name.png",
    "fileSize": 1024567,
    "mimeType": "image/png",
    "attachmentType": "POST_CONTENT",
    "downloadUrl": "/api/attachments/301/download",
    "previewUrl": "/api/attachments/301/preview",
    "uploadedAt": "2024-12-04T11:00:00"
  }
}
```

---

### 3.2 다중 파일 업로드
**POST** `/attachments/upload/multiple`

#### Content-Type
`multipart/form-data`

#### Form Data
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| files | File[] | ✅ | 업로드할 파일들 (최대 10개) |
| attachmentType | String | ✅ | 첨부 유형 |

#### 요청 예시
```http
POST /api/attachments/upload/multiple
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="files"; filename="file1.jpg"
[파일1 데이터]
--boundary
Content-Disposition: form-data; name="files"; filename="file2.pdf"
[파일2 데이터]
--boundary--
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "message": "2개 파일이 성공적으로 업로드되었습니다",
  "data": {
    "uploadedFiles": [
      {
        "id": 302,
        "originalName": "file1.jpg",
        "fileSize": 2048000,
        "downloadUrl": "/api/attachments/302/download"
      },
      {
        "id": 303,
        "originalName": "file2.pdf", 
        "fileSize": 5120000,
        "downloadUrl": "/api/attachments/303/download"
      }
    ],
    "failedFiles": []
  }
}
```

---

### 3.3 파일 다운로드
**GET** `/attachments/{attachmentId}/download`

#### Parameters
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| attachmentId | Long | ✅ | 첨부파일 ID |

#### 다운로드 추적
- 다운로드 시마다 `attachment_downloads` 테이블에 기록
- 사용자 ID, IP 주소, 다운로드 시간 저장
- 다운로드 횟수 통계 제공

#### 요청 예시
```http
GET /api/attachments/301/download
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답
- **Content-Type**: 파일의 원본 MIME 타입
- **Content-Disposition**: `attachment; filename="원본파일명"`
- **Body**: 파일 바이너리 데이터

---

### 3.4 이미지 미리보기
**GET** `/attachments/{attachmentId}/preview`

#### Parameters
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| attachmentId | Long | ✅ | 첨부파일 ID |
| width | Integer | ❌ | 미리보기 너비 (기본값: 800) |
| height | Integer | ❌ | 미리보기 높이 (기본값: 600) |
| quality | Integer | ❌ | 이미지 품질 (1-100, 기본값: 80) |

#### 지원 형식
- 이미지 파일만 미리보기 지원
- 자동 리사이징 및 압축
- 썸네일 캐싱으로 성능 최적화

#### 요청 예시
```http
GET /api/attachments/301/preview?width=400&height=300&quality=70
```

#### 응답
- **Content-Type**: `image/jpeg` (변환됨)
- **Body**: 리사이즈된 이미지 데이터

---

### 3.5 첨부파일 정보 조회
**GET** `/attachments/{attachmentId}`

#### Parameters
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| attachmentId | Long | ✅ | 첨부파일 ID |

#### 요청 예시
```http
GET /api/attachments/301
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "message": "첨부파일 정보 조회 성공",
  "data": {
    "id": 301,
    "originalName": "screenshot.png",
    "storedName": "2024/12/04/uuid-generated-name.png",
    "fileSize": 1024567,
    "mimeType": "image/png",
    "attachmentType": "POST_CONTENT",
    "postId": 123,
    "commentId": null,
    "uploaderId": 456,
    "uploaderName": "김학생",
    "downloadCount": 45,
    "downloadUrl": "/api/attachments/301/download",
    "previewUrl": "/api/attachments/301/preview",
    "uploadedAt": "2024-12-04T11:00:00"
  }
}
```

---

### 3.6 첨부파일 삭제
**DELETE** `/attachments/{attachmentId}`

#### Parameters
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| attachmentId | Long | ✅ | 첨부파일 ID |

#### 권한
- 파일 업로더 본인만 삭제 가능
- 게시글/댓글 작성자도 삭제 가능
- 관리자는 모든 파일 삭제 가능

#### 삭제 정책
- **Soft Delete**: `deleted_at` 필드 업데이트
- **물리적 파일**: 관리자가 별도로 정리
- **연결 해제**: 게시글/댓글에서 자동 제거

#### 요청 예시
```http
DELETE /api/attachments/301
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "message": "첨부파일이 성공적으로 삭제되었습니다",
  "data": {
    "id": 301,
    "deletedAt": "2024-12-04T12:00:00"
  }
}
```

---

## ❌ 공통 에러 코드

| 에러 코드 | HTTP 상태 | 설명 |
|-----------|-----------|------|
| `BOARD_NOT_FOUND` | 404 | 게시판을 찾을 수 없음 |
| `POST_NOT_FOUND` | 404 | 게시글을 찾을 수 없음 |
| `COMMENT_NOT_FOUND` | 404 | 댓글을 찾을 수 없음 |
| `ATTACHMENT_NOT_FOUND` | 404 | 첨부파일을 찾을 수 없음 |
| `ACCESS_DENIED` | 403 | 접근 권한 없음 |
| `UNAUTHORIZED` | 401 | 인증 필요 |
| `INVALID_BOARD_TYPE` | 400 | 잘못된 게시판 유형 |
| `VALIDATION_ERROR` | 400 | 입력값 검증 실패 |
| `FILE_SIZE_EXCEEDED` | 400 | 파일 크기 초과 |
| `UNSUPPORTED_FILE_TYPE` | 400 | 지원하지 않는 파일 형식 |
| `COMMENT_DEPTH_EXCEEDED` | 400 | 댓글 깊이 초과 (대댓글만 허용) |
| `COMMENTS_NOT_ALLOWED` | 400 | 댓글이 허용되지 않는 게시판 |

---

## 📚 관련 API 명세서

### 완성된 API 섹션
1. **📝 게시글 기본 CRUD API** (이 파일)
2. **💬 댓글 시스템 API** (이 파일)  
3. **📎 첨부파일 시스템 API** (이 파일)
4. **✅ 고급 기능 통합 API** → [API_SPECIFICATION_ADVANCED_FEATURES.md](./API_SPECIFICATION_ADVANCED_FEATURES.md) 🚀

### 🎯 완성된 LMS 게시판 시스템

**모든 핵심 기능이 완성되었습니다!** 🎉

통합된 고급 기능:
- ✅ **좋아요/북마크 시스템** (8 APIs)
- ✅ **과제/시험 관리** (13 APIs)  
- ✅ **스터디 모집** (7 APIs)
- ✅ **해시태그 시스템** (7 APIs)
- ✅ **통합 분석** (2 APIs)

**총 51개 API**로 구성된 완전한 교육용 소셜 플랫폼

각 기능별로 별도 파일로 관리하여 1000줄 제한을 준수합니다! 🎯