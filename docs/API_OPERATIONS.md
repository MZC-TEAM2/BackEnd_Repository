# LMS API Operation Specification

> **프로젝트명**: MZC 1st Project - 학습관리시스템
> 
> **버전**: 1.0
>
> 
> **작성일**: 2025-12-22
> 
> **포맷**: 오퍼레이션 명세서 (Operation Specification)
> 
> **작성자** : 송명주 (최종 수정일 : 2025-12-22)

---

## 목차

1. [인증 API](#1-인증-api)
2. [프로필 API](#2-프로필-api)
3. [사용자 검색 API](#3-사용자-검색-api)
4. [강의 API](#4-강의-api)
5. [교수 강의 관리 API](#5-교수-강의-관리-api)
6. [주차별 콘텐츠 API](#6-주차별-콘텐츠-api)
7. [과목 API](#7-과목-api)
8. [수강신청 API](#8-수강신청-api)
9. [장바구니 API](#9-장바구니-api)
10. [게시판 API](#10-게시판-api)
11. [댓글 API](#11-댓글-api)
12. [첨부파일 API](#12-첨부파일-api)
13. [과제 API](#13-과제-api)
14. [알림 API](#14-알림-api)
15. [대화방 API](#15-대화방-api)
16. [메시지 API](#16-메시지-api)
17. [SSE API](#17-sse-api)
18. [출석 API](#18-출석-api)
19. [대시보드 API](#19-대시보드-api)
20. [시험/퀴즈 API](#20-시험퀴즈-api)
21. [성적 API](#21-성적-api)

---

## 1. 인증 API

### 1.1 이메일 인증 코드 발송

| 항목 | 내용 |
|------|------|
| **식별자** | `AUTH-001` |
| **엔드포인트** | `POST /api/auth/signup/email-verification` |
| **책임** | 회원가입을 위한 이메일 인증 코드를 발송하고 Redis에 저장함을 보장 |
| **입력** | `{ "email": "string (required)" }` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "message": "인증 코드가 발송되었습니다." }` |
| **출력 (실패)** | `400 Bad Request` - 이미 가입된 이메일 / `500 Internal Server Error` - 발송 실패 |
| **전제조건** | 유효한 이메일 형식, 미가입 이메일 |
| **후조건** | 6자리 인증 코드가 Redis에 저장됨 (TTL: 5분) |
| **예외/에러 코드** | `EMAIL_ALREADY_REGISTERED` - 이미 가입된 이메일 / `EMAIL_SEND_FAILED` - 발송 실패 |
| **성능 제약** | 응답시간 < 3초 (SMTP 발송 포함) |
| **트랜잭션/락** | 불필요 |
| **비동기 처리** | **Yes** - `@Async("emailExecutor")` 이메일 발송은 별도 스레드풀에서 비동기 처리 |

---

### 1.2 인증 코드 확인

| 항목 | 내용 |
|------|------|
| **식별자** | `AUTH-002` |
| **엔드포인트** | `POST /api/auth/signup/verify-code` |
| **책임** | 사용자가 입력한 인증 코드가 Redis에 저장된 코드와 일치하는지 검증 |
| **입력** | `{ "email": "string (required)", "code": "string (required, 6자리)" }` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "message": "이메일 인증이 완료되었습니다." }` |
| **출력 (실패)** | `400 Bad Request` - 코드 불일치 또는 만료 |
| **전제조건** | 해당 이메일로 인증 코드가 발송된 상태, Redis에 코드 존재 |
| **후조건** | 이메일 인증 완료 상태 (verified) 마킹 |
| **예외/에러 코드** | `INVALID_CODE` - 코드 불일치 / `CODE_EXPIRED` - 코드 만료 |
| **성능 제약** | 응답시간 < 100ms |
| **트랜잭션/락** | 불필요 |

---

### 1.3 회원가입

| 항목 | 내용 |
|------|------|
| **식별자** | `AUTH-003` |
| **엔드포인트** | `POST /api/auth/signup` |
| **책임** | 학생 또는 교수 계정을 생성하고 초기 프로필 정보를 저장 |
| **입력** | `SignupRequestDto` - email, password, name, userType(STUDENT/PROFESSOR), departmentId, 추가 정보 |
| **출력 (성공)** | `201 Created` - `{ "success": true, "message": "회원가입이 완료되었습니다.", "userId": "string" }` |
| **출력 (실패)** | `400 Bad Request` - 유효성 검증 실패 / `500 Internal Server Error` |
| **전제조건** | 이메일 인증 완료, 비밀번호 정책 충족 (8자 이상, 영문+숫자+특수문자) |
| **후조건** | users, students/professors 테이블에 레코드 생성 |
| **예외/에러 코드** | `EMAIL_NOT_VERIFIED` - 이메일 미인증 / `INVALID_PASSWORD` - 비밀번호 정책 미충족 / `DUPLICATE_EMAIL` - 이메일 중복 |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | **Required** - users, students/professors 테이블 트랜잭션 |

---

### 1.4 로그인

| 항목 | 내용 |
|------|------|
| **식별자** | `AUTH-004` |
| **엔드포인트** | `POST /api/auth/login` |
| **책임** | 사용자 인증 후 JWT Access Token과 Refresh Token 발급 |
| **입력** | `{ "email": "string (required)", "password": "string (required)" }` |
| **출력 (성공)** | `200 OK` - `LoginResponseDto` (accessToken, refreshToken, userType, userId, expiresIn) |
| **출력 (실패)** | `400 Bad Request` - 잘못된 자격 증명 / `500 Internal Server Error` |
| **전제조건** | 가입된 사용자, 올바른 비밀번호 |
| **후조건** | Access Token (30분), Refresh Token (7일) 발급, 로그인 IP 기록 |
| **예외/에러 코드** | `INVALID_CREDENTIALS` - 이메일/비밀번호 불일치 / `ACCOUNT_DISABLED` - 비활성화 계정 |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

### 1.5 토큰 갱신

| 항목 | 내용 |
|------|------|
| **식별자** | `AUTH-005` |
| **엔드포인트** | `POST /api/auth/refresh` |
| **책임** | 유효한 Refresh Token으로 새로운 Access Token 발급 |
| **입력** | `{ "refreshToken": "string (required)" }` |
| **출력 (성공)** | `200 OK` - `RefreshTokenResponseDto` (accessToken, expiresIn) |
| **출력 (실패)** | `401 Unauthorized` - 유효하지 않은 토큰 |
| **전제조건** | 유효한 Refresh Token |
| **후조건** | 새로운 Access Token 발급 |
| **예외/에러 코드** | `INVALID_REFRESH_TOKEN` - 토큰 유효하지 않음 / `TOKEN_EXPIRED` - 토큰 만료 |
| **성능 제약** | 응답시간 < 100ms |
| **트랜잭션/락** | 불필요 |

---

### 1.6 로그아웃

| 항목 | 내용 |
|------|------|
| **식별자** | `AUTH-006` |
| **엔드포인트** | `POST /api/auth/logout` |
| **책임** | 사용자 세션 종료 및 Refresh Token 무효화 |
| **입력** | Header: `Refresh-Token: string (optional)` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "message": "로그아웃되었습니다." }` |
| **출력 (실패)** | 항상 성공 응답 (실패해도 성공 처리) |
| **전제조건** | 없음 |
| **후조건** | Refresh Token 무효화 (블랙리스트 등록) |
| **예외/에러 코드** | 없음 (항상 성공) |
| **성능 제약** | 응답시간 < 100ms |
| **트랜잭션/락** | 불필요 |

---

### 1.7 이메일 중복 확인

| 항목 | 내용 |
|------|------|
| **식별자** | `AUTH-007` |
| **엔드포인트** | `GET /api/auth/check-email` |
| **책임** | 입력된 이메일이 이미 가입되어 있는지 확인 |
| **입력** | Query: `email=string (required)` |
| **출력 (성공)** | `200 OK` - `{ "available": boolean, "message": "string" }` |
| **출력 (실패)** | `500 Internal Server Error` |
| **전제조건** | 유효한 이메일 형식 |
| **후조건** | 없음 (조회만 수행) |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 100ms |
| **트랜잭션/락** | 불필요 |

---

## 2. 프로필 API

### 2.1 내 프로필 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `PROFILE-001` |
| **엔드포인트** | `GET /api/v1/profile/me` |
| **책임** | 인증된 사용자의 프로필 정보를 반환 |
| **입력** | Header: `Authorization: Bearer {accessToken}` |
| **출력 (성공)** | `200 OK` - `ProfileResponseDto` (userId, name, email, userType, department, profileImage 등) |
| **출력 (실패)** | `401 Unauthorized` - 미인증 |
| **전제조건** | 유효한 Access Token |
| **후조건** | 없음 (조회만 수행) |
| **예외/에러 코드** | `UNAUTHORIZED` - 인증 필요 |
| **성능 제약** | 응답시간 < 100ms |
| **트랜잭션/락** | 불필요 |

---

### 2.2 프로필 수정

| 항목 | 내용 |
|------|------|
| **식별자** | `PROFILE-002` |
| **엔드포인트** | `PATCH /api/v1/profile/me` |
| **책임** | 사용자의 프로필 정보 (연락처 등) 수정 |
| **입력** | `ProfileUpdateRequestDto` - phone, address 등 수정 가능한 필드 |
| **출력 (성공)** | `200 OK` - `ProfileResponseDto` (수정된 프로필) |
| **출력 (실패)** | `400 Bad Request` - 유효성 검증 실패 / `401 Unauthorized` |
| **전제조건** | 유효한 Access Token |
| **후조건** | users/students/professors 테이블 업데이트 |
| **예외/에러 코드** | `INVALID_INPUT` - 유효성 검증 실패 |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** - 프로필 데이터 트랜잭션 |

---

### 2.3 프로필 이미지 업로드

| 항목 | 내용 |
|------|------|
| **식별자** | `PROFILE-003` |
| **엔드포인트** | `POST /api/v1/profile/me/image` |
| **책임** | 프로필 이미지를 WebP 형식으로 변환하여 저장 |
| **입력** | `multipart/form-data` - image: MultipartFile |
| **출력 (성공)** | `202 Accepted` |
| **출력 (실패)** | `400 Bad Request` - 파일 형식 오류 / `413 Payload Too Large` |
| **전제조건** | 유효한 Access Token, 지원 이미지 형식 (jpg, png, gif, webp) |
| **후조건** | 이미지 WebP 변환 및 저장, 프로필 이미지 URL 업데이트 |
| **예외/에러 코드** | `INVALID_FILE_TYPE` - 지원하지 않는 형식 / `FILE_TOO_LARGE` - 파일 크기 초과 |
| **성능 제약** | 응답시간 < 2초 (이미지 처리 포함) |
| **트랜잭션/락** | 불필요 |
| **비동기 처리** | **Yes** - `@Async("imageProcessingExecutor")` WebP 변환/썸네일 생성은 별도 스레드풀에서 비동기 처리 (202 Accepted 즉시 반환) |

---

### 2.4 프로필 이미지 삭제

| 항목 | 내용 |
|------|------|
| **식별자** | `PROFILE-004` |
| **엔드포인트** | `DELETE /api/v1/profile/me/image` |
| **책임** | 프로필 이미지 삭제 및 기본 이미지로 복원 |
| **입력** | Header: `Authorization: Bearer {accessToken}` |
| **출력 (성공)** | `200 OK` |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 유효한 Access Token |
| **후조건** | 프로필 이미지 파일 삭제, 프로필 이미지 URL null 처리 |
| **예외/에러 코드** | `IMAGE_NOT_FOUND` - 이미지 없음 |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

## 3. 사용자 검색 API

### 3.1 사용자 검색

| 항목 | 내용 |
|------|------|
| **식별자** | `USER-001` |
| **엔드포인트** | `GET /api/v1/users/search` |
| **책임** | 단과대, 학과, 이름, 사용자 타입으로 사용자를 검색 (커서 기반 무한스크롤) |
| **입력** | Query: `collegeId, departmentId, name, userType, cursor, size` (모두 optional) |
| **출력 (성공)** | `200 OK` - `UserSearchCursorResponseDto` (users[], nextCursor, hasNext) |
| **출력 (실패)** | `400 Bad Request` |
| **전제조건** | 유효한 Access Token |
| **후조건** | 없음 (조회만 수행) |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 200ms, 최대 결과 100건 |
| **트랜잭션/락** | 불필요 |

---

### 3.2 단과대 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `USER-002` |
| **엔드포인트** | `GET /api/v1/users/colleges` |
| **책임** | 전체 단과대 목록을 반환 (필터 드롭다운용) |
| **입력** | 없음 |
| **출력 (성공)** | `200 OK` - `List<CollegeListResponseDto>` |
| **출력 (실패)** | `500 Internal Server Error` |
| **전제조건** | 없음 |
| **후조건** | 없음 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 100ms (캐시 권장) |
| **트랜잭션/락** | 불필요 |

---

### 3.3 학과 목록 조회 (단과대별)

| 항목 | 내용 |
|------|------|
| **식별자** | `USER-003` |
| **엔드포인트** | `GET /api/v1/users/colleges/{collegeId}/departments` |
| **책임** | 특정 단과대에 속한 학과 목록을 반환 |
| **입력** | Path: `collegeId: Long` |
| **출력 (성공)** | `200 OK` - `List<DepartmentListResponseDto>` |
| **출력 (실패)** | `404 Not Found` - 단과대 없음 |
| **전제조건** | 유효한 collegeId |
| **후조건** | 없음 |
| **예외/에러 코드** | `COLLEGE_NOT_FOUND` - 단과대 없음 |
| **성능 제약** | 응답시간 < 100ms |
| **트랜잭션/락** | 불필요 |

---

### 3.4 전체 학과 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `USER-004` |
| **엔드포인트** | `GET /api/v1/users/departments` |
| **책임** | 모든 학과 목록을 반환 |
| **입력** | 없음 |
| **출력 (성공)** | `200 OK` - `List<DepartmentListResponseDto>` |
| **출력 (실패)** | `500 Internal Server Error` |
| **전제조건** | 없음 |
| **후조건** | 없음 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 100ms (캐시 권장) |
| **트랜잭션/락** | 불필요 |

---

## 4. 강의 API

### 4.1 강의 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `COURSE-001` |
| **엔드포인트** | `GET /api/v1/courses` |
| **책임** | 검색 조건에 따른 강의 목록을 페이지네이션으로 반환 |
| **입력** | Query: `page, size, keyword, departmentId, courseType, credits, enrollmentPeriodId (required), sort` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "data": CourseResponseDto }` |
| **출력 (실패)** | `401 Unauthorized` / `500 Internal Server Error` |
| **전제조건** | 유효한 Access Token, 유효한 enrollmentPeriodId |
| **후조건** | 없음 |
| **예외/에러 코드** | `UNAUTHORIZED` - 인증 필요 / `INVALID_PERIOD` - 잘못된 수강신청 기간 |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | 불필요 |

---

### 4.2 강의 상세 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `COURSE-002` |
| **엔드포인트** | `GET /api/v1/courses/{courseId}` |
| **책임** | 특정 강의의 상세 정보 (시간표, 주차별 계획, 선수과목 등) 반환 |
| **입력** | Path: `courseId: Long` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "data": CourseDetailDto }` |
| **출력 (실패)** | `404 Not Found` - 강의 없음 / `401 Unauthorized` |
| **전제조건** | 유효한 Access Token, 존재하는 courseId |
| **후조건** | 없음 |
| **예외/에러 코드** | `COURSE_NOT_FOUND` - 강의 없음 |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

## 5. 교수 강의 관리 API

### 5.1 강의 개설

| 항목 | 내용 |
|------|------|
| **식별자** | `PROF-COURSE-001` |
| **엔드포인트** | `POST /api/v1/professor/courses` |
| **책임** | 새로운 강의를 개설하고 시간표를 등록 |
| **입력** | `CreateCourseRequestDto` - subjectId, sectionNumber, schedules[], maxStudents, enrollmentPeriodId |
| **출력 (성공)** | `201 Created` - `{ "success": true, "data": CreateCourseResponseDto }` |
| **출력 (실패)** | `400 Bad Request` - 시간표 충돌 / `401 Unauthorized` |
| **전제조건** | 교수 권한, 유효한 과목 ID, 시간표 충돌 없음 |
| **후조건** | courses, course_schedules 테이블에 레코드 생성 |
| **예외/에러 코드** | `SCHEDULE_CONFLICT` - 시간표 충돌 / `SUBJECT_NOT_FOUND` - 과목 없음 / `DUPLICATE_SECTION` - 분반 중복 |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | **Required** - courses, schedules 트랜잭션 |

---

### 5.2 강의 수정

| 항목 | 내용 |
|------|------|
| **식별자** | `PROF-COURSE-002` |
| **엔드포인트** | `PUT /api/v1/professor/courses/{courseId}` |
| **책임** | 개설된 강의 정보 및 시간표 수정 |
| **입력** | Path: `courseId`, Body: `UpdateCourseRequestDto` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "data": CreateCourseResponseDto }` |
| **출력 (실패)** | `400 Bad Request` / `403 Forbidden` - 본인 강의 아님 |
| **전제조건** | 교수 권한, 본인이 개설한 강의 |
| **후조건** | courses, course_schedules 업데이트 |
| **예외/에러 코드** | `COURSE_NOT_FOUND` - 강의 없음 / `NOT_OWNER` - 본인 강의 아님 / `SCHEDULE_CONFLICT` - 시간표 충돌 |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | **Required** |

---

### 5.3 강의 취소

| 항목 | 내용 |
|------|------|
| **식별자** | `PROF-COURSE-003` |
| **엔드포인트** | `DELETE /api/v1/professor/courses/{courseId}` |
| **책임** | 개설된 강의를 취소 (Soft Delete) |
| **입력** | Path: `courseId` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "message": "강의가 취소되었습니다." }` |
| **출력 (실패)** | `400 Bad Request` - 수강생 존재 시 취소 불가 |
| **전제조건** | 교수 권한, 본인 강의, 수강 신청한 학생 없음 |
| **후조건** | courses.deleted_at 설정 |
| **예외/에러 코드** | `HAS_ENROLLMENTS` - 수강생 존재 / `NOT_OWNER` - 본인 강의 아님 |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

### 5.4 내 강의 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `PROF-COURSE-004` |
| **엔드포인트** | `GET /api/v1/professor/courses` |
| **책임** | 교수가 개설한 강의 목록 반환 |
| **입력** | Query: `academicTermId (optional)` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "data": MyCoursesResponseDto }` |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 교수 권한 |
| **후조건** | 없음 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

### 5.5 교수 강의 상세 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `PROF-COURSE-005` |
| **엔드포인트** | `GET /api/v1/professor/courses/{courseId}` |
| **책임** | 교수가 개설한 강의의 상세 정보 반환 (수강생 목록 포함) |
| **입력** | Path: `courseId` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "data": ProfessorCourseDetailDto }` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 교수 권한, 본인 강의 |
| **후조건** | 없음 |
| **예외/에러 코드** | `COURSE_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | 불필요 |

---

## 6. 주차별 콘텐츠 API

### 6.1 주차 생성

| 항목 | 내용 |
|------|------|
| **식별자** | `WEEK-001` |
| **엔드포인트** | `POST /api/v1/professor/courses/{courseId}/weeks` |
| **책임** | 강의에 새로운 주차를 생성 |
| **입력** | Path: `courseId`, Body: `CreateWeekRequestDto` (weekNumber, title, description) |
| **출력 (성공)** | `201 Created` - `WeekDto` |
| **출력 (실패)** | `400 Bad Request` - 중복 주차 번호 |
| **전제조건** | 교수 권한, 본인 강의, 주차 번호 미중복 |
| **후조건** | course_weeks 테이블에 레코드 생성 |
| **예외/에러 코드** | `DUPLICATE_WEEK` - 주차 번호 중복 |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

### 6.2 주차 수정

| 항목 | 내용 |
|------|------|
| **식별자** | `WEEK-002` |
| **엔드포인트** | `PUT /api/v1/professor/courses/{courseId}/weeks/{weekId}` |
| **책임** | 주차 정보 (제목, 설명) 수정 |
| **입력** | Path: `courseId, weekId`, Body: `UpdateWeekRequestDto` |
| **출력 (성공)** | `200 OK` - `WeekDto` |
| **출력 (실패)** | `404 Not Found` |
| **전제조건** | 교수 권한, 본인 강의 |
| **후조건** | course_weeks 업데이트 |
| **예외/에러 코드** | `WEEK_NOT_FOUND` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

### 6.3 주차 삭제

| 항목 | 내용 |
|------|------|
| **식별자** | `WEEK-003` |
| **엔드포인트** | `DELETE /api/v1/professor/courses/{courseId}/weeks/{weekId}` |
| **책임** | 주차 및 해당 주차의 모든 콘텐츠 삭제 |
| **입력** | Path: `courseId, weekId` |
| **출력 (성공)** | `200 OK` - `{ "message": "주차가 삭제되었습니다." }` |
| **출력 (실패)** | `404 Not Found` |
| **전제조건** | 교수 권한, 본인 강의 |
| **후조건** | course_weeks, course_week_contents 삭제 |
| **예외/에러 코드** | `WEEK_NOT_FOUND` |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | **Required** - Cascade 삭제 |

---

### 6.4 콘텐츠 등록

| 항목 | 내용 |
|------|------|
| **식별자** | `CONTENT-001` |
| **엔드포인트** | `POST /api/v1/professor/courses/{courseId}/weeks/{weekId}/contents` |
| **책임** | 주차에 학습 콘텐츠 (영상, 문서 등) 등록 |
| **입력** | Path: `courseId, weekId`, Body: `CreateWeekContentRequestDto` (contentType, title, url, duration 등) |
| **출력 (성공)** | `201 Created` - `WeekContentDto` |
| **출력 (실패)** | `400 Bad Request` |
| **전제조건** | 교수 권한, 본인 강의, 유효한 주차 |
| **후조건** | course_week_contents 테이블에 레코드 생성 |
| **예외/에러 코드** | `WEEK_NOT_FOUND` / `INVALID_CONTENT_TYPE` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

### 6.5 콘텐츠 수정

| 항목 | 내용 |
|------|------|
| **식별자** | `CONTENT-002` |
| **엔드포인트** | `PUT /api/v1/professor/courses/{courseId}/weeks/{weekId}/contents/{contentId}` |
| **책임** | 콘텐츠 정보 수정 |
| **입력** | Path: `courseId, weekId, contentId`, Body: `UpdateWeekContentRequestDto` |
| **출력 (성공)** | `200 OK` - `WeekContentDto` |
| **출력 (실패)** | `404 Not Found` |
| **전제조건** | 교수 권한, 본인 강의 |
| **후조건** | course_week_contents 업데이트 |
| **예외/에러 코드** | `CONTENT_NOT_FOUND` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

### 6.6 콘텐츠 삭제

| 항목 | 내용 |
|------|------|
| **식별자** | `CONTENT-003` |
| **엔드포인트** | `DELETE /api/v1/professor/courses/{courseId}/weeks/{weekId}/contents/{contentId}` |
| **책임** | 콘텐츠 삭제 |
| **입력** | Path: `courseId, weekId, contentId` |
| **출력 (성공)** | `200 OK` - `{ "message": "콘텐츠가 삭제되었습니다." }` |
| **출력 (실패)** | `404 Not Found` |
| **전제조건** | 교수 권한, 본인 강의 |
| **후조건** | course_week_contents 삭제 |
| **예외/에러 코드** | `CONTENT_NOT_FOUND` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

### 6.7 주차 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `WEEK-004` |
| **엔드포인트** | `GET /api/v1/professor/courses/{courseId}/weeks` |
| **책임** | 강의의 전체 주차 목록 반환 (교수 또는 수강 학생만 조회 가능) |
| **입력** | Path: `courseId` |
| **출력 (성공)** | `200 OK` - `List<WeekListResponseDto>` |
| **출력 (실패)** | `403 Forbidden` - 접근 권한 없음 |
| **전제조건** | 교수(본인 강의) 또는 수강 중인 학생 |
| **후조건** | 없음 |
| **예외/에러 코드** | `ACCESS_DENIED` - 접근 권한 없음 |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

### 6.8 주차별 콘텐츠 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `CONTENT-004` |
| **엔드포인트** | `GET /api/v1/professor/courses/{courseId}/weeks/{weekId}/contents` |
| **책임** | 특정 주차의 콘텐츠 목록 반환 |
| **입력** | Path: `courseId, weekId` |
| **출력 (성공)** | `200 OK` - `WeekContentsResponseDto` |
| **출력 (실패)** | `403 Forbidden` / `404 Not Found` |
| **전제조건** | 교수(본인 강의) 또는 수강 중인 학생 |
| **후조건** | 없음 |
| **예외/에러 코드** | `WEEK_NOT_FOUND` / `ACCESS_DENIED` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

## 7. 과목 API

### 7.1 과목 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `SUBJECT-001` |
| **엔드포인트** | `GET /api/v1/subjects` |
| **책임** | 과목 목록을 페이지네이션으로 반환 (필터링 지원) |
| **입력** | Query: `page, size, keyword, departmentId, showAllDepartments, courseType, credits, isActive` |
| **출력 (성공)** | `200 OK` - `PageResponse<SubjectResponse>` |
| **출력 (실패)** | `500 Internal Server Error` |
| **전제조건** | 유효한 Access Token |
| **후조건** | 없음 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | 불필요 |

---

### 7.2 과목 상세 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `SUBJECT-002` |
| **엔드포인트** | `GET /api/v1/subjects/{subjectId}` |
| **책임** | 과목 상세 정보 (선수과목 포함) 반환 |
| **입력** | Path: `subjectId` |
| **출력 (성공)** | `200 OK` - `SubjectDetailResponse` |
| **출력 (실패)** | `404 Not Found` |
| **전제조건** | 존재하는 subjectId |
| **후조건** | 없음 |
| **예외/에러 코드** | `SUBJECT_NOT_FOUND` |
| **성능 제약** | 응답시간 < 100ms |
| **트랜잭션/락** | 불필요 |

---

### 7.3 과목 검색

| 항목 | 내용 |
|------|------|
| **식별자** | `SUBJECT-003` |
| **엔드포인트** | `GET /api/v1/subjects/search` |
| **책임** | 과목명 또는 과목코드로 과목 검색 |
| **입력** | Query: `q (required), page, size` |
| **출력 (성공)** | `200 OK` - `PageResponse<SubjectSearchResponse>` |
| **출력 (실패)** | `400 Bad Request` - 검색어 누락 |
| **전제조건** | 검색어 필수 |
| **후조건** | 없음 |
| **예외/에러 코드** | `MISSING_QUERY` - 검색어 누락 |
| **성능 제약** | 응답시간 < 300ms, size 최대 50 |
| **트랜잭션/락** | 불필요 |

---

## 8. 수강신청 API

### 8.1 현재 수강신청 기간 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `ENROLL-001` |
| **엔드포인트** | `GET /api/v1/enrollments/periods/current` |
| **책임** | 현재 활성화된 수강신청 기간 정보 반환 |
| **입력** | Query: `type (optional)` - ENROLLMENT, COURSE_REGISTRATION, ADJUSTMENT, CANCELLATION |
| **출력 (성공)** | `200 OK` - `EnrollmentPeriodResponseDto` |
| **출력 (실패)** | `400 Bad Request` - 현재 활성 기간 없음 |
| **전제조건** | 없음 |
| **후조건** | 없음 |
| **예외/에러 코드** | `NO_ACTIVE_PERIOD` - 활성 기간 없음 |
| **성능 제약** | 응답시간 < 100ms |
| **트랜잭션/락** | 불필요 |

---

### 8.2 수강신청 가능 강의 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `ENROLL-002` |
| **엔드포인트** | `GET /api/v1/enrollments/courses` |
| **책임** | 수강신청 가능한 강의 목록 반환 (잔여석, 신청 여부 포함) |
| **입력** | Query: `page, size, keyword, departmentId, courseType, credits, enrollmentPeriodId (required), sort` |
| **출력 (성공)** | `200 OK` - `CourseListResponseDto` |
| **출력 (실패)** | `401 Unauthorized` / `400 Bad Request` |
| **전제조건** | 유효한 Access Token (학생), 유효한 enrollmentPeriodId |
| **후조건** | 없음 |
| **예외/에러 코드** | `UNAUTHORIZED` / `INVALID_PERIOD` |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | 불필요 |

---

### 8.3 일괄 수강신청

| 항목 | 내용 |
|------|------|
| **식별자** | `ENROLL-003` |
| **엔드포인트** | `POST /api/v1/enrollments/bulk` |
| **책임** | 여러 강의를 한 번에 수강신청, 정원/시간표/학점 검증 후 성공/실패 결과 반환 |
| **입력** | `{ "courseIds": [Long] }` |
| **출력 (성공)** | `200 OK` - `EnrollmentBulkResponseDto` (results[], summary{successCount, failedCount}) |
| **출력 (실패)** | `401 Unauthorized` / `400 Bad Request` |
| **전제조건** | 학생 권한, 수강신청 기간 내, 각 강의별 정원 여유, 시간표 충돌 없음, 학점 제한 (21학점) 미초과, 선수과목 이수 완료 |
| **후조건** | enrollments 테이블에 레코드 생성, courses.current_students 증가, 수강신청 이벤트 발행 |
| **예외/에러 코드** | `COURSE_FULL` - 정원 마감 / `SCHEDULE_CONFLICT` - 시간표 충돌 / `CREDIT_EXCEEDED` - 학점 초과 / `PREREQUISITE_NOT_MET` - 선수과목 미이수 / `ALREADY_ENROLLED` - 이미 수강신청 |
| **성능 제약** | 응답시간 < 2초 (동시성 처리 포함) |
| **트랜잭션/락** | **Required** - **비관적 락 (PESSIMISTIC_WRITE)** 으로 정원 동시성 제어 |
| **비동기 처리** | **Yes** - `@Async` 수강신청 완료 알림은 Redis Queue에 비동기로 발행 |

---

### 8.4 일괄 수강신청 취소

| 항목 | 내용 |
|------|------|
| **식별자** | `ENROLL-004` |
| **엔드포인트** | `DELETE /api/v1/enrollments/bulk` |
| **책임** | 여러 수강신청을 한 번에 취소 |
| **입력** | `{ "enrollmentIds": [Long] }` |
| **출력 (성공)** | `200 OK` - `EnrollmentBulkCancelResponseDto` |
| **출력 (실패)** | `401 Unauthorized` / `400 Bad Request` |
| **전제조건** | 학생 권한, 본인의 수강신청 |
| **후조건** | enrollments 삭제, courses.current_students 감소, 취소 이벤트 발행 |
| **예외/에러 코드** | `ENROLLMENT_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 1초 |
| **트랜잭션/락** | **Required** |

---

### 8.5 내 수강신청 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `ENROLL-005` |
| **엔드포인트** | `GET /api/v1/enrollments/my` |
| **책임** | 학생의 수강신청 목록 및 총 학점 정보 반환 |
| **입력** | Query: `enrollmentPeriodId (optional)` |
| **출력 (성공)** | `200 OK` - `MyEnrollmentsResponseDto` (enrollments[], totalCredits, remainingCredits) |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 학생 권한 |
| **후조건** | 없음 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

## 9. 장바구니 API

### 9.1 장바구니 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `CART-001` |
| **엔드포인트** | `GET /api/v1/carts` |
| **책임** | 학생의 장바구니 목록 반환 |
| **입력** | Header: `Authorization` |
| **출력 (성공)** | `200 OK` - `CartResponseDto` |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 학생 권한 |
| **후조건** | 없음 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

### 9.2 장바구니 일괄 추가

| 항목 | 내용 |
|------|------|
| **식별자** | `CART-002` |
| **엔드포인트** | `POST /api/v1/carts/bulk` |
| **책임** | 여러 강의를 장바구니에 추가 |
| **입력** | `{ "courseIds": [Long] }` |
| **출력 (성공)** | `200 OK` - `CartBulkAddResponseDto` |
| **출력 (실패)** | `400 Bad Request` - 이미 장바구니에 존재 |
| **전제조건** | 학생 권한, 중복 추가 불가 |
| **후조건** | course_carts 테이블에 레코드 생성 |
| **예외/에러 코드** | `ALREADY_IN_CART` - 이미 장바구니에 존재 |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | **Required** |

---

### 9.3 장바구니 일괄 삭제

| 항목 | 내용 |
|------|------|
| **식별자** | `CART-003` |
| **엔드포인트** | `DELETE /api/v1/carts/bulk` |
| **책임** | 선택한 장바구니 항목 삭제 |
| **입력** | `{ "cartIds": [Long] }` |
| **출력 (성공)** | `200 OK` - `CartBulkDeleteResponseDto` |
| **출력 (실패)** | `400 Bad Request` |
| **전제조건** | 학생 권한, 본인 장바구니 |
| **후조건** | course_carts 삭제 |
| **예외/에러 코드** | `CART_NOT_FOUND` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

### 9.4 장바구니 전체 비우기

| 항목 | 내용 |
|------|------|
| **식별자** | `CART-004` |
| **엔드포인트** | `DELETE /api/v1/carts` |
| **책임** | 장바구니의 모든 항목 삭제 |
| **입력** | Header: `Authorization` |
| **출력 (성공)** | `200 OK` - `CartBulkDeleteResponseDto` |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 학생 권한 |
| **후조건** | 해당 학생의 모든 course_carts 삭제 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

## 10. 게시판 API

### 10.1 게시글 작성

| 항목 | 내용 |
|------|------|
| **식별자** | `POST-001` |
| **엔드포인트** | `POST /api/v1/boards/{boardType}/posts` |
| **책임** | 게시판에 새 게시글 작성 (해시태그, 첨부파일 지원) |
| **입력** | Path: `boardType (STUDENT/PROFESSOR)`, Body: `PostCreateRequestDto` (title, content, isAnonymous, hashtags[], attachmentIds[]) |
| **출력 (성공)** | `201 Created` - `PostResponseDto` |
| **출력 (실패)** | `400 Bad Request` / `403 Forbidden` - 게시판 권한 없음 |
| **전제조건** | 해당 게시판 접근 권한 (학생→STUDENT, 교수→PROFESSOR) |
| **후조건** | posts, post_hashtags, attachment 연결 생성 |
| **예외/에러 코드** | `ACCESS_DENIED` - 게시판 권한 없음 / `INVALID_ATTACHMENT` - 첨부파일 없음 |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | **Required** |

---

### 10.2 게시글 상세 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `POST-002` |
| **엔드포인트** | `GET /api/v1/boards/{boardType}/posts/{id}` |
| **책임** | 게시글 상세 정보 반환, 조회수 증가 |
| **입력** | Path: `boardType, id` |
| **출력 (성공)** | `200 OK` - `PostResponseDto` (author, content, attachments, hashtags, viewCount, likeCount) |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 해당 게시판 접근 권한, 존재하는 게시글 (삭제되지 않음) |
| **후조건** | posts.view_count 증가 |
| **예외/에러 코드** | `POST_NOT_FOUND` / `ACCESS_DENIED` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 (조회수는 비동기 처리 권장) |

---

### 10.3 게시글 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `POST-003` |
| **엔드포인트** | `GET /api/v1/boards/{boardType}/posts` |
| **책임** | 게시글 목록 페이지네이션 반환 (검색, 해시태그 필터 지원) |
| **입력** | Path: `boardType`, Query: `search, hashtag, page, size, sort` |
| **출력 (성공)** | `200 OK` - `Page<PostListResponseDto>` |
| **출력 (실패)** | `403 Forbidden` |
| **전제조건** | 해당 게시판 접근 권한 |
| **후조건** | 없음 |
| **예외/에러 코드** | `ACCESS_DENIED` |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | 불필요 |

---

### 10.4 게시글 수정

| 항목 | 내용 |
|------|------|
| **식별자** | `POST-004` |
| **엔드포인트** | `PUT /api/v1/boards/posts/{id}` |
| **책임** | 게시글 제목, 내용, 해시태그, 첨부파일 수정 |
| **입력** | Path: `id`, Body: `PostUpdateRequestDto` (Multipart 또는 JSON) |
| **출력 (성공)** | `200 OK` - `PostResponseDto` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` - 본인 게시글 아님 |
| **전제조건** | 본인 게시글 |
| **후조건** | posts, post_hashtags 업데이트 |
| **예외/에러 코드** | `POST_NOT_FOUND` / `NOT_AUTHOR` |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | **Required** |

---

### 10.5 게시글 삭제

| 항목 | 내용 |
|------|------|
| **식별자** | `POST-005` |
| **엔드포인트** | `DELETE /api/v1/boards/posts/{id}` |
| **책임** | 게시글 삭제 (Soft Delete) |
| **입력** | Path: `id` |
| **출력 (성공)** | `204 No Content` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 본인 게시글 또는 관리자 권한 |
| **후조건** | posts.deleted_at 설정 |
| **예외/에러 코드** | `POST_NOT_FOUND` / `NOT_AUTHOR` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

### 10.6 게시글 좋아요 토글

| 항목 | 내용 |
|------|------|
| **식별자** | `POST-006` |
| **엔드포인트** | `POST /api/v1/boards/posts/{id}/like` |
| **책임** | 좋아요 추가/취소 토글 (중복 방지) |
| **입력** | Path: `id`, Query: `userId` |
| **출력 (성공)** | `200 OK` - `LikeToggleResponseDto` (liked: boolean) |
| **출력 (실패)** | `404 Not Found` |
| **전제조건** | 존재하는 게시글 |
| **후조건** | post_likes 테이블 추가/삭제, posts.like_count 갱신 |
| **예외/에러 코드** | `POST_NOT_FOUND` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

### 10.7 좋아요 여부 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `POST-007` |
| **엔드포인트** | `GET /api/v1/boards/posts/{id}/liked` |
| **책임** | 현재 사용자가 해당 게시글에 좋아요 했는지 확인 |
| **입력** | Path: `id`, Query: `userId` |
| **출력 (성공)** | `200 OK` - `LikeCheckResponseDto` (liked: boolean) |
| **출력 (실패)** | `404 Not Found` |
| **전제조건** | 존재하는 게시글 |
| **후조건** | 없음 |
| **예외/에러 코드** | `POST_NOT_FOUND` |
| **성능 제약** | 응답시간 < 100ms |
| **트랜잭션/락** | 불필요 |

---

## 11. 댓글 API

### 11.1 댓글 작성

| 항목 | 내용 |
|------|------|
| **식별자** | `COMMENT-001` |
| **엔드포인트** | `POST /api/v1/board/comments` |
| **책임** | 게시글에 댓글 또는 대댓글 작성 |
| **입력** | `CommentCreateRequestDto` (postId, content, isAnonymous, parentCommentId) |
| **출력 (성공)** | `201 Created` - `CommentResponseDto` |
| **출력 (실패)** | `400 Bad Request` / `404 Not Found` - 게시글 없음 |
| **전제조건** | 존재하는 게시글, 대댓글인 경우 부모 댓글 존재 |
| **후조건** | comments 테이블에 레코드 생성 |
| **예외/에러 코드** | `POST_NOT_FOUND` / `PARENT_COMMENT_NOT_FOUND` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |
| **비동기 처리** | **Yes** - `@NotifyEvent` 게시글 작성자에게 댓글 알림을 Redis Queue에 비동기 발행 |

---

### 11.2 댓글 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `COMMENT-002` |
| **엔드포인트** | `GET /api/v1/board/comments` |
| **책임** | 특정 게시글의 댓글 목록 반환 (계층형) |
| **입력** | Query: `postId (required)` |
| **출력 (성공)** | `200 OK` - `List<CommentResponseDto>` (replies 포함) |
| **출력 (실패)** | `400 Bad Request` - postId 누락 |
| **전제조건** | 유효한 postId |
| **후조건** | 없음 |
| **예외/에러 코드** | `MISSING_POST_ID` |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | 불필요 |

---

### 11.3 댓글 수정

| 항목 | 내용 |
|------|------|
| **식별자** | `COMMENT-003` |
| **엔드포인트** | `PUT /api/v1/board/comments/{id}` |
| **책임** | 댓글 내용 수정 |
| **입력** | Path: `id`, Body: `CommentUpdateRequestDto` (content) |
| **출력 (성공)** | `200 OK` - `CommentResponseDto` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 본인 댓글 |
| **후조건** | comments 업데이트 |
| **예외/에러 코드** | `COMMENT_NOT_FOUND` / `NOT_AUTHOR` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

### 11.4 댓글 삭제

| 항목 | 내용 |
|------|------|
| **식별자** | `COMMENT-004` |
| **엔드포인트** | `DELETE /api/v1/board/comments/{id}` |
| **책임** | 댓글 삭제 (Soft Delete) |
| **입력** | Path: `id` |
| **출력 (성공)** | `204 No Content` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 본인 댓글 또는 관리자 |
| **후조건** | comments.deleted_at 설정 |
| **예외/에러 코드** | `COMMENT_NOT_FOUND` / `NOT_AUTHOR` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

## 12. 첨부파일 API

### 12.1 단일 파일 업로드

| 항목 | 내용 |
|------|------|
| **식별자** | `ATTACH-001` |
| **엔드포인트** | `POST /api/v1/attachments/upload` |
| **책임** | 파일을 서버에 저장하고 첨부파일 ID 반환 |
| **입력** | `multipart/form-data` - file: MultipartFile, attachmentType: string |
| **출력 (성공)** | `200 OK` - `{ "status": "SUCCESS", "data": AttachmentResponseDto }` |
| **출력 (실패)** | `500 Internal Server Error` |
| **전제조건** | 허용된 파일 형식, 크기 제한 이내 |
| **후조건** | attachments 테이블에 레코드 생성, 파일 저장 |
| **예외/에러 코드** | `INVALID_FILE_TYPE` / `FILE_TOO_LARGE` |
| **성능 제약** | 응답시간 < 5초 (파일 크기에 따라 변동) |
| **트랜잭션/락** | **Required** |

---

### 12.2 다중 파일 업로드

| 항목 | 내용 |
|------|------|
| **식별자** | `ATTACH-002` |
| **엔드포인트** | `POST /api/v1/attachments/upload/multiple` |
| **책임** | 여러 파일을 한 번에 업로드 |
| **입력** | `multipart/form-data` - files: List<MultipartFile>, attachmentType: string |
| **출력 (성공)** | `200 OK` - `{ "status": "SUCCESS", "data": List<AttachmentResponseDto> }` |
| **출력 (실패)** | `500 Internal Server Error` |
| **전제조건** | 허용된 파일 형식, 크기 제한 이내 |
| **후조건** | 각 파일별 attachments 레코드 생성 |
| **예외/에러 코드** | `INVALID_FILE_TYPE` / `FILE_TOO_LARGE` |
| **성능 제약** | 응답시간 < 10초 |
| **트랜잭션/락** | **Required** |

---

### 12.3 파일 다운로드

| 항목 | 내용 |
|------|------|
| **식별자** | `ATTACH-003` |
| **엔드포인트** | `GET /api/v1/attachments/{attachmentId}/download` |
| **책임** | 파일 다운로드 (Content-Disposition 헤더 설정) |
| **입력** | Path: `attachmentId` |
| **출력 (성공)** | `200 OK` - 파일 스트림 (적절한 Content-Type) |
| **출력 (실패)** | `404 Not Found` |
| **전제조건** | 존재하는 첨부파일 |
| **후조건** | 없음 |
| **예외/에러 코드** | `ATTACHMENT_NOT_FOUND` |
| **성능 제약** | 파일 크기에 따라 변동 |
| **트랜잭션/락** | 불필요 |

---

### 12.4 첨부파일 정보 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `ATTACH-004` |
| **엔드포인트** | `GET /api/v1/attachments/{attachmentId}` |
| **책임** | 첨부파일 메타데이터 반환 |
| **입력** | Path: `attachmentId` |
| **출력 (성공)** | `200 OK` - `AttachmentResponseDto` (id, originalName, storedName, size, type) |
| **출력 (실패)** | `404 Not Found` |
| **전제조건** | 존재하는 첨부파일 |
| **후조건** | 없음 |
| **예외/에러 코드** | `ATTACHMENT_NOT_FOUND` |
| **성능 제약** | 응답시간 < 100ms |
| **트랜잭션/락** | 불필요 |

---

### 12.5 첨부파일 삭제

| 항목 | 내용 |
|------|------|
| **식별자** | `ATTACH-005` |
| **엔드포인트** | `DELETE /api/v1/attachments/{attachmentId}` |
| **책임** | 첨부파일 삭제 (DB + 물리 파일) |
| **입력** | Path: `attachmentId` |
| **출력 (성공)** | `200 OK` - `{ "status": "SUCCESS", "message": "파일이 삭제되었습니다." }` |
| **출력 (실패)** | `500 Internal Server Error` |
| **전제조건** | 존재하는 첨부파일, 삭제 권한 |
| **후조건** | attachments 삭제, 물리 파일 삭제 |
| **예외/에러 코드** | `ATTACHMENT_NOT_FOUND` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

## 13. 과제 API

### 13.1 과제 등록

| 항목 | 내용 |
|------|------|
| **식별자** | `ASSIGN-001` |
| **엔드포인트** | `POST /api/v1/assignments` |
| **책임** | 강의에 새로운 과제 등록 |
| **입력** | `AssignmentCreateRequestDto` (courseId, title, content, dueDate, maxScore) |
| **출력 (성공)** | `201 Created` - `AssignmentResponseDto` |
| **출력 (실패)** | `400 Bad Request` / `403 Forbidden` - 교수 권한 필요 |
| **전제조건** | 교수 권한, 본인 강의 |
| **후조건** | assignments, posts 테이블에 레코드 생성, 수강생에게 알림 발송 |
| **예외/에러 코드** | `COURSE_NOT_FOUND` / `NOT_PROFESSOR` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | **Required** |

---

### 13.2 과제 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `ASSIGN-002` |
| **엔드포인트** | `GET /api/v1/assignments` |
| **책임** | 강의별 또는 전체 과제 목록 반환 |
| **입력** | Query: `courseId (optional)` |
| **출력 (성공)** | `200 OK` - `List<AssignmentResponseDto>` |
| **출력 (실패)** | `500 Internal Server Error` |
| **전제조건** | 유효한 Access Token |
| **후조건** | 없음 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | 불필요 |

---

### 13.3 과제 상세 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `ASSIGN-003` |
| **엔드포인트** | `GET /api/v1/assignments/{id}` |
| **책임** | 과제 상세 정보 반환 (Post ID로 조회) |
| **입력** | Path: `id (postId)` |
| **출력 (성공)** | `200 OK` - `AssignmentResponseDto` |
| **출력 (실패)** | `404 Not Found` |
| **전제조건** | 존재하는 과제 |
| **후조건** | 없음 |
| **예외/에러 코드** | `ASSIGNMENT_NOT_FOUND` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

### 13.4 과제 수정

| 항목 | 내용 |
|------|------|
| **식별자** | `ASSIGN-004` |
| **엔드포인트** | `PUT /api/v1/assignments/{id}` |
| **책임** | 과제 정보 수정 |
| **입력** | Path: `id`, Body: `AssignmentUpdateRequestDto` |
| **출력 (성공)** | `200 OK` - `AssignmentResponseDto` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 교수 권한, 본인이 출제한 과제 |
| **후조건** | assignments, posts 업데이트 |
| **예외/에러 코드** | `ASSIGNMENT_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | **Required** |

---

### 13.5 과제 삭제

| 항목 | 내용 |
|------|------|
| **식별자** | `ASSIGN-005` |
| **엔드포인트** | `DELETE /api/v1/assignments/{id}` |
| **책임** | 과제 삭제 |
| **입력** | Path: `id` |
| **출력 (성공)** | `204 No Content` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 교수 권한, 본인이 출제한 과제 |
| **후조건** | assignments, posts 삭제 |
| **예외/에러 코드** | `ASSIGNMENT_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

### 13.6 과제 제출

| 항목 | 내용 |
|------|------|
| **식별자** | `ASSIGN-006` |
| **엔드포인트** | `POST /api/v1/assignments/{id}/submit` |
| **책임** | 학생이 과제 답안 제출 |
| **입력** | Path: `id`, Body: `AssignmentSubmissionRequestDto` (content, attachmentIds[]) |
| **출력 (성공)** | `201 Created` - `AssignmentSubmissionResponseDto` |
| **출력 (실패)** | `400 Bad Request` - 마감일 초과 / `403 Forbidden` - 수강생 아님 |
| **전제조건** | 학생 권한, 해당 강의 수강 중, 마감일 이전 (또는 재제출 허용) |
| **후조건** | assignment_submissions 테이블에 레코드 생성 |
| **예외/에러 코드** | `PAST_DUE_DATE` - 마감일 초과 / `NOT_ENROLLED` - 미수강 / `ALREADY_SUBMITTED` - 이미 제출 |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | **Required** |

---

### 13.7 과제 제출 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `ASSIGN-007` |
| **엔드포인트** | `GET /api/v1/assignments/{id}/submissions` |
| **책임** | 해당 과제의 모든 제출 목록 반환 (교수용) |
| **입력** | Path: `id` |
| **출력 (성공)** | `200 OK` - `List<AssignmentSubmissionResponseDto>` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 교수 권한, 본인 강의 과제 |
| **후조건** | 없음 |
| **예외/에러 코드** | `ASSIGNMENT_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | 불필요 |

---

### 13.8 과제 채점

| 항목 | 내용 |
|------|------|
| **식별자** | `ASSIGN-008` |
| **엔드포인트** | `PUT /api/v1/assignments/submissions/{submissionId}/grade` |
| **책임** | 제출된 과제에 점수 및 피드백 부여 |
| **입력** | Path: `submissionId`, Body: `AssignmentGradeRequestDto` (score, feedback) |
| **출력 (성공)** | `200 OK` - `AssignmentSubmissionResponseDto` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 교수 권한, 본인 강의 과제 제출물 |
| **후조건** | assignment_submissions.score, feedback 업데이트, 학생에게 알림 발송 |
| **예외/에러 코드** | `SUBMISSION_NOT_FOUND` / `NOT_OWNER` / `INVALID_SCORE` - 최대 점수 초과 |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | **Required** |

---

### 13.9 내 제출 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `ASSIGN-009` |
| **엔드포인트** | `GET /api/v1/assignments/{id}/my-submission` |
| **책임** | 학생 본인의 과제 제출 정보 조회 |
| **입력** | Path: `id` |
| **출력 (성공)** | `200 OK` - `AssignmentSubmissionResponseDto` |
| **출력 (실패)** | `404 Not Found` - 제출 없음 |
| **전제조건** | 학생 권한 |
| **후조건** | 없음 |
| **예외/에러 코드** | `SUBMISSION_NOT_FOUND` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

### 13.10 재제출 허용

| 항목 | 내용 |
|------|------|
| **식별자** | `ASSIGN-010` |
| **엔드포인트** | `POST /api/v1/assignments/submissions/{submissionId}/allow-resubmission` |
| **책임** | 학생에게 과제 재제출 허용 |
| **입력** | Path: `submissionId`, Query: `deadline (optional, ISO DateTime)` |
| **출력 (성공)** | `200 OK` - `AssignmentSubmissionResponseDto` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 교수 권한, 본인 강의 과제 |
| **후조건** | assignment_submissions.can_resubmit 설정, 새 마감일 설정 |
| **예외/에러 코드** | `SUBMISSION_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

## 14. 알림 API

### 14.1 알림 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `NOTIF-001` |
| **엔드포인트** | `GET /api/notifications` |
| **책임** | 사용자의 알림 목록을 커서 기반 페이지네이션으로 반환 |
| **입력** | Query: `cursor (optional), size (default: 20, max: 100), unreadOnly (default: false)` |
| **출력 (성공)** | `200 OK` - `NotificationCursorResponseDto` (notifications[], nextCursor, hasNext) |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 유효한 Access Token |
| **후조건** | 없음 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

### 14.2 알림 상세 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `NOTIF-002` |
| **엔드포인트** | `GET /api/notifications/{notificationId}` |
| **책임** | 특정 알림의 상세 정보 반환 |
| **입력** | Path: `notificationId` |
| **출력 (성공)** | `200 OK` - `NotificationResponseDto` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` - 본인 알림 아님 |
| **전제조건** | 본인의 알림 |
| **후조건** | 없음 |
| **예외/에러 코드** | `NOTIFICATION_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 100ms |
| **트랜잭션/락** | 불필요 |

---

### 14.3 읽지 않은 알림 개수 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `NOTIF-003` |
| **엔드포인트** | `GET /api/notifications/unread-count` |
| **책임** | 읽지 않은 알림 개수 반환 |
| **입력** | Header: `Authorization` |
| **출력 (성공)** | `200 OK` - `{ "unreadCount": number }` |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 유효한 Access Token |
| **후조건** | 없음 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 50ms |
| **트랜잭션/락** | 불필요 |

---

### 14.4 알림 읽음 처리

| 항목 | 내용 |
|------|------|
| **식별자** | `NOTIF-004` |
| **엔드포인트** | `PATCH /api/notifications/{notificationId}/read` |
| **책임** | 특정 알림을 읽음 상태로 변경 |
| **입력** | Path: `notificationId` |
| **출력 (성공)** | `200 OK` - `NotificationResponseDto` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 본인의 알림 |
| **후조건** | notifications.read_at 설정 |
| **예외/에러 코드** | `NOTIFICATION_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 100ms |
| **트랜잭션/락** | **Required** |

---

### 14.5 모든 알림 읽음 처리

| 항목 | 내용 |
|------|------|
| **식별자** | `NOTIF-005` |
| **엔드포인트** | `PATCH /api/notifications/read-all` |
| **책임** | 사용자의 모든 알림을 읽음 상태로 변경 |
| **입력** | Header: `Authorization` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "updatedCount": number }` |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 유효한 Access Token |
| **후조건** | 모든 알림의 read_at 설정 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | **Required** - 벌크 업데이트 |

---

### 14.6 알림 삭제

| 항목 | 내용 |
|------|------|
| **식별자** | `NOTIF-006` |
| **엔드포인트** | `DELETE /api/notifications/{notificationId}` |
| **책임** | 특정 알림 삭제 |
| **입력** | Path: `notificationId` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "message": "알림이 삭제되었습니다." }` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 본인의 알림 |
| **후조건** | notifications 삭제 |
| **예외/에러 코드** | `NOTIFICATION_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 100ms |
| **트랜잭션/락** | **Required** |

---

### 14.7 읽은 알림 일괄 삭제

| 항목 | 내용 |
|------|------|
| **식별자** | `NOTIF-007` |
| **엔드포인트** | `DELETE /api/notifications/read` |
| **책임** | 읽은 알림 모두 삭제 |
| **입력** | Header: `Authorization` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "deletedCount": number }` |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 유효한 Access Token |
| **후조건** | 읽은 알림 모두 삭제 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | **Required** - 벌크 삭제 |

---

### 14.8 모든 알림 삭제

| 항목 | 내용 |
|------|------|
| **식별자** | `NOTIF-008` |
| **엔드포인트** | `DELETE /api/notifications/all` |
| **책임** | 사용자의 모든 알림 삭제 |
| **입력** | Header: `Authorization` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "deletedCount": number }` |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 유효한 Access Token |
| **후조건** | 해당 사용자의 모든 알림 삭제 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | **Required** - 벌크 삭제 |

---

## 15. 대화방 API

### 15.1 대화방 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `CONV-001` |
| **엔드포인트** | `GET /api/v1/conversations` |
| **책임** | 사용자의 대화방 목록 반환 (최근 메시지순 정렬) |
| **입력** | Header: `Authorization` |
| **출력 (성공)** | `200 OK` - `List<ConversationListResponseDto>` (lastMessage, unreadCount 포함) |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 유효한 Access Token |
| **후조건** | 없음 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

### 15.2 대화방 생성/조회

| 항목 | 내용 |
|------|------|
| **식별자** | `CONV-002` |
| **엔드포인트** | `POST /api/v1/conversations/with/{otherUserId}` |
| **책임** | 상대방과의 대화방 생성 또는 기존 대화방 반환 |
| **입력** | Path: `otherUserId` |
| **출력 (성공)** | `200 OK` - `ConversationResponseDto` |
| **출력 (실패)** | `404 Not Found` - 상대방 사용자 없음 |
| **전제조건** | 존재하는 상대방 사용자 |
| **후조건** | 대화방 없으면 conversations 생성 |
| **예외/에러 코드** | `USER_NOT_FOUND` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

### 15.3 대화방 상세 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `CONV-003` |
| **엔드포인트** | `GET /api/v1/conversations/{conversationId}` |
| **책임** | 대화방 상세 정보 반환 |
| **입력** | Path: `conversationId` |
| **출력 (성공)** | `200 OK` - `ConversationResponseDto` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` - 참여자 아님 |
| **전제조건** | 대화방 참여자 |
| **후조건** | 없음 |
| **예외/에러 코드** | `CONVERSATION_NOT_FOUND` / `NOT_PARTICIPANT` |
| **성능 제약** | 응답시간 < 100ms |
| **트랜잭션/락** | 불필요 |

---

### 15.4 대화방 삭제

| 항목 | 내용 |
|------|------|
| **식별자** | `CONV-004` |
| **엔드포인트** | `DELETE /api/v1/conversations/{conversationId}` |
| **책임** | 대화방 삭제 (해당 사용자에게만 숨김 처리) |
| **입력** | Path: `conversationId` |
| **출력 (성공)** | `204 No Content` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 대화방 참여자 |
| **후조건** | 해당 사용자의 대화방 숨김 처리 (Soft Delete) |
| **예외/에러 코드** | `CONVERSATION_NOT_FOUND` / `NOT_PARTICIPANT` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

### 15.5 대화방 읽음 처리

| 항목 | 내용 |
|------|------|
| **식별자** | `CONV-005` |
| **엔드포인트** | `POST /api/v1/conversations/{conversationId}/read` |
| **책임** | 대화방의 모든 메시지를 읽음 처리 |
| **입력** | Path: `conversationId` |
| **출력 (성공)** | `200 OK` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 대화방 참여자 |
| **후조건** | 해당 대화방의 메시지 read_at 설정 |
| **예외/에러 코드** | `CONVERSATION_NOT_FOUND` / `NOT_PARTICIPANT` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

### 15.6 전체 안읽음 수 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `CONV-006` |
| **엔드포인트** | `GET /api/v1/conversations/unread-count` |
| **책임** | 모든 대화방의 안읽은 메시지 총 개수 반환 |
| **입력** | Header: `Authorization` |
| **출력 (성공)** | `200 OK` - `count: number` |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 유효한 Access Token |
| **후조건** | 없음 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 100ms |
| **트랜잭션/락** | 불필요 |

---

## 16. 메시지 API

### 16.1 메시지 전송

| 항목 | 내용 |
|------|------|
| **식별자** | `MSG-001` |
| **엔드포인트** | `POST /api/v1/messages` |
| **책임** | 대화방에 메시지 전송 |
| **입력** | `MessageSendRequestDto` (conversationId, content) |
| **출력 (성공)** | `200 OK` - `MessageResponseDto` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 대화방 참여자 |
| **후조건** | messages 테이블에 레코드 생성, SSE로 상대방에게 실시간 전송 |
| **예외/에러 코드** | `CONVERSATION_NOT_FOUND` / `NOT_PARTICIPANT` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

### 16.2 메시지 일괄 전송

| 항목 | 내용 |
|------|------|
| **식별자** | `MSG-002` |
| **엔드포인트** | `POST /api/v1/messages/bulk` |
| **책임** | 여러 메시지를 한 번에 전송 |
| **입력** | `MessageBulkSendRequestDto` (messages[]) |
| **출력 (성공)** | `200 OK` - `List<MessageResponseDto>` |
| **출력 (실패)** | `400 Bad Request` |
| **전제조건** | 모든 대화방의 참여자 |
| **후조건** | 각 메시지별 messages 생성, SSE 전송 |
| **예외/에러 코드** | `CONVERSATION_NOT_FOUND` / `NOT_PARTICIPANT` |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | **Required** |

---

### 16.3 대화 내역 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `MSG-003` |
| **엔드포인트** | `GET /api/v1/messages/conversations/{conversationId}` |
| **책임** | 대화방의 메시지 목록 반환 (커서 기반 페이지네이션) |
| **입력** | Path: `conversationId`, Query: `cursor, size (default: 20)` |
| **출력 (성공)** | `200 OK` - `MessageCursorResponseDto` (messages[], nextCursor, hasNext) |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 대화방 참여자 |
| **후조건** | 없음 |
| **예외/에러 코드** | `CONVERSATION_NOT_FOUND` / `NOT_PARTICIPANT` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

### 16.4 메시지 삭제

| 항목 | 내용 |
|------|------|
| **식별자** | `MSG-004` |
| **엔드포인트** | `DELETE /api/v1/messages/{messageId}` |
| **책임** | 메시지 삭제 (본인 메시지만) |
| **입력** | Path: `messageId` |
| **출력 (성공)** | `204 No Content` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 본인이 보낸 메시지 |
| **후조건** | messages 삭제 |
| **예외/에러 코드** | `MESSAGE_NOT_FOUND` / `NOT_SENDER` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

### 16.5 메시지 읽음 처리

| 항목 | 내용 |
|------|------|
| **식별자** | `MSG-005` |
| **엔드포인트** | `POST /api/v1/messages/conversations/{conversationId}/read` |
| **책임** | 대화방의 메시지를 읽음 처리 |
| **입력** | Path: `conversationId` |
| **출력 (성공)** | `200 OK` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 대화방 참여자 |
| **후조건** | 메시지 read_at 설정 |
| **예외/에러 코드** | `CONVERSATION_NOT_FOUND` / `NOT_PARTICIPANT` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | **Required** |

---

## 17. SSE API

### 17.1 SSE 구독

| 항목 | 내용 |
|------|------|
| **식별자** | `SSE-001` |
| **엔드포인트** | `GET /api/v1/sse/subscribe` |
| **책임** | 실시간 알림 및 메시지 수신을 위한 SSE 연결 수립 |
| **입력** | Header: `Authorization` |
| **출력 (성공)** | `200 OK` - `text/event-stream` (SseEmitter) |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 유효한 Access Token |
| **후조건** | SSE 연결 유지, 실시간 이벤트 수신 가능 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 연결 유지 (타임아웃: 30분) |
| **트랜잭션/락** | 불필요 |

**이벤트 종류:**
- `message` - 새 메시지 수신
- `notification` - 새 알림 수신
- `heartbeat` - 연결 유지 (30초 간격)

---

## 18. 출석 API

### 18.1 강의 전체 출석 현황 조회 (교수)

| 항목 | 내용 |
|------|------|
| **식별자** | `ATTEND-001` |
| **엔드포인트** | `GET /api/v1/professor/courses/{courseId}/attendance` |
| **책임** | 강의의 전체 출석 통계 및 현황 반환 |
| **입력** | Path: `courseId` |
| **출력 (성공)** | `200 OK` - `CourseAttendanceOverviewDto` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 교수 권한, 본인 강의 |
| **후조건** | 없음 |
| **예외/에러 코드** | `COURSE_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | 불필요 |

---

### 18.2 학생별 출석 목록 조회 (교수)

| 항목 | 내용 |
|------|------|
| **식별자** | `ATTEND-002` |
| **엔드포인트** | `GET /api/v1/professor/courses/{courseId}/attendance/students` |
| **책임** | 강의 수강생별 출석 현황 반환 |
| **입력** | Path: `courseId` |
| **출력 (성공)** | `200 OK` - `List<StudentAttendanceDto>` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 교수 권한, 본인 강의 |
| **후조건** | 없음 |
| **예외/에러 코드** | `COURSE_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | 불필요 |

---

### 18.3 주차별 학생 출석 현황 조회 (교수)

| 항목 | 내용 |
|------|------|
| **식별자** | `ATTEND-003` |
| **엔드포인트** | `GET /api/v1/professor/courses/{courseId}/weeks/{weekId}/attendance` |
| **책임** | 특정 주차의 학생별 출석 현황 반환 |
| **입력** | Path: `courseId, weekId` |
| **출력 (성공)** | `200 OK` - `List<WeekStudentAttendanceDto>` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` |
| **전제조건** | 교수 권한, 본인 강의, 유효한 주차 |
| **후조건** | 없음 |
| **예외/에러 코드** | `COURSE_NOT_FOUND` / `WEEK_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | 불필요 |

---

### 18.4 내 전체 출석 현황 조회 (학생)

| 항목 | 내용 |
|------|------|
| **식별자** | `ATTEND-004` |
| **엔드포인트** | `GET /api/v1/attendance/my` |
| **책임** | 학생의 전체 수강 강의별 출석 현황 반환 |
| **입력** | Header: `Authorization` |
| **출력 (성공)** | `200 OK` - `List<CourseAttendanceSummaryDto>` |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 학생 권한 |
| **후조건** | 없음 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | 불필요 |

---

### 18.5 특정 강의 출석 현황 조회 (학생)

| 항목 | 내용 |
|------|------|
| **식별자** | `ATTEND-005` |
| **엔드포인트** | `GET /api/v1/attendance/courses/{courseId}` |
| **책임** | 학생의 특정 강의 출석 상세 현황 반환 |
| **입력** | Path: `courseId` |
| **출력 (성공)** | `200 OK` - `CourseAttendanceDto` |
| **출력 (실패)** | `404 Not Found` / `403 Forbidden` - 수강 중 아님 |
| **전제조건** | 학생 권한, 해당 강의 수강 중 |
| **후조건** | 없음 |
| **예외/에러 코드** | `COURSE_NOT_FOUND` / `NOT_ENROLLED` |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | 불필요 |

---

### 18.6 주차별 출석 상세 조회 (학생)

| 항목 | 내용 |
|------|------|
| **식별자** | `ATTEND-006` |
| **엔드포인트** | `GET /api/v1/attendance/courses/{courseId}/weeks/{weekId}` |
| **책임** | 학생의 특정 주차 출석 상세 반환 |
| **입력** | Path: `courseId, weekId` |
| **출력 (성공)** | `200 OK` - `WeekAttendanceDto` |
| **출력 (실패)** | `404 Not Found` |
| **전제조건** | 학생 권한, 해당 강의 수강 중, 유효한 주차 |
| **후조건** | 없음 |
| **예외/에러 코드** | `COURSE_NOT_FOUND` / `WEEK_NOT_FOUND` / `NOT_ENROLLED` |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

## 19. 대시보드 API

### 19.1 미제출 과제 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `DASH-001` |
| **엔드포인트** | `GET /api/v1/dashboard/student/pending-assignments` |
| **책임** | 마감 임박 미제출 과제 목록 반환 |
| **입력** | Query: `days (default: 7, max: 30)` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "data": List<PendingAssignmentDto>, "count": number }` |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 학생 권한 |
| **후조건** | 없음 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | 불필요 |

---

### 19.2 오늘의 강의 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `DASH-002` |
| **엔드포인트** | `GET /api/v1/dashboard/student/today-courses` |
| **책임** | 오늘 요일의 수강 강의 목록 및 시간표 반환 |
| **입력** | Header: `Authorization` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "data": List<TodayCourseDto>, "count": number }` |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 학생 권한 |
| **후조건** | 없음 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

### 19.3 최신 공지사항 목록 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `DASH-003` |
| **엔드포인트** | `GET /api/v1/dashboard/student/notices` |
| **책임** | 최신 공지사항 목록 반환 |
| **입력** | Query: `limit (default: 5, max: 10)` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "data": List<NoticeDto>, "count": number }` |
| **출력 (실패)** | `500 Internal Server Error` |
| **전제조건** | 없음 |
| **후조건** | 없음 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

### 19.4 수강 현황 요약 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `DASH-004` |
| **엔드포인트** | `GET /api/v1/dashboard/student/enrollment-summary` |
| **책임** | 수강 중인 과목 수 및 총 학점 반환 |
| **입력** | Header: `Authorization` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "data": EnrollmentSummaryDto }` |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 학생 권한 |
| **후조건** | 없음 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 200ms |
| **트랜잭션/락** | 불필요 |

---

## 20. 시험/퀴즈 API

### 20.1 시험/퀴즈 목록 조회 (학생)

| 항목 | 내용 |
|------|------|
| **식별자** | `EXAM-001` |
| **엔드포인트** | `GET /api/v1/exams` |
| **책임** | 학생이 수강 중인 강의의 시험/퀴즈 목록을 반환 (시작시간 전 항목 숨김) |
| **입력** | Query: `courseId (required)`, `examType (required: QUIZ/MIDTERM/FINAL)` |
| **출력 (성공)** | `200 OK` - `List<AssessmentListItemResponseDto>` |
| **출력 (실패)** | `400 Bad Request` / `401 Unauthorized` |
| **전제조건** | 학생 권한, 해당 강의 수강 중 |
| **후조건** | 없음 |
| **예외/에러 코드** | `UNAUTHORIZED` / `COURSE_NOT_FOUND` / `NOT_ENROLLED` |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | 불필요 |

---

### 20.2 시험/퀴즈 상세 조회 (학생)

| 항목 | 내용 |
|------|------|
| **식별자** | `EXAM-002` |
| **엔드포인트** | `GET /api/v1/exams/{examId}` |
| **책임** | 시험/퀴즈 상세 조회 (학생에게는 정답이 마스킹된 questionData 제공) |
| **입력** | Path: `examId (required)` |
| **출력 (성공)** | `200 OK` - `AssessmentDetailResponseDto` |
| **출력 (실패)** | `400 Bad Request` / `401 Unauthorized` / `404 Not Found` |
| **전제조건** | 학생 권한, 해당 강의 수강 중 |
| **후조건** | 없음 |
| **예외/에러 코드** | `EXAM_NOT_FOUND` / `NOT_ENROLLED` |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | 불필요 |

---

### 20.3 응시 시작 (학생)

| 항목 | 내용 |
|------|------|
| **식별자** | `EXAM-003` |
| **엔드포인트** | `POST /api/v1/exams/{examId}/start` |
| **책임** | 응시 시작 처리 (응시 레코드 생성/조회) |
| **입력** | Path: `examId (required)` |
| **출력 (성공)** | `200 OK` - `AttemptStartResponseDto` |
| **출력 (실패)** | `400 Bad Request` / `401 Unauthorized` |
| **전제조건** | 학생 권한, 시험 시작 가능 시간/상태 |
| **후조건** | 응시 레코드 생성 또는 기존 응시 레코드 반환 |
| **예외/에러 코드** | `NOT_STARTED` / `ALREADY_FINISHED` |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | **Required** |

---

### 20.4 최종 제출 (학생)

| 항목 | 내용 |
|------|------|
| **식별자** | `EXAM-004` |
| **엔드포인트** | `POST /api/v1/exams/results/{attemptId}/submit` |
| **책임** | 시험/퀴즈 최종 제출 처리 |
| **입력** | Path: `attemptId (required)`, Body: `AttemptSubmitRequestDto` |
| **출력 (성공)** | `200 OK` - `AttemptSubmitResponseDto` |
| **출력 (실패)** | `400 Bad Request` / `401 Unauthorized` |
| **전제조건** | 학생 권한, 본인 응시, 제출 가능 상태 |
| **후조건** | 응시 결과 저장, 퀴즈인 경우 자동 채점 반영 |
| **예외/에러 코드** | `ATTEMPT_NOT_FOUND` / `NOT_OWNER` / `INVALID_STATE` |
| **성능 제약** | 응답시간 < 1s |
| **트랜잭션/락** | **Required** |

---

### 20.5 시험/퀴즈 목록 조회 (교수)

| 항목 | 내용 |
|------|------|
| **식별자** | `EXAM-005` |
| **엔드포인트** | `GET /api/v1/professor/exams` |
| **책임** | 교수 강의의 시험/퀴즈 목록 조회 (시작 전 포함 전체 조회 가능) |
| **입력** | Query: `courseId (required)`, `examType (required: QUIZ/MIDTERM/FINAL)` |
| **출력 (성공)** | `200 OK` - `List<AssessmentListItemResponseDto>` |
| **출력 (실패)** | `400 Bad Request` / `401 Unauthorized` |
| **전제조건** | 교수 권한, 본인 강의 |
| **후조건** | 없음 |
| **예외/에러 코드** | `NOT_OWNER` / `COURSE_NOT_FOUND` |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | 불필요 |

---

### 20.6 시험/퀴즈 상세 조회 (교수)

| 항목 | 내용 |
|------|------|
| **식별자** | `EXAM-006` |
| **엔드포인트** | `GET /api/v1/professor/exams/{examId}` |
| **책임** | 시험/퀴즈 상세 조회 (교수는 정답 포함 원본 questionData 조회 가능) |
| **입력** | Path: `examId (required)` |
| **출력 (성공)** | `200 OK` - `AssessmentDetailResponseDto` |
| **출력 (실패)** | `400 Bad Request` / `401 Unauthorized` / `404 Not Found` |
| **전제조건** | 교수 권한, 본인 강의 |
| **후조건** | 없음 |
| **예외/에러 코드** | `EXAM_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | 불필요 |

---

### 20.7 응시자/응시 결과 목록 조회 (교수)

| 항목 | 내용 |
|------|------|
| **식별자** | `EXAM-007` |
| **엔드포인트** | `GET /api/v1/professor/exams/{examId}/attempts` |
| **책임** | 응시자/응시 결과 목록 조회 |
| **입력** | Path: `examId (required)`, Query: `status (optional: ALL/SUBMITTED/IN_PROGRESS, default ALL)` |
| **출력 (성공)** | `200 OK` - `List<ProfessorAttemptListItemResponseDto>` |
| **출력 (실패)** | `400 Bad Request` / `401 Unauthorized` |
| **전제조건** | 교수 권한, 본인 강의 |
| **후조건** | 없음 |
| **예외/에러 코드** | `EXAM_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | 불필요 |

---

### 20.8 응시 결과 상세 조회 (교수)

| 항목 | 내용 |
|------|------|
| **식별자** | `EXAM-008` |
| **엔드포인트** | `GET /api/v1/professor/exams/results/{attemptId}` |
| **책임** | 응시 결과 상세 조회 (답안 포함) |
| **입력** | Path: `attemptId (required)` |
| **출력 (성공)** | `200 OK` - `ProfessorAttemptDetailResponseDto` |
| **출력 (실패)** | `400 Bad Request` / `401 Unauthorized` / `404 Not Found` |
| **전제조건** | 교수 권한, 본인 강의 |
| **후조건** | 없음 |
| **예외/에러 코드** | `ATTEMPT_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | 불필요 |

---

### 20.9 시험/퀴즈 등록 (교수)

| 항목 | 내용 |
|------|------|
| **식별자** | `EXAM-009` |
| **엔드포인트** | `POST /api/v1/boards/{boardType}/exams` |
| **책임** | 시험/퀴즈 생성 |
| **입력** | Path: `boardType (required)`, Body: `AssessmentCreateRequestDto` |
| **출력 (성공)** | `201 Created` - `AssessmentDetailResponseDto` |
| **출력 (실패)** | `400 Bad Request` / `401 Unauthorized` |
| **전제조건** | 교수 권한, 본인 강의 |
| **후조건** | 시험/퀴즈 레코드 생성 |
| **예외/에러 코드** | `NOT_OWNER` / `INVALID_INPUT` |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | **Required** |

---

### 20.10 시험/퀴즈 수정 (교수)

| 항목 | 내용 |
|------|------|
| **식별자** | `EXAM-010` |
| **엔드포인트** | `PUT /api/v1/exams/{examId}/edit` |
| **책임** | 시험/퀴즈 수정 |
| **입력** | Path: `examId (required)`, Body: `AssessmentUpdateRequestDto` |
| **출력 (성공)** | `200 OK` - `AssessmentDetailResponseDto` |
| **출력 (실패)** | `400 Bad Request` / `401 Unauthorized` / `404 Not Found` |
| **전제조건** | 교수 권한, 본인 강의 |
| **후조건** | 시험/퀴즈 수정 반영 |
| **예외/에러 코드** | `EXAM_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | **Required** |

---

### 20.11 시험/퀴즈 삭제 (교수)

| 항목 | 내용 |
|------|------|
| **식별자** | `EXAM-011` |
| **엔드포인트** | `DELETE /api/v1/exams/{examId}/delete` |
| **책임** | 시험/퀴즈 삭제 |
| **입력** | Path: `examId (required)` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "message": "삭제되었습니다" }` |
| **출력 (실패)** | `400 Bad Request` / `401 Unauthorized` / `404 Not Found` |
| **전제조건** | 교수 권한, 본인 강의 |
| **후조건** | 시험/퀴즈 삭제 처리 |
| **예외/에러 코드** | `EXAM_NOT_FOUND` / `NOT_OWNER` |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | **Required** |

---

### 20.12 시험 채점 (교수)

| 항목 | 내용 |
|------|------|
| **식별자** | `EXAM-012` |
| **엔드포인트** | `PUT /api/v1/exams/results/{attemptId}/grade` |
| **책임** | 시험(주관식 포함) 채점 처리 (퀴즈는 자동채점) |
| **입력** | Path: `attemptId (required)`, Body: `AttemptGradeRequestDto` |
| **출력 (성공)** | `200 OK` - `AttemptGradeResponseDto` |
| **출력 (실패)** | `400 Bad Request` / `401 Unauthorized` / `404 Not Found` |
| **전제조건** | 교수 권한, 본인 강의, 제출 상태 |
| **후조건** | 응시 결과 점수/채점 상태 반영 |
| **예외/에러 코드** | `ATTEMPT_NOT_FOUND` / `NOT_OWNER` / `INVALID_STATE` |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | **Required** |

---

## 21. 성적 API

### 21.1 학생 성적 조회

| 항목 | 내용 |
|------|------|
| **식별자** | `GRADE-001` |
| **엔드포인트** | `GET /api/v1/student/grades` |
| **책임** | 학생 본인 성적 조회 (PUBLISHED 성적만 반환, 학기 필터 가능) |
| **입력** | Query: `academicTermId (optional)` |
| **출력 (성공)** | `200 OK` - `List<StudentGradeResponseDto>` |
| **출력 (실패)** | `401 Unauthorized` |
| **전제조건** | 학생 권한 |
| **후조건** | 없음 |
| **예외/에러 코드** | `UNAUTHORIZED` |
| **성능 제약** | 응답시간 < 300ms |
| **트랜잭션/락** | 불필요 |

---

### 21.2 담당 강의 수강생 성적 전체 조회 (교수)

| 항목 | 내용 |
|------|------|
| **식별자** | `GRADE-002` |
| **엔드포인트** | `GET /api/v1/professor/courses/{courseId}/grades` |
| **책임** | 담당 강의 수강생 성적 전체 조회 (status 필터: ALL/PUBLISHED) |
| **입력** | Path: `courseId (required)`, Query: `status (optional: ALL/PUBLISHED, default ALL)` |
| **출력 (성공)** | `200 OK` - `List<ProfessorCourseGradesResponseDto>` |
| **출력 (실패)** | `401 Unauthorized` / `400 Bad Request` |
| **전제조건** | 교수 권한, 본인 강의 |
| **후조건** | 없음 |
| **예외/에러 코드** | `NOT_OWNER` / `COURSE_NOT_FOUND` |
| **성능 제약** | 응답시간 < 500ms |
| **트랜잭션/락** | 불필요 |

---

### 21.3 강의 성적 산출(점수 계산) 수동 실행 (교수)

| 항목 | 내용 |
|------|------|
| **식별자** | `GRADE-003` |
| **엔드포인트** | `POST /api/v1/professor/courses/{courseId}/grades/calculate` |
| **책임** | 강의 단위 성적 산출 수행 (status=GRADED, finalScore 산출) |
| **입력** | Path: `courseId (required)` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "message": "성적 산출 처리를 실행했습니다." }` |
| **출력 (실패)** | `401 Unauthorized` / `400 Bad Request` |
| **전제조건** | 교수 권한, 본인 강의, 성적산출기간(GRADE_CALCULATION) 진행 중, 채점 미완료 없음 |
| **후조건** | grades upsert 및 status=GRADED 저장 |
| **예외/에러 코드** | `NOT_OWNER` / `NOT_IN_CALCULATION_PERIOD` |
| **성능 제약** | 응답시간 < 2초 |
| **트랜잭션/락** | **Required** |

---

### 21.4 강의 성적 공개(확정/공개) 수동 실행 (교수)

| 항목 | 내용 |
|------|------|
| **식별자** | `GRADE-004` |
| **엔드포인트** | `POST /api/v1/professor/courses/{courseId}/grades/publish` |
| **책임** | 강의 단위 성적 확정 및 공개 (finalGrade 부여, status=PUBLISHED) |
| **입력** | Path: `courseId (required)` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "message": "성적 공개 처리를 실행했습니다." }` |
| **출력 (실패)** | `401 Unauthorized` / `400 Bad Request` |
| **전제조건** | 교수 권한, 본인 강의, 성적공개기간(GRADE_PUBLISH) 진행 중, 산출(GRADED) 완료 |
| **후조건** | grades finalGrade 확정 및 status=PUBLISHED 저장 |
| **예외/에러 코드** | `NOT_OWNER` / `NOT_IN_PUBLISH_PERIOD` / `NEED_CALCULATE` |
| **성능 제약** | 응답시간 < 2초 |
| **트랜잭션/락** | **Required** |

---

### 21.5 특정 학기 성적 공개(수동) (교수)

| 항목 | 내용 |
|------|------|
| **식별자** | `GRADE-005` |
| **엔드포인트** | `POST /api/v1/professor/grades/publish/terms/{academicTermId}` |
| **책임** | 특정 학기의 강의들을 대상으로 성적 공개 처리 실행 |
| **입력** | Path: `academicTermId (required)` |
| **출력 (성공)** | `200 OK` - `{ "success": true, "message": "성적 공개 처리를 실행했습니다." }` |
| **출력 (실패)** | `401 Unauthorized` / `400 Bad Request` |
| **전제조건** | 교수 권한, 성적공개기간(GRADE_PUBLISH) 진행 중 |
| **후조건** | 학기 내 공개 가능 강의들의 성적이 PUBLISHED로 전환 |
| **예외/에러 코드** | `NOT_IN_PUBLISH_PERIOD` |
| **성능 제약** | 응답시간 < 5초 |
| **트랜잭션/락** | **Required** |

---

### 21.6 공개기간 대상 성적 공개 처리(배치 트리거) (교수)

| 항목 | 내용 |
|------|------|
| **식별자** | `GRADE-006` |
| **엔드포인트** | `POST /api/v1/professor/grades/publish-ended-terms` |
| **책임** | 성적 공개 기간(GRADE_PUBLISH) 대상 학기의 강의들에 대해 공개 로직을 즉시 실행 |
| **입력** | 없음 |
| **출력 (성공)** | `200 OK` - `{ "success": true, "message": "성적 공개 처리를 실행했습니다." }` |
| **출력 (실패)** | `401 Unauthorized` / `400 Bad Request` |
| **전제조건** | 교수 권한 |
| **후조건** | 공개 가능 강의들의 성적이 PUBLISHED로 전환 |
| **예외/에러 코드** | 없음 |
| **성능 제약** | 응답시간 < 5초 |
| **트랜잭션/락** | **Required** |

---

## 부록: 공통 에러 코드

| 에러 코드 | HTTP Status | 설명 |
|-----------|-------------|------|
| `UNAUTHORIZED` | 401 | 인증 필요 |
| `FORBIDDEN` | 403 | 권한 없음 |
| `NOT_FOUND` | 404 | 리소스 없음 |
| `BAD_REQUEST` | 400 | 잘못된 요청 |
| `INTERNAL_ERROR` | 500 | 서버 내부 오류 |
| `VALIDATION_ERROR` | 400 | 유효성 검증 실패 |

---

## 부록: 공통 응답 형식

### 성공 응답

```json
{
  "success": true,
  "data": { ... },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

### 실패 응답

```json
{
  "success": false,
  "message": "에러 메시지",
  "code": "ERROR_CODE"
}
```

---

**문서 작성일**: 2025-12-22
**버전**: 1.0
