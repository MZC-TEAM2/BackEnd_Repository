# LMS API Specification

> Learning Management System Backend API 문서

## 목차

| 도메인 | 설명 | 문서 |
|--------|------|------|
| [Auth](./auth.md) | 인증/인가 (회원가입, 로그인, 토큰) | 7개 API |
| [Profile](./profile.md) | 프로필 관리 (조회, 수정, 이미지) | 4개 API |
| [User Search](./user-search.md) | 유저 검색 (학생/교수 탐색) | 4개 API |
| [Course](./course.md) | 강의 관리 (조회, 개설, 주차/콘텐츠) | 16개 API |
| [Subject](./subject.md) | 과목 관리 (목록, 상세, 검색) | 3개 API |
| [Enrollment](./enrollment.md) | 수강신청 (장바구니, 신청, 취소) | 9개 API |
| [Attendance](./attendance.md) | 출석 관리 (학생/교수) | 6개 API |
| [Board](./board.md) | 게시판 (게시글, 댓글, 첨부파일) | 15개 API |
| [Assignment](./assignment.md) | 과제 (등록, 제출, 채점) | 10개 API |
| [Message](./message.md) | 메시지 (대화방, 메시지, SSE) | 12개 API |
| [Notification](./notification.md) | 알림 (조회, 읽음처리, 삭제) | 8개 API |
| [Dashboard](./dashboard.md) | 대시보드 (학생용) | 4개 API |

---

## 공통 사항

### Base URL
```
/api/v1/...
/api/auth/...  (인증 관련)
/api/notifications/...  (알림)
```

### 인증 방식
- JWT Bearer Token
- Header: `Authorization: Bearer {accessToken}`
- 토큰 갱신: `Refresh-Token: {refreshToken}`

### 공통 응답 형식

#### 성공 응답
```json
{
  "success": true,
  "data": { ... },
  "message": "처리 완료"
}
```

#### 에러 응답
```json
{
  "success": false,
  "message": "에러 메시지"
}
```

### HTTP 상태 코드
| 코드 | 설명 |
|------|------|
| 200 | 성공 |
| 201 | 생성 성공 |
| 204 | 삭제 성공 (No Content) |
| 400 | 잘못된 요청 |
| 401 | 인증 필요 |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 500 | 서버 에러 |

### 사용자 타입
| 타입 | 설명 |
|------|------|
| STUDENT | 학생 |
| PROFESSOR | 교수 |

---

## Quick Reference

##  API 엔드포인트

### 1 인증 API (`/api/auth`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /signup/email-verification | 이메일 인증 코드 발송 |
| POST | /signup/verify-code | 인증 코드 확인 |
| POST | /signup | 회원가입 |
| POST | /login | 로그인 |
| POST | /refresh | 토큰 갱신 |
| POST | /logout | 로그아웃 |
| GET | /check-email | 이메일 중복 확인 |

### 2 프로필 API (`/api/v1/profile`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /me | 내 프로필 조회 |
| PATCH | /me | 프로필 수정 |
| POST | /me/image | 프로필 이미지 업로드 |
| DELETE | /me/image | 프로필 이미지 삭제 |

### 3 사용자 검색 API (`/api/v1/users`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /search | 사용자 검색 |
| GET | /colleges | 단과대학 목록 |
| GET | /colleges/{id}/departments | 단과대학별 학과 목록 |
| GET | /departments | 전체 학과 목록 |

### 4 강의 API (`/api/v1/courses`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | / | 강의 목록 조회 |
| GET | /{courseId} | 강의 상세 조회 |

### 5 교수 강의 관리 API (`/api/v1/professor/courses`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | / | 강의 개설 |
| PUT | /{courseId} | 강의 수정 |
| DELETE | /{courseId} | 강의 삭제 |
| GET | / | 내 강의 목록 |
| GET | /{courseId} | 내 강의 상세 |

### 6 주차별 콘텐츠 API (`/api/v1/professor/courses/{courseId}/weeks`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | / | 주차 목록 조회 |
| POST | / | 주차 등록 |
| PUT | /{weekId} | 주차 수정 |
| DELETE | /{weekId} | 주차 삭제 |
| GET | /{weekId}/contents | 콘텐츠 목록 |
| POST | /{weekId}/contents | 콘텐츠 등록 |
| PUT | /{weekId}/contents/{contentId} | 콘텐츠 수정 |
| DELETE | /{weekId}/contents/{contentId} | 콘텐츠 삭제 |
| PUT | /{weekId}/contents/reorder | 콘텐츠 순서 변경 |

### 7 과목 API (`/api/v1/subjects`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | / | 과목 목록 조회 |
| GET | /{subjectId} | 과목 상세 조회 |
| GET | /search | 과목 검색 |

### 8 수강신청 API (`/api/v1/enrollments`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /periods/current | 현재 수강신청 기간 조회 |
| GET | /courses | 수강신청 가능 강의 조회 |
| POST | /bulk | 수강신청 (일괄) |
| DELETE | /bulk | 수강 취소 (일괄) |
| GET | /my | 내 수강 목록 |

### 9 장바구니 API (`/api/v1/carts`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | / | 장바구니 조회 |
| POST | /bulk | 장바구니 추가 (일괄) |
| DELETE | /bulk | 장바구니 삭제 (일괄) |
| DELETE | / | 장바구니 전체 삭제 |

### 10 게시판 API (`/api/v1/board`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /{boardType}/posts | 게시글 작성 |
| GET | /{boardType}/posts | 게시글 목록 |
| GET | /{boardType}/posts/{id} | 게시글 상세 |
| PUT | /posts/{id} | 게시글 수정 |
| DELETE | /posts/{id} | 게시글 삭제 |
| POST | /posts/{id}/like | 좋아요 토글 |
| GET | /posts/{id}/liked | 좋아요 여부 조회 |

### 11 댓글 API (`/api/v1/board`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /comments | 댓글 작성 |
| GET | /comments | 댓글 목록 (postId 필터) |
| PUT | /comments/{id} | 댓글 수정 |
| DELETE | /comments/{id} | 댓글 삭제 |

### 12 첨부파일 API (`/api/v1/attachments`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /upload | 단일 파일 업로드 |
| POST | /upload/multiple | 다중 파일 업로드 |
| GET | /{attachmentId}/download | 파일 다운로드 |
| GET | /{attachmentId} | 첨부파일 정보 조회 |
| DELETE | /{attachmentId} | 첨부파일 삭제 |

### 13 과제 API (`/api/v1/assignments`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | / | 과제 등록 (교수) |
| GET | / | 강의별 과제 목록 |
| GET | /{id} | 과제 상세 |
| PUT | /{id} | 과제 수정 (교수) |
| DELETE | /{id} | 과제 삭제 (교수) |
| POST | /{id}/submit | 과제 제출 (학생) |
| GET | /{id}/submissions | 제출 목록 (교수) |
| PUT | /submissions/{submissionId}/grade | 채점 (교수) |
| GET | /{id}/my-submission | 내 제출 조회 (학생) |

### 14 알림 API (`/api/notifications`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | / | 알림 목록 (커서 기반) |
| GET | /{notificationId} | 알림 상세 |
| GET | /unread-count | 안읽은 알림 개수 |
| PATCH | /{notificationId}/read | 읽음 처리 |
| PATCH | /read-all | 모든 알림 읽음 처리 |
| DELETE | /{notificationId} | 알림 삭제 |
| DELETE | /read | 읽은 알림 삭제 |
| DELETE | /all | 모든 알림 삭제 |

### 15 대화방 API (`/api/v1/conversations`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | / | 대화방 목록 |
| POST | /with/{otherUserId} | 대화방 생성/조회 |
| GET | /{conversationId} | 대화방 상세 |
| DELETE | /{conversationId} | 대화방 삭제 |
| POST | /{conversationId}/read | 읽음 처리 |
| GET | /unread-count | 전체 안읽음 수 |

### 16 메시지 API (`/api/v1/messages`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | / | 메시지 전송 |
| POST | /bulk | 메시지 일괄 전송 |
| GET | /conversations/{conversationId} | 대화 내역 조회 |
| DELETE | /{messageId} | 메시지 삭제 |
| POST | /conversations/{conversationId}/read | 읽음 처리 |

### 17 SSE API (`/api/v1/sse`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /subscribe | SSE 연결 (실시간 알림/메시지) |

---

### 18 Video Streaming API (Port: 8090)

> **Video Streaming Server** 전용 API

#### 영상 업로드 (TUS Protocol)
| Method | Endpoint | 설명 |
|--------|----------|------|
| OPTIONS | /api/v1/videos/upload | TUS 프로토콜 지원 확인 |
| POST | /api/v1/videos/upload | 업로드 생성 (Location 헤더 반환) |
| HEAD | /api/v1/videos/upload/** | 업로드 상태 확인 |
| PATCH | /api/v1/videos/upload/** | 청크 업로드 |
| DELETE | /api/v1/videos/upload/** | 업로드 취소 |

#### 영상 스트리밍
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/v1/videos/stream/{videoId} | 영상 스트리밍 (Range 헤더 지원) |

#### 시청 세션
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /api/v1/sessions | 시청 세션 시작 |
| DELETE | /api/v1/sessions/{sessionId} | 시청 세션 종료 |
| GET | /api/v1/sessions/{sessionId} | 세션 정보 조회 |
| GET | /api/v1/sessions/active | 활성 세션 조회 (userId) |

#### 진도 보고
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /api/v1/progress | 진도 보고 (5초 간격) |
| GET | /api/v1/progress/{contentId} | 진도 조회 (studentId) |

#### 시청 이벤트
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /api/v1/watch-events | 이벤트 기록 |
| GET | /api/v1/watch-events/session/{sessionId} | 세션별 이벤트 조회 |
| GET | /api/v1/watch-events/session/{sessionId}/type | 이벤트 유형별 조회 |
