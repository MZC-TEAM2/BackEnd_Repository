# Board Domain API 명세서 - 고급 기능

## 📌 개요
LMS 게시판 시스템의 좋아요/북마크, 과제/시험, 스터디모집/해시태그 REST API 명세서입니다.

**Base URL:** `http://localhost:8080/api/v1`

**인증:** JWT Bearer Token 필요

---

## ❤️ 4. 좋아요/북마크 시스템 API

### 4.1 게시글 좋아요 토글
**POST** `/boards/{boardType}/posts/{postId}/like`

#### 동작 방식
- **좋아요가 없는 경우**: 좋아요 추가
- **좋아요가 있는 경우**: 좋아요 취소 (토글 방식)

#### 요청 예시
```http
POST /api/v1/boards/FREE/posts/123/like
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시 (좋아요 추가)
```json
{
  "status": "SUCCESS",
  "message": "좋아요가 추가되었습니다",
  "data": {
    "postId": 123,
    "isLiked": true,
    "totalLikes": 15,
    "likedAt": "2024-12-08T10:30:00"
  }
}
```

#### 응답 예시 (좋아요 취소)
```json
{
  "status": "SUCCESS",
  "message": "좋아요가 취소되었습니다",
  "data": {
    "postId": 123,
    "isLiked": false,
    "totalLikes": 14,
    "canceledAt": "2024-12-08T10:31:00"
  }
}
```

### 4.2 게시글 북마크 토글
**POST** `/posts/{postId}/bookmark`

#### 동작 방식
- **북마크가 없는 경우**: 북마크 추가
- **북마크가 있는 경우**: 북마크 취소 (토글 방식)

#### 요청 Body
```json
{
  "category": "나중에_읽기"
}
```

#### 요청 예시
```http
POST /api/v1/posts/123/bookmark
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "category": "나중에_읽기"
}
```

#### 응답 예시 (북마크 추가)
```json
{
  "status": "SUCCESS", 
  "message": "북마크가 추가되었습니다",
  "data": {
    "postId": 123,
    "category": "나중에_읽기",
    "isBookmarked": true,
    "bookmarkedAt": "2024-12-08T10:30:00"
  }
}
```

#### 응답 예시 (북마크 취소)
```json
{
  "status": "SUCCESS", 
  "message": "북마크가 취소되었습니다",
  "data": {
    "postId": 123,
    "category": "나중에_읽기",
    "isBookmarked": false,
    "canceledAt": "2024-12-08T10:31:00"
  }
}
```

### 4.3 내 북마크 목록 조회
**GET** `/bookmarks/my`

#### Parameters
- `category`: 카테고리 필터 (선택)
- `boardType`: 게시판 타입 필터 (선택)
- `page`, `size`: 페이지네이션

#### 요청 예시
```http
GET /api/v1/bookmarks/my?category=나중에_읽기&page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "data": {
    "content": [
      {
        "id": 789,
        "post": {
          "id": 123,
          "title": "Spring Boot 튜토리얼",
          "boardType": "FREE",
          "author": "김개발"
        },
        "category": "나중에_읽기",
        "bookmarkedAt": "2024-12-08T10:30:00"
      }
    ]
  }
}
```

### 4.4 좋아요 많은 게시글 조회
**GET** `/boards/{boardType}/posts/popular`

#### Parameters
- `period`: 기간 (WEEK/MONTH/ALL)
- `minLikes`: 최소 좋아요 수

#### 요청 예시
```http
GET /api/v1/boards/FREE/posts/popular?period=MONTH&minLikes=10
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "data": [
    {
      "id": 123,
      "title": "유용한 개발 팁",
      "likeCount": 45,
      "viewCount": 1230,
      "author": "김개발",
      "createdAt": "2024-12-01T10:00:00"
    }
  ]
}
```

---

## 📚 5. 과제 시스템 API

### 5.1 과제 등록 (교수용)
**POST** `/assignments`

#### 요청 예시
```http
POST /api/v1/assignments
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

#### 요청 Body
```json
{
  "courseId": 101,
  "title": "Spring Boot 실습 과제",
  "content": "Spring Boot를 활용한 웹 애플리케이션 개발",
  "dueDate": "2024-12-15T23:59:00",
  "maxScore": 100,
  "submissionMethod": "BOTH",
  "lateSubmissionAllowed": true,
  "latePenaltyPercent": 10,
  "maxFileSizeMb": 20,
  "allowedFileTypes": "zip,pdf,docx,hwp"
}
```

#### 제출 방법 (SubmissionMethod)
- `FILE_UPLOAD` - 파일 업로드만
- `TEXT_INPUT` - 텍스트 입력만  
- `BOTH` - 파일 + 텍스트 모두

### 5.2 과제 목록 조회
**GET** `/courses/{courseId}/assignments`

#### Parameters
- `status`: 과제 상태 (UPCOMING/ACTIVE/OVERDUE/COMPLETED)
- `page`, `size`: 페이지네이션

#### 요청 예시
```http
GET /api/v1/courses/101/assignments?status=ACTIVE&page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시 (학생용)
```json
{
  "status": "SUCCESS",
  "data": {
    "content": [
      {
        "id": 201,
        "title": "Spring Boot 실습 과제",
        "dueDate": "2024-12-15T23:59:00",
        "maxScore": 100,
        "submissionStatus": "NOT_SUBMITTED",
        "remainingTime": "7일 9시간 29분",
        "canSubmit": true
      }
    ]
  }
}
```

### 5.3 과제 제출 (학생용)
**POST** `/assignments/{assignmentId}/submit`

#### 요청 예시
```http
POST /api/v1/assignments/201/submit
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

#### 요청 Body
```json
{
  "content": "구현한 웹 애플리케이션 설명",
  "attachmentIds": [401, 402]
}
```

### 5.4 과제 제출물 수정 (학생용)
**PUT** `/assignments/{assignmentId}/submissions/{submissionId}/edit`

#### 요청 예시
```http
PUT /api/v1/assignments/201/submissions/301/edit
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

#### 요청 Body
```json
{
  "content": "구현한 웹 애플리케이션 설명 (수정본)",
  "attachmentIds": [401, 403, 404]
}
```

### 5.5 과제 수정 (교수용)
**PUT** `/assignments/{assignmentId}/edit`

#### 요청 예시
```http
PUT /api/v1/assignments/201/edit
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

#### 요청 Body
```json
{
  "title": "Spring Boot 실습 과제 (수정)",
  "dueDate": "2024-12-20T23:59:00",
  "maxScore": 100
}
```

### 5.6 과제 삭제 (교수용)
**DELETE** `/assignments/{assignmentId}/delete`

#### 요청 예시
```http
DELETE /api/v1/assignments/201/delete
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 5.7 과제 채점 (교수용)
**POST** `/assignments/{assignmentId}/submissions/{submissionId}/grade`

#### 요청 예시
```http
POST /api/v1/assignments/201/submissions/301/grade
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

#### 요청 Body
```json
{
  "score": 85,
  "feedback": "잘 구현했습니다. 예외 처리 부분을 보완하면 더 좋겠습니다."
}
```

### 5.8 과제 채점 수정 (교수용)
**PUT** `/assignments/{assignmentId}/submissions/{submissionId}/grade/edit`

#### 요청 예시
```http
PUT /api/v1/assignments/201/submissions/301/grade/edit
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

#### 요청 Body
```json
{
  "score": 90,
  "feedback": "수정된 피드백 내용"
}
```

---

## 🎓 6. 시험 시스템 API

### 6.1 시험 등록 (교수용)
**POST** `/boards/EXAM/exams`

#### 요청 예시
```http
POST /api/v1/boards/EXAM/exams
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

#### 요청 Body
```json
{
  "courseId": 101,
  "title": "중간고사 - 웹 프로그래밍",
  "examType": "MIDTERM",
  "examDate": "2024-12-20T09:00:00",
  "durationMinutes": 120,
  "totalScore": 100,
  "isOnline": true,
  "location": "온라인 (LMS)",
  "questionCount": 25,
  "passingScore": 60
}
```

#### 시험 유형 (ExamType)
- `MIDTERM` - 중간고사
- `FINAL` - 기말고사
- `QUIZ` - 퀴즈
- `REGULAR` - 정기시험

### 6.2 시험 응시 시작
**POST** `/exams/{examId}/start`

#### 요청 예시
```http
POST /api/v1/exams/501/start
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "data": {
    "examResultId": 601,
    "startedAt": "2024-12-20T09:00:00",
    "endTime": "2024-12-20T11:00:00",
    "remainingTime": 7200,
    "autoSubmitEnabled": true
  }
}
```

### 6.3 시험 답안 저장
**PUT** `/exams/results/{examResultId}/answers`

#### 요청 예시
```http
PUT /api/v1/exams/results/601/answers
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

#### 요청 Body
```json
{
  "answers": {
    "question_1": "Spring Boot는 스프링 기반의 애플리케이션을 쉽게 만들 수 있게 해주는 프레임워크입니다.",
    "question_2": "B"
  },
  "isAutoSave": true
}
```

### 6.4 시험 수정 (교수용)
**PUT** `/exams/{examId}/edit`

#### 요청 예시
```http
PUT /api/v1/exams/501/edit
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

#### 요청 Body
```json
{
  "title": "중간고사 - 웹 프로그래밍 (수정)",
  "examDate": "2024-12-22T09:00:00",
  "durationMinutes": 150,
  "totalScore": 120,
  "passingScore": 70
}
```

### 6.5 시험 삭제 (교수용)
**DELETE** `/exams/{examId}/delete`

#### 요청 예시
```http
DELETE /api/v1/exams/501/delete
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 6.6 시험 결과 조회
**GET** `/exams/results/{examResultId}`

#### 요청 예시
```http
GET /api/v1/exams/results/601
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "data": {
    "score": 85,
    "grade": "B+",
    "rank": 8,
    "totalParticipants": 45,
    "percentile": 82.2,
    "isPassed": true,
    "feedback": "전반적으로 잘 이해하고 있습니다."
  }
}
```

---

## 👥 7. 스터디 모집 시스템 API

### 7.1 스터디 모집글 등록
**POST** `/boards/STUDY_RECRUITMENT/studies`

#### 요청 예시
```http
POST /api/v1/boards/STUDY_RECRUITMENT/studies
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

#### 요청 Body
```json
{
  "title": "Spring Boot 프로젝트 스터디원 모집",
  "content": "포트폴리오용 웹 애플리케이션 개발 스터디",
  "studyType": "PROJECT",
  "maxMembers": 5,
  "recruitmentEndDate": "2024-12-20T23:59:00",
  "studyStartDate": "2024-12-23",
  "meetingType": "HYBRID",
  "preferredTime": "주말 오후",
  "location": "강남역 스터디카페",
  "requiredSkills": ["Spring Boot", "Java", "Git"],
  "tags": ["웹개발", "포트폴리오", "프로젝트"],
  "contactMethod": "CHAT"
}
```

#### 스터디 유형 (StudyType)
- `EXAM_PREP` - 시험 준비
- `PROJECT` - 프로젝트 
- `READING` - 독서/논문
- `CERTIFICATION` - 자격증

#### 만남 방식 (MeetingType)
- `ONLINE` - 온라인만
- `OFFLINE` - 오프라인만
- `HYBRID` - 온라인 + 오프라인

### 7.2 스터디 목록 조회
**GET** `/boards/STUDY_RECRUITMENT/studies`

#### Parameters
- `studyType`: 스터디 유형 필터
- `meetingType`: 만남 방식 필터
- `status`: 모집 상태 (OPEN/CLOSED/COMPLETED)
- `tags`: 해시태그 필터
- `sort`: 정렬 (latest/popular/deadline)

#### 요청 예시
```http
GET /api/v1/boards/STUDY_RECRUITMENT/studies?studyType=PROJECT&status=OPEN&sort=latest
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "data": {
    "content": [
      {
        "id": 701,
        "title": "Spring Boot 프로젝트 스터디원 모집",
        "studyType": "PROJECT",
        "maxMembers": 5,
        "currentMembers": 3,
        "recruitmentStatus": "OPEN",
        "dDay": "D-12",
        "tags": ["웹개발", "포트폴리오"],
        "createdBy": {
          "name": "김개발"
        }
      }
    ]
  }
}
```

### 7.3 스터디 신청
**POST** `/studies/{studyId}/apply`

#### 요청 예시
```http
POST /api/v1/studies/701/apply
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

#### 요청 Body
```json
{
  "applicationMessage": "Spring Boot에 관심이 많은 3학년 학생입니다. 적극적으로 참여하겠습니다!"
}
```

### 7.4 스터디 신청 처리 (리더용)
**PUT** `/studies/applications/{applicationId}/process`

#### 요청 예시
```http
PUT /api/v1/studies/applications/801/process
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

#### 요청 Body
```json
{
  "action": "APPROVE",
  "message": "환영합니다! 스터디 단체채팅방 링크를 보내드릴게요."
}
```

### 7.5 내 스터디 목록 조회
**GET** `/studies/my-studies`

#### Parameters
- `role`: 역할 필터 (LEADER/MEMBER)
- `status`: 스터디 상태

#### 요청 예시
```http
GET /api/v1/studies/my-studies?role=MEMBER&status=ACTIVE
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 7.6 스터디 멤버 관리 (리더용)
**PUT** `/studies/{studyId}/members/{userId}/role`

#### 요청 예시
```http
PUT /api/v1/studies/701/members/790/role
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

#### 요청 Body
```json
{
  "action": "REMOVE",
  "reason": "참여도 부족"
}
```

### 7.7 스터디 멤버 목록 조회
**GET** `/studies/{studyId}/members`

#### 요청 예시
```http
GET /api/v1/studies/701/members
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "data": [
    {
      "id": 789,
      "name": "김개발",
      "role": "LEADER",
      "joinedAt": "2024-12-08T16:00:00"
    },
    {
      "id": 790,
      "name": "박코딩",
      "role": "MEMBER",
      "joinedAt": "2024-12-09T15:30:00"
    }
  ]
}
```

---

## 🏷️ 8. 해시태그 시스템 API

### 8.1 게시글에 해시태그 추가
**POST** `/posts/{postId}/tags`

#### 요청 예시
```http
POST /api/v1/posts/1001/tags
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

#### 요청 Body
```json
{
  "tags": ["Spring Boot", "웹개발", "포트폴리오"]
}
```

#### 태그 규칙
- 최대 5개 태그
- 각 태그 1-20자
- 한글, 영문, 숫자만 허용

### 8.2 해시태그별 게시글 검색
**GET** `/posts/by-tag`

#### Parameters
- `tag`: 검색할 태그명
- `boardType`: 게시판 타입 필터
- `sort`: 정렬 기준

#### 요청 예시
```http
GET /api/v1/posts/by-tag?tag=Spring Boot&boardType=QUESTION&sort=latest
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "data": {
    "tag": {
      "name": "Spring Boot",
      "usageCount": 25,
      "relatedTags": ["REST API", "JPA", "웹개발"]
    },
    "posts": {
      "content": [
        {
          "id": 1001,
          "title": "Spring Boot 시작하기",
          "boardType": "QUESTION",
          "author": {
            "name": "김개발"
          },
          "tags": ["Spring Boot", "초보자", "입문"]
        }
      ]
    }
  }
}
```

### 8.3 인기 해시태그 조회
**GET** `/tags/popular`

#### Parameters
- `period`: 집계 기간 (WEEK/MONTH/ALL)
- `limit`: 조회 개수

#### 요청 예시
```http
GET /api/v1/tags/popular?period=MONTH&limit=10
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "data": {
    "tags": [
      {
        "name": "웹개발",
        "usageCount": 157,
        "growthRate": 15.2,
        "rank": 1
      },
      {
        "name": "Spring Boot",
        "usageCount": 134,
        "growthRate": 8.7,
        "rank": 2
      }
    ]
  }
}
```

### 8.4 해시태그 자동완성
**GET** `/tags/autocomplete`

#### Parameters
- `query`: 검색어 (최소 1자)
- `limit`: 결과 개수

#### 요청 예시
```http
GET /api/v1/tags/autocomplete?query=스프링&limit=5
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 응답 예시
```json
{
  "status": "SUCCESS",
  "data": [
    {
      "name": "Spring Boot",
      "usageCount": 134,
      "matchType": "PARTIAL"
    },
    {
      "name": "Spring Security",
      "usageCount": 67,
      "matchType": "PARTIAL"  
    }
  ]
}
```

---

## ❌ 에러 코드 정리

### 좋아요/북마크 에러
- `POST_NOT_FOUND` (404): 게시글을 찾을 수 없음
- `CANNOT_LIKE_OWN_POST` (400): 본인 게시글 좋아요 불가
- `BOOKMARK_LIMIT_EXCEEDED` (400): 북마크 개수 제한 초과

### 과제/시험 에러  
- `ASSIGNMENT_NOT_FOUND` (404): 과제를 찾을 수 없음
- `EXAM_NOT_FOUND` (404): 시험을 찾을 수 없음
- `ASSIGNMENT_CLOSED` (400): 과제 제출 마감
- `EXAM_ALREADY_TAKEN` (409): 이미 응시한 시험
- `EXAM_TIME_EXPIRED` (400): 시험 시간 만료

### 스터디/태그 에러
- `STUDY_NOT_FOUND` (404): 스터디를 찾을 수 없음
- `STUDY_FULL` (409): 스터디 정원 초과
- `ALREADY_APPLIED` (409): 이미 신청한 스터디
- `TAG_NOT_FOUND` (404): 해시태그를 찾을 수 없음
- `TOO_MANY_TAGS` (400): 태그 개수 제한 초과
- `INVALID_TAG_FORMAT` (400): 잘못된 태그 형식

---

## 🎯 비즈니스 로직 요약

### 좋아요/북마크 시스템
- **토글 방식**: 중복 클릭시 취소
- **카테고리 분류**: 북마크를 용도별로 정리
- **인기 게시글**: 좋아요 기반 랭킹

### 과제/시험 관리
- **과제 워크플로우**: 등록 → 제출 → 채점 → 피드백
- **시험 시스템**: 실시간 답안 저장, 자동 제출
- **성적 관리**: 자동 등급 산정, 통계 분석

### 스터디 모집
- **모집 프로세스**: 모집글 등록 → 신청 → 승인/거절
- **다양한 스터디**: 시험준비, 프로젝트, 독서 등
- **팀 관리**: 리더/멤버 역할 구분

### 해시태그 관리
- **태그 생성**: 게시글 작성시 자동 생성
- **검색 최적화**: 태그 기반 게시글 검색
- **트렌드 분석**: 인기 태그 순위 제공

---

**완성된 LMS 게시판 시스템**: 51개 API로 구성된 완전한 교육용 소셜 플랫폼 🎉