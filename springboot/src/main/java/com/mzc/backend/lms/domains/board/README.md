# Board Domain - 전체 테이블 설계

## 요구사항 분석
1. **학교 공지사항**: CRUD (댓글 없음)
2. **자유 게시판**: CRUD + 이미지 (본문, 하단)
3. **질문 게시판**: 학습 관련 질문/답변
4. **토론 게시판**: 주제별 토론 및 의견 교환
5. **교수 게시판**: 교수만 이용 가능한 전용 커뮤니티
6. **학생 게시판**: 학생만 이용 가능한 전용 커뮤니티
7. **학과 게시판**: 학과별 공지 및 관리
8. **공모전 게시판**: 공모전 정보 공유
9. **취업 게시판**: 취업 정보 및 후기 공유
10. **과제 게시판**: 과제 등록, 제출, 채점 관리
11. **시험/퀴즈 게시판**: 시험 등록, 응시, 결과 관리  
12. **스터디모집 게시판**: 스터디 모집 및 지원자 관리 (모집중/완료)
13. **해시태그** 시스템

## 통합 테이블 설계

```dbml
// ===== 사용자 관련 테이블 (참조) =====

Table users {
  id bigint [pk, increment, note: "사용자 고유 ID"]
  login_id varchar(50) [unique, not null, note: "학번/교번"]
  password varchar(255) [not null, note: "암호화된 비밀번호"]
  email varchar(100) [unique, not null, note: "이메일"]
  name varchar(50) [not null, note: "이름"]
  role varchar(30) [not null, note: "역할 (STUDENT/PROFESSOR/TA/ADMIN)"]
  status varchar(20) [not null, default: 'ACTIVE', note: "계정 상태"]
  created_at timestamp [not null, default: `now()`, note: "생성일시"]
  updated_at timestamp [note: "수정일시"]
  deleted_at timestamp [note: "삭제일시 (Soft Delete)"]

  indexes {
    login_id
    email
    role
  }
}

// ===== 게시판 공통 테이블 =====

// 게시판 카테고리 (게시판 유형 관리)
Table board_categories {
  id bigint [pk, increment, note: "카테고리 고유 ID"]
  name varchar(50) [unique, not null, note: "카테고리 이름"]
  description varchar(255) [note: "카테고리 설명"]
  board_type varchar(30) [not null, note: "게시판 유형 (NOTICE/FREE/QUESTION/DISCUSSION/PROFESSOR/STUDENT/DEPARTMENT/CONTEST/CAREER/ASSIGNMENT/EXAM/QUIZ/STUDY_RECRUITMENT)"]
  allow_comments boolean [default: true, note: "댓글 허용 여부"]
  allow_attachments boolean [default: true, note: "첨부파일 허용 여부"]
  allow_anonymous boolean [default: false, note: "익명 작성 허용 여부"]
  display_order int [default: 0, note: "표시 순서"]
  is_active boolean [default: true, note: "활성화 여부"]
  created_at timestamp [not null, default: `now()`, note: "생성일시"]
  updated_at timestamp [note: "수정일시"]
  deleted_at timestamp [note: "삭제일시 (Soft Delete)"]

  indexes {
    board_type
    display_order
    is_active
  }
}

// 통합 게시글 테이블
Table posts {
  id bigint [pk, increment, note: "게시글 고유 ID"]
  category_id bigint [not null, ref: > board_categories.id, note: "카테고리 ID"]
  course_id bigint [note: "강의 ID (질문 게시판용, 선택사항)"]
  department_id bigint [note: "학과 ID (학과 게시판용, 선택사항)"]
  author_id bigint [not null, ref: > users.id, note: "작성자 ID"]
  title varchar(255) [not null, note: "제목"]
  content text [not null, note: "내용"]
  post_type varchar(30) [not null, note: "게시글 유형 (NOTICE/GENERAL/QUESTION/DISCUSSION/PROFESSOR/STUDENT/DEPARTMENT/CONTEST/CAREER/ASSIGNMENT/EXAM/QUIZ/STUDY_RECRUITMENT)"]
  is_anonymous boolean [default: false, note: "익명 게시글 여부"]
  is_deleted boolean [default: false, note: "삭제 여부 (성능 최적화용, 초고빈도 조회)"]
  created_at timestamp [not null, default: `now()`, note: "생성일시"]
  updated_at timestamp [note: "수정일시"]
  deleted_at timestamp [note: "삭제일시 (Soft Delete)"]

  indexes {
    (category_id, is_deleted, created_at) [name: 'idx_category_active_created']
    (course_id, is_deleted, created_at) [name: 'idx_course_active_created']
    (department_id, is_deleted, created_at) [name: 'idx_department_active_created']
    (author_id, created_at) [name: 'idx_author_created']
    post_type
    is_deleted
  }
}

// 관리자 전용 게시글 설정 테이블
Table post_admin_settings {
  id bigint [pk, increment, note: "설정 고유 ID"]
  post_id bigint [not null, unique, ref: > posts.id, note: "게시글 ID"]
  status varchar(20) [not null, default: 'ACTIVE', note: "상태 (ACTIVE/CLOSED/ARCHIVED)"]
  priority varchar(20) [not null, default: 'NORMAL', note: "우선순위 (HIGH/NORMAL/LOW)"]
  is_notice boolean [not null, default: false, note: "공지사항 여부"]
  is_pinned boolean [not null, default: false, note: "상단 고정 여부"]
  managed_by bigint [ref: > users.id, note: "최종 관리자 ID"]
  managed_at timestamp [note: "최종 관리 일시"]

  indexes {
    post_id [unique]
    status
    priority
    is_notice
    is_pinned
    managed_by
  }
}

// 댓글 테이블
Table comments {
  id bigint [pk, increment, note: "댓글 고유 ID"]
  post_id bigint [not null, ref: > posts.id, note: "게시글 ID"]
  author_id bigint [not null, ref: > users.id, note: "작성자 ID"]
  parent_comment_id bigint [ref: > comments.id, note: "부모 댓글 ID (대댓글용)"]
  content text [not null, note: "댓글 내용"]
  depth int [default: 0, note: "댓글 깊이 (0: 댓글, 1: 대댓글)"]
  is_anonymous boolean [default: false, note: "익명 댓글 여부"]
  is_deleted boolean [default: false, note: "삭제 여부 (성능 최적화용)"]
  created_at timestamp [not null, default: `now()`, note: "생성일시"]
  updated_at timestamp [note: "수정일시"]
  deleted_at timestamp [note: "삭제일시 (Soft Delete)"]

  indexes {
    (post_id, is_deleted, created_at) [name: 'idx_post_active_created']
    (parent_comment_id, is_deleted, created_at) [name: 'idx_parent_active_created']
    (author_id, created_at) [name: 'idx_author_created']
    depth
    is_deleted
  }
}



// ===== 첨부파일 테이블 =====

Table attachments {
  id bigint [pk, increment, note: "첨부파일 고유 ID"]
  post_id bigint [ref: > posts.id, note: "게시글 ID"]
  comment_id bigint [ref: > comments.id, note: "댓글 ID"]
  attachment_type varchar(30) [not null, note: "첨부 유형 (POST_CONTENT/POST_BOTTOM/COMMENT)"]
  original_name varchar(255) [not null, note: "원본 파일명"]
  stored_name varchar(255) [not null, note: "서버 저장 파일명 (UUID)"]
  file_path varchar(500) [not null, note: "파일 저장 경로"]
  file_size bigint [not null, note: "파일 크기 (bytes)"]
  mime_type varchar(100) [not null, note: "MIME 타입"]
  uploader_id bigint [not null, ref: > users.id, note: "업로더 ID"]
  created_at timestamp [not null, default: `now()`, note: "업로드일시"]
  updated_at timestamp [note: "수정일시"]
  deleted_at timestamp [note: "삭제일시 (Soft Delete)"]

  indexes {
    (post_id, deleted_at) [name: 'idx_post_active_attachments']
    (comment_id, deleted_at) [name: 'idx_comment_active_attachments']
    uploader_id
    attachment_type
    mime_type
  }
}

// ===== 조회수/다운로드 추적 테이블 =====

Table post_views {
  id bigint [pk, increment, note: "조회 기록 고유 ID"]
  post_id bigint [not null, ref: > posts.id, note: "게시글 ID"]
  user_id bigint [ref: > users.id, note: "사용자 ID (비로그인 시 null)"]
  ip_address varchar(45) [note: "IP 주소 (IPv6 지원)"]
  user_agent text [note: "브라우저 정보"]
  viewed_at timestamp [not null, default: `now()`, note: "조회일시"]

  indexes {
    post_id
    user_id  
    viewed_at
  }
}

Table attachment_downloads {
  id bigint [pk, increment, note: "다운로드 기록 고유 ID"]
  attachment_id bigint [not null, ref: > attachments.id, note: "첨부파일 ID"]
  user_id bigint [ref: > users.id, note: "다운로더 ID (비로그인 시 null)"]
  ip_address varchar(45) [note: "IP 주소"]
  downloaded_at timestamp [not null, default: `now()`, note: "다운로드일시"]

  indexes {
    attachment_id
    user_id
    downloaded_at
  }
}

// ===== 태그 시스템 =====

Table hashtags {
  id bigint [pk, increment, note: "해시태그 고유 ID"]
  name varchar(50) [unique, not null, note: "태그 이름 (#제외, 소문자)"]
  display_name varchar(50) [not null, note: "화면 표시용 태그명"]
  description varchar(255) [note: "태그 설명"]
  color varchar(7) [default: '#007bff', note: "태그 색상 (HEX)"]
  tag_category varchar(30) [note: "태그 카테고리 (SUBJECT/DIFFICULTY/TYPE 등)"]
  is_active boolean [default: true, note: "활성화 상태"]
  created_by bigint [ref: > users.id, note: "태그 생성자 ID"]
  created_at timestamp [not null, default: `now()`, note: "생성일시"]
  updated_at timestamp [note: "수정일시"]
  deleted_at timestamp [note: "삭제일시 (Soft Delete)"]

  indexes {
    name
    tag_category
    is_active
  }
}

Table post_hashtags {
  id bigint [pk, increment, note: "게시글-해시태그 연결 ID"]
  post_id bigint [not null, ref: > posts.id, note: "게시글 ID"]
  hashtag_id bigint [not null, ref: > hashtags.id, note: "해시태그 ID"]
  created_by bigint [not null, ref: > users.id, note: "태그 추가자 ID"]
  created_at timestamp [not null, default: `now()`, note: "연결 생성일시"]

  indexes {
    (post_id, hashtag_id) [unique, name: 'idx_post_hashtag']
    hashtag_id
    created_by
  }
}

// ===== 좋아요/북마크 시스템 =====

Table post_likes {
  id bigint [pk, increment, note: "좋아요 고유 ID"]
  user_id bigint [not null, ref: > users.id, note: "사용자 ID"]
  post_id bigint [ref: > posts.id, note: "게시글 ID"]
  comment_id bigint [ref: > comments.id, note: "댓글 ID"]
  like_type varchar(20) [not null, note: "좋아요 유형 (POST/COMMENT)"]
  created_at timestamp [not null, default: `now()`, note: "좋아요 생성일시"]
  deleted_at timestamp [note: "좋아요 취소일시 (Soft Delete)"]

  indexes {
    (user_id, post_id) [unique, name: 'idx_user_post_like']
    (user_id, comment_id) [unique, name: 'idx_user_comment_like']
    like_type
  }
}

Table post_bookmarks {
  id bigint [pk, increment, note: "북마크 고유 ID"]
  user_id bigint [not null, ref: > users.id, note: "사용자 ID"]
  post_id bigint [not null, ref: > posts.id, note: "게시글 ID"]
  bookmark_category varchar(50) [note: "북마크 카테고리"]
  notes text [note: "개인 메모"]
  created_at timestamp [not null, default: `now()`, note: "북마크 생성일시"]
  updated_at timestamp [note: "수정일시"]
  deleted_at timestamp [note: "북마크 삭제일시 (Soft Delete)"]

  indexes {
    (user_id, post_id) [unique, name: 'idx_user_post_bookmark']
    (user_id, bookmark_category) [name: 'idx_user_category']
  }
}

// ===== 과제 시스템 =====

Table assignments {
  id bigint [pk, increment, note: "과제 고유 ID"]
  post_id bigint [not null, ref: > posts.id, note: "게시글 ID (제목/내용은 posts 테이블 참조)"]
  course_id bigint [not null, note: "강의 ID"]
  due_date timestamp [not null, note: "제출 마감일"]
  max_score decimal(5,2) [not null, note: "만점"]
  submission_method varchar(20) [not null, note: "제출방법 (FILE_UPLOAD/TEXT_INPUT/BOTH)"]
  late_submission_allowed boolean [not null, default: false, note: "지각 제출 허용"]
  late_penalty_percent decimal(3,2) [null, note: "지각 제출 감점 비율 (%)"]
  max_file_size_mb int [default: 10, note: "최대 파일 크기 (MB)"]
  allowed_file_types varchar(255) [note: "허용 파일 확장자 (쉼표 구분)"]
  instructions text [null, note: "제출 지침"]
  created_by bigint [not null, ref: > users.id, note: "생성자 ID"]
  created_at timestamp [not null, default: `now()`, note: "생성일시"]
  updated_at timestamp [note: "수정일시"]
  deleted_at timestamp [note: "삭제일시 (Soft Delete)"]

  indexes {
    post_id [unique]
    course_id
    due_date
    created_by
  }
}

Table assignment_submissions {
  id bigint [pk, increment, note: "과제 제출 고유 ID"]
  assignment_id bigint [not null, ref: > assignments.id, note: "과제 ID"]
  user_id bigint [not null, ref: > users.id, note: "제출자 ID"]
  content text [null, note: "텍스트 제출 내용"]
  submitted_at timestamp [not null, default: `now()`, note: "제출일시"]
  status varchar(20) [not null, default: 'SUBMITTED', note: "제출 상태"]
  score decimal(5,2) [null, note: "획득 점수"]
  feedback text [null, note: "피드백"]
  graded_at timestamp [null, note: "채점일시"]
  graded_by bigint [null, ref: > users.id, note: "채점자 ID"]
  created_at timestamp [not null, default: `now()`, note: "생성일시"]
  updated_at timestamp [note: "수정일시"]
  deleted_at timestamp [note: "삭제일시 (Soft Delete)"]

  indexes {
    (assignment_id, user_id) [unique, name: 'idx_assignment_user']
    user_id
    status
    submitted_at
  }
}

// ===== 시험/퀴즈 시스템 =====

Table exams {
  id bigint [pk, increment, note: "시험 고유 ID"]
  post_id bigint [not null, ref: > posts.id, note: "게시글 ID (제목/내용은 posts 테이블 참조)"]
  course_id bigint [not null, note: "강의 ID"]
  exam_type varchar(20) [not null, note: "시험 유형 (MIDTERM/FINAL/QUIZ/REGULAR)"]
  exam_date timestamp [not null, note: "시험 일시"]
  duration_minutes int [not null, note: "시험 시간 (분)"]
  total_score decimal(5,2) [not null, note: "총점"]
  is_online boolean [not null, default: false, note: "온라인 시험 여부"]
  location varchar(100) [null, note: "시험 장소"]
  instructions text [null, note: "시험 안내사항"]
  question_count int [null, note: "문제 수"]
  passing_score decimal(5,2) [null, note: "합격 점수"]
  created_by bigint [not null, ref: > users.id, note: "생성자 ID"]
  created_at timestamp [not null, default: `now()`, note: "생성일시"]
  updated_at timestamp [note: "수정일시"]
  deleted_at timestamp [note: "삭제일시 (Soft Delete)"]

  indexes {
    post_id [unique]
    course_id
    exam_date
    exam_type
    created_by
  }
}

Table exam_results {
  id bigint [pk, increment, note: "시험 결과 고유 ID"]
  exam_id bigint [not null, ref: > exams.id, note: "시험 ID"]
  user_id bigint [not null, ref: > users.id, note: "응시자 ID"]
  started_at timestamp [null, note: "시험 시작 시간"]
  submitted_at timestamp [null, note: "제출 시간"]
  score decimal(5,2) [null, note: "획득 점수"]
  grade varchar(2) [null, note: "등급 (A+, A, B+ 등)"]
  answer_data text [null, note: "답안 데이터 (JSON)"]
  feedback text [null, note: "피드백"]
  graded_at timestamp [null, note: "채점일시"]
  graded_by bigint [null, ref: > users.id, note: "채점자 ID"]
  created_at timestamp [not null, default: `now()`, note: "생성일시"]
  updated_at timestamp [note: "수정일시"]
  deleted_at timestamp [note: "삭제일시 (Soft Delete)"]

  indexes {
    (exam_id, user_id) [unique, name: 'idx_exam_user']
    user_id
    submitted_at
    score
    grade
  }
}

// ===== 스터디모집 시스템 =====

Table study_recruitments {
  id bigint [pk, increment, note: "스터디모집 고유 ID"]
  post_id bigint [not null, unique, ref: > posts.id, note: "게시글 ID (제목/내용은 posts 테이블 참조)"]
  study_type varchar(20) [not null, note: "스터디 유형 (EXAM_PREP/PROJECT/LANGUAGE/CERTIFICATION/READING/CODING/OTHER)"]
  max_participants int [not null, note: "최대 참여 인원"]
  recruitment_status varchar(20) [not null, default: 'RECRUITING', note: "모집 상태 (RECRUITING/COMPLETED/CANCELLED/IN_PROGRESS/FINISHED)"]
  recruitment_deadline timestamp [null, note: "모집 마감일"]
  requirements text [null, note: "지원 자격/조건"]

  indexes {
    post_id [unique]
    study_type
    recruitment_status
    recruitment_deadline
  }
}

Table study_applications {
  id bigint [pk, increment, note: "스터디 지원 고유 ID"]
  study_recruitment_id bigint [not null, ref: > study_recruitments.id, note: "스터디모집 ID"]
  applicant_id bigint [not null, ref: > users.id, note: "지원자 ID"]
  application_message text [null, note: "지원 메시지"]
  status varchar(20) [not null, default: 'PENDING', note: "지원 상태 (PENDING/APPROVED/REJECTED/WITHDRAWN)"]
  applied_at timestamp [not null, default: `now()`, note: "지원일시"]
  processed_at timestamp [null, note: "처리일시 (승인/거절)"]
  process_message text [null, note: "처리 메시지 (거절 사유 등)"]

  indexes {
    (study_recruitment_id, applicant_id) [unique, name: 'idx_study_applicant']
    applicant_id
    status
    applied_at
  }
}
```

## 요구사항별 구현 방식

### 1. 과제 시스템 구현

#### 교수 페이지 - 과제 등록
- `assignments` 테이블에 과제 정보 저장
- `posts` 테이블에 과제 게시글 저장 (제목/내용)
- 과제 유형, 점수, 제출 형식, 마감일 등 설정
- 지각 제출 허용 여부 및 감점 비율 설정

#### 교수 페이지 - 점수 등록 및 채점
- `assignment_submissions` 테이블에서 학생 제출물 조회
- 점수(`score`), 피드백(`feedback`) 입력
- 채점 완료 시 `graded_at`, `graded_by` 업데이트
- 채점 상태(`status`) 관리: SUBMITTED → GRADED

#### 학생 페이지 - 과제 제출
- `assignment_submissions` 테이블에 제출 정보 저장
- 텍스트 제출(`content`) + 첨부파일(`attachments`) 지원
- 제출 시간(`submitted_at`) 자동 기록
- 마감일 이후 제출 시 지각 처리(`status = 'LATE'`)

#### 학생 페이지 - 점수 확인
- `assignment_submissions` 테이블에서 본인 과제 조회
- 제출 상태, 점수, 피드백 확인
- 채점 완료된 과제만 점수 공개

### 2. 시험/퀴즈 시스템 구현
- `exams` 테이블에 시험 정보 저장
- `exam_results` 테이블에 응시 결과 저장
- 온라인/오프라인 시험 지원
- 답안 데이터 JSON 형태 저장

### 3. 스터디모집 시스템 구현
- `study_recruitments` 테이블에 모집 정보 저장
- `study_applications` 테이블에 지원자 정보 저장
- 모집 상태 관리 (모집중/완료/취소/진행중/종료)

### 4. 학교 공지사항 (댓글 없음)
- `board_categories`에서 `allow_comments = false`
- `post_admin_settings`에서 `is_notice = true`

### 5. 교수 전용 자유 게시판
- `posts` 테이블에 `post_type = 'PROFESSOR'`로 구분
- `board_categories`에서 `board_type = 'PROFESSOR'`
- 교수 역할만 접근 가능 (RBAC 적용)
- 이미지 첨부, 댓글/대댓글, 해시태그 지원

### 6. 학생 전용 자유 게시판  
- `posts` 테이블에 `post_type = 'STUDENT'`로 구분
- `board_categories`에서 `board_type = 'STUDENT'`
- 학생 역할만 접근 가능 (RBAC 적용)
- 이미지 첨부, 댓글/대댓글, 해시태그 지원

### 7. 일반 자유 게시판 (이미지 포함)
- `posts` 테이블에 `post_type = 'GENERAL'`로 구분
- `posts` 테이블 + `attachments` 테이블
- `attachment_type`으로 본문/하단 이미지 구분
- 모든 사용자 접근 가능

### 8. 질문/토론/커뮤니티 게시판 (대댓글)
- `comments` 테이블의 `depth` 필드로 대댓글 구현
- `parent_comment_id`로 계층 구조 관리

### 9. 해시태그 시스템
- `hashtags` + `post_hashtags` 테이블
- 자동 완성, 인기 태그 기능 지원

// ===== Relationships Summary =====
// users 1--* posts (작성자)
// users 1--* comments (댓글 작성자)
// users 1--* attachments (업로더)
// users 1--* post_likes (좋아요 누른 사용자)
// users 1--* post_bookmarks (북마크한 사용자)
// users 1--* hashtags (해시태그 생성자)
// board_categories 1--* posts (카테고리-게시글)
// posts 1--* comments (게시글-댓글)
// posts 1--* attachments (게시글-첨부파일)
// posts 1--* post_hashtags (게시글-해시태그)
// comments 1--* attachments (댓글-첨부파일)
// hashtags 1--* post_hashtags (해시태그-게시글)
// users 1--* assignments (과제 생성자)
// users 1--* assignment_submissions (과제 제출자)
// users 1--* exams (시험 생성자)
// users 1--* exam_results (시험 응시자)
// users 1--* study_recruitments (스터디 모집자)
// users 1--* study_applications (스터디 지원자)
// study_recruitments 1--* study_applications (스터디-지원자)
// posts 1--1 study_recruitments (게시글-스터디모집)

## Enums

```java
// 사용자 역할 (User 도메인에서 참조)
public enum UserRole {
    STUDENT,            // 학생
    PROFESSOR,          // 교수
    TEACHING_ASSISTANT, // 조교 (TA)
    ADMIN              // 관리자
}

// 사용자 상태 (User 도메인에서 참조)
public enum UserStatus {
    ACTIVE,    // 활성
    INACTIVE,  // 비활성
    SUSPENDED, // 정지
    WITHDRAWN, // 탈퇴
    PENDING    // 승인대기
}

// 게시판 유형
public enum BoardType {
    NOTICE,           // 공지사항
    FREE,            // 자유게시판  
    QUESTION,        // 질문게시판
    DISCUSSION,      // 토론게시판
    PROFESSOR,       // 교수게시판
    STUDENT,         // 학생게시판
    DEPARTMENT,      // 학과게시판
    CONTEST,         // 공모전게시판
    CAREER,          // 취업게시판
    ASSIGNMENT,      // 과제게시판
    EXAM,            // 시험게시판
    QUIZ,            // 퀴즈게시판
    STUDY_RECRUITMENT // 스터디모집게시판
}

// 게시글 유형
public enum PostType {
    NOTICE,           // 공지사항
    GENERAL,          // 일반 게시글
    QUESTION,         // 질문
    DISCUSSION,       // 토론
    PROFESSOR,        // 교수 게시글
    STUDENT,          // 학생 게시글
    DEPARTMENT,       // 학과 게시글
    CONTEST,          // 공모전
    CAREER,           // 취업정보
    ASSIGNMENT,       // 과제
    EXAM,             // 시험
    QUIZ,             // 퀴즈
    STUDY_RECRUITMENT // 스터디모집
}

// 게시글 상태
public enum PostStatus {
    ACTIVE,   // 활성
    CLOSED,   // 마감/종료
    ARCHIVED  // 아카이브
}

// 우선순위
public enum Priority {
    HIGH,    // 높음
    NORMAL,  // 보통
    LOW      // 낮음
}

// 첨부파일 유형
public enum AttachmentType {
    POST_CONTENT, // 본문 이미지
    POST_BOTTOM,  // 하단 첨부파일
    COMMENT       // 댓글 첨부파일
}

// 좋아요 유형
public enum LikeType {
    POST,    // 게시글 좋아요
    COMMENT  // 댓글 좋아요
}

// 태그 카테고리
public enum TagCategory {
    SUBJECT,    // 과목별
    DIFFICULTY, // 난이도별
    TYPE,       // 유형별
    GENERAL     // 일반
}

// 과제 제출 상태
public enum SubmissionStatus {
    NOT_SUBMITTED, // 미제출
    SUBMITTED,     // 제출완료
    LATE,          // 지각제출
    GRADED         // 채점완료
}

// 시험 유형
public enum ExamType {
    MIDTERM,  // 중간고사
    FINAL,    // 기말고사
    QUIZ,     // 퀴즈
    REGULAR   // 정기시험
}

// 제출 방법
public enum SubmissionMethod {
    FILE_UPLOAD, // 파일업로드
    TEXT_INPUT,  // 텍스트입력
    BOTH         // 파일+텍스트
}

// 스터디 모집 상태
public enum RecruitmentStatus {
    RECRUITING,  // 모집중
    COMPLETED,   // 모집완료
    CANCELLED,   // 모집취소
    IN_PROGRESS, // 스터디 진행중
    FINISHED     // 스터디 종료
}

// 스터디 지원 상태
public enum ApplicationStatus {
    PENDING,   // 지원 대기
    APPROVED,  // 승인됨
    REJECTED,  // 거절됨
    WITHDRAWN  // 지원 철회
}

// 스터디 유형
public enum StudyType {
    EXAM_PREP,    // 시험 대비
    PROJECT,      // 프로젝트
    LANGUAGE,     // 어학
    CERTIFICATION,// 자격증
    READING,      // 독서
    CODING,       // 코딩
    OTHER         // 기타
}


```

// ===== Notes =====
// - User 도메인 테이블 구조와 일관성 유지
// - 모든 테이블에 created_at, updated_at, deleted_at 표준 적용
// - Soft Delete 정책 전체 적용
// - 인덱스 최적화로 조회 성능 향상
// - 외래키 제약조건으로 데이터 무결성 보장
// - 통합 게시글 테이블로 확장성 및 유지보수성 향상
// - 역할 기반 접근 제어 (RBAC) 적용
// - 관리자(ADMIN)는 모든 게시판에 대한 전체 권한 보유
// - 교수/학생 전용 게시판으로 역할별 커뮤니티 분리
// - 학과게시판은 공식 공지 및 관리 용도로 활용

## 구현될 기능

### 1. 학교 공지사항
- 관리자/교수만 작성 가능
- 댓글 기능 비활성화
- 중요도별 상단 고정

### 2. 자유 게시판  
- 모든 사용자 작성 가능
- 이미지 첨부 (본문 + 하단)
- 댓글/대댓글 기능
- 해시태그 시스템

### 3. 질문 게시판
- 학습 관련 질문/답변
- 강의별 카테고리 (선택적)
- 댓글/대댓글 기능
- 좋아요/북마크 기능

### 4. 토론 게시판
- 주제별 토론 공간
- 댓글/대댓글 기능  
- 해시태그로 주제 분류

### 5. 교수 자유게시판 (교수 전용)
- **접근 권한**: 교수(PROFESSOR) 역할만 접근 가능
- **게시글 유형**: `post_type = 'PROFESSOR'`
- **기능**: 교육/연구 정보 공유, 학사 운영 논의
- **지원 기능**: 이미지 첨부, 댓글/대댓글, 해시태그, 좋아요/북마크
- **익명 게시**: 설정에 따라 지원

### 6. 학생 자유게시판 (학생 전용)
- **접근 권한**: 학생(STUDENT) 역할만 접근 가능  
- **게시글 유형**: `post_type = 'STUDENT'`
- **기능**: 학습/생활 정보 공유, 동아리/모임 활동
- **지원 기능**: 이미지 첨부, 댓글/대댓글, 해시태그, 좋아요/북마크
- **익명 게시**: 설정에 따라 지원

### 7. 학과 게시판
- 학과별 공지사항 및 관리
- 학과 교수진의 공식 공지
- 학과 행사 및 중요 안내사항

### 8. 공모전 게시판
- 공모전 정보 및 팀 모집
- 마감일 정보 관리
- 분야별 해시태그 분류

### 9. 취업 게시판
- 채용 정보 공유
- 취업 후기 및 면접 정보
- 기업별/직무별 해시태그

### 10. 과제 게시판
- 교수만 과제 생성/수정 가능
- 학생은 과제 제출 (파일 업로드 + 텍스트)
- 마감일 관리 및 지각 제출 처리
- 수동 채점 및 피드백 시스템

### 11. 시험/퀴즈 게시판
- 교수만 시험 생성/수정 가능
- 온라인/오프라인 시험 지원
- 시험 시간 제한 및 자동 제출
- 실시간 답안 저장
- 성적 통계 및 분석

### 12. 스터디모집 게시판
- 스터디 그룹 모집 및 참여
- 모집 상태 관리 (모집중/완료/취소/진행중/종료)
- 스터디 유형별 분류 (시험준비/프로젝트/어학/자격증/독서/코딩/기타)
- 최대 참여 인원 제한
- 모집 마감일 설정 (선택)
- 지원 자격/조건 명시 (선택)

#### 모집자(작성자) 기능:
- **지원자 목록 조회**: `study_applications` 테이블에서 본인 모집글의 지원자 확인
- **지원자 승인/거절**: `status`를 PENDING → APPROVED/REJECTED로 변경
- **모집 상태 관리**: 수동으로 RECRUITING → COMPLETED/CANCELLED로 변경
- **지원자별 메시지 확인**: 각 지원자의 `application_message` 내용 열람
- **거절 사유 작성**: `process_message`로 거절 이유 전달

#### 지원자 기능:
- **스터디 지원**: 지원 메시지와 함께 지원 신청
- **지원 상태 확인**: 본인의 지원 상태 및 처리 결과 확인
- **지원 철회**: 승인 전 지원을 철회 가능 (status = 'WITHDRAWN')

### 13. 공통 기능
- 해시태그 시스템
- 좋아요/북마크
- 첨부파일 (이미지 + 문서)
- 익명 게시 (설정에 따라)
- 검색 및 필터링

이 테이블 설계로 모든 요구사항을 만족할 수 있습니다. 추가로 구현하고 싶은 기능이나 수정이 필요한 부분이 있다면 말씀해 주세요!