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

### 학생 주요 API
- `POST /api/v1/enrollments/bulk` - 수강신청
- `GET /api/v1/attendance/my` - 내 출석 현황
- `GET /api/v1/dashboard/student/*` - 대시보드
- `POST /api/v1/assignments/{id}/submit` - 과제 제출

### 교수 주요 API
- `POST /api/v1/professor/courses` - 강의 개설
- `GET /api/v1/professor/courses/{courseId}/attendance` - 출석 관리
- `POST /api/v1/assignments` - 과제 등록
- `PUT /api/v1/assignments/submissions/{id}/grade` - 채점
