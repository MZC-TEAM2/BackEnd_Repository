# Board Domain 완전 가이드 (350줄)

LMS 게시판 시스템 핵심 설계 및 구현 가이드

## 📋 목차
1. [통합 테이블 설계](#1-통합-테이블-설계)
2. [13가지 게시판 시스템](#2-13가지-게시판-시스템)  
3. [역할 기반 접근 제어](#3-역할-기반-접근-제어)
4. [과제/시험/스터디 시스템](#4-과제시험스터디-시스템)
5. [핵심 구현 가이드](#5-핵심-구현-가이드)

---

## 1. 통합 테이블 설계

### 🎯 핵심 철학
**모든 게시판을 하나의 `posts` 테이블로 통합 관리**
- 게시판별 테이블 분리 ❌ → 통합 테이블 ✅
- `post_type`으로 게시글 유형 구분
- 확장성과 유지보수성 극대화

### 📊 핵심 테이블 5개
```sql
-- 1️⃣ 통합 게시글 (posts)
posts: id, category_id, author_id, title, content, post_type, is_anonymous

-- 2️⃣ 게시판 설정 (board_categories) 
board_categories: board_type, allow_comments, allow_attachments, allow_anonymous

-- 3️⃣ 관리자 설정 (post_admin_settings)
post_admin_settings: post_id, is_notice, is_pinned, priority

-- 4️⃣ 댓글 시스템 (comments)
comments: id, post_id, parent_comment_id, content, depth

-- 5️⃣ 첨부파일 (attachments)
attachments: id, post_id, original_name, file_path, attachment_type
```

---

## 2. 13가지 게시판 시스템

### 📌 기본 게시판 (5개)
1. **학교 공지사항** - 학교 전체 공지 (댓글 없음)
2. **자유 게시판** - 자유로운 소통 공간
3. **질문 게시판** - 강의별 질문/답변
4. **토론 게시판** - 주제별 토론
5. **학과 게시판** - 학과별 공지 및 소통

### 🎯 역할별 제한 게시판 (2개)
6. **교수 게시판** - 교수/TA만 이용 가능
7. **학생 게시판** - 학생만 이용 가능

### 💼 특수 목적 게시판 (2개)
8. **공모전 게시판** - 공모전 정보 및 팀 모집
9. **취업 게시판** - 채용 정보 및 면접 후기

### 🎓 학습관리 시스템 (4개)
10. **과제 게시판** - 과제 등록/제출/채점 관리
11. **시험 게시판** - 시험 등록/응시/결과 관리
12. **퀴즈 게시판** - 퀴즈 관리 시스템
13. **스터디모집 게시판** - 스터디 모집 및 지원자 관리

### 게시판별 특징

| 게시판 | 주요 용도 | 익명 가능 | 첨부파일 | 해시태그 |
|--------|-----------|-----------|----------|----------|
| 학교공지 | 중요 공지사항 | ❌ | ✅ | ❌ |
| 자유게시판 | 자유 소통 | ✅ | ✅ | ✅ |
| 질문게시판 | 학습 질문 | ✅ | ✅ | ✅ |
| 토론게시판 | 주제 토론 | ❌ | ✅ | ✅ |
| 학과게시판 | 학과 공지/소통 | ❌ | ✅ | ❌ |
| 교수게시판 | 교수진 소통 | ❌ | ✅ | ❌ |
| 학생게시판 | 학생 전용 소통 | ✅ | ✅ | ✅ |
| 공모전게시판 | 공모전 정보 | ❌ | ✅ | ✅ |
| 취업게시판 | 취업 정보 | ✅ | ✅ | ✅ |
| **과제게시판** | **과제 관리** | ❌ | ✅ | ❌ |
| **시험게시판** | **시험 관리** | ❌ | ✅ | ❌ |
| **퀴즈게시판** | **퀴즈 관리** | ❌ | ❌ | ❌ |
| **스터디모집** | **스터디 모집** | ❌ | ❌ | ✅ |

---

## 3. 역할 기반 접근 제어

### 👥 사용자 역할
- **STUDENT**: 학생 - **PROFESSOR**: 교수 - **TEACHING_ASSISTANT**: 조교 - **ADMIN**: 관리자

### 🔐 핵심 RBAC 규칙
| 게시판 | 학생 | 교수 | 관리자 |
|--------|------|------|--------|
| 교수게시판 | ❌ | ✅ | ✅ |
| 학생게시판 | ✅ | ❌ | ✅ |
| 과제게시판 | 제출만 | 등록/채점 | 전체 |
| 시험게시판 | 응시만 | 등록/결과 | 전체 |
| 기타게시판 | 읽기/쓰기 | 읽기/쓰기 | 전체 |

### 💻 권한 체크 코드
```java
// Spring Security 활용
@PreAuthorize("hasRole('PROFESSOR') or hasRole('ADMIN')")
public List<Post> getProfessorPosts() { 
    return postRepository.findByBoardType(BoardType.PROFESSOR);
}

// 서비스 레벨 권한 체크
public boolean canAccessBoard(BoardType boardType, UserRole userRole) {
    if (userRole == UserRole.ADMIN) return true; // 관리자는 모든 접근
    
    return switch (boardType) {
        case PROFESSOR -> userRole == UserRole.PROFESSOR || userRole == UserRole.TEACHING_ASSISTANT;
        case STUDENT -> userRole == UserRole.STUDENT;
        default -> true; // 나머지는 모든 역할 접근 가능
    };
}
```



---

## 4. 과제/시험/스터디 시스템

### 📝 과제 시스템
```sql
-- 과제 등록 (교수)
assignments: 과제 정보, 마감일, 점수, 제출방법

-- 과제 제출 (학생)  
assignment_submissions: 제출내용, 파일, 점수, 피드백
```

**교수 기능**: 과제 등록 → 채점 → 피드백 작성
**학생 기능**: 과제 제출 → 점수 확인

### 🎓 시험 시스템
```sql  
-- 시험 등록
exams: 시험일시, 시간제한, 총점, 온라인여부

-- 시험 응시 결과
exam_results: 시작시간, 제출시간, 점수, 답안데이터(JSON)
```

### 👥 스터디모집 시스템
```sql
-- 스터디 모집글
study_recruitments: 스터디유형, 최대인원, 모집상태, 마감일

-- 지원자 관리
study_applications: 지원자, 지원메시지, 상태(대기/승인/거절)
```

**모집자**: 스터디 모집 → 지원자 승인/거절  
**지원자**: 스터디 지원 → 상태 확인

### 🔧 핵심 Enum 정의

```java
// 과제 제출 상태
public enum SubmissionStatus {
    NOT_SUBMITTED, SUBMITTED, LATE, GRADED
}

// 스터디 모집 상태
public enum RecruitmentStatus {
    RECRUITING, COMPLETED, CANCELLED, IN_PROGRESS, FINISHED
}

// 스터디 지원 상태
public enum ApplicationStatus {
    PENDING, APPROVED, REJECTED, WITHDRAWN
}
```

---

## 5. 핵심 구현 가이드

### 🔧 주요 Enum 정의
```java
// 게시판 유형
public enum BoardType {
    NOTICE, FREE, QUESTION, DISCUSSION, PROFESSOR, STUDENT,
    DEPARTMENT, CONTEST, CAREER, ASSIGNMENT, EXAM, QUIZ, STUDY_RECRUITMENT
}

// 사용자 역할
public enum UserRole {
    STUDENT, PROFESSOR, TEACHING_ASSISTANT, ADMIN
}

// 과제 제출 상태
public enum SubmissionStatus {
    NOT_SUBMITTED, SUBMITTED, LATE, GRADED
}

// 스터디 모집/지원 상태
public enum RecruitmentStatus {
    RECRUITING, COMPLETED, CANCELLED, IN_PROGRESS, FINISHED
}
public enum ApplicationStatus {
    PENDING, APPROVED, REJECTED, WITHDRAWN
}
```

### 📋 핵심 비즈니스 로직
```java
// 1. 게시글 작성
public Long createPost(CreatePostRequest request, User author) {
    validateBoardAccess(request.getBoardType(), author.getRole());
    
    Post post = Post.builder()
        .categoryId(request.getCategoryId())
        .authorId(author.getId())
        .title(request.getTitle())
        .content(request.getContent())
        .postType(request.getPostType())
        .build();
        
    return postRepository.save(post).getId();
}

// 2. 과제 제출
public void submitAssignment(Long assignmentId, SubmissionRequest request, User student) {
    Assignment assignment = assignmentRepository.findById(assignmentId)
        .orElseThrow(() -> new EntityNotFoundException("과제를 찾을 수 없습니다"));
    
    boolean isLate = LocalDateTime.now().isAfter(assignment.getDueDate());
    
    AssignmentSubmission submission = AssignmentSubmission.builder()
        .assignmentId(assignmentId)
        .userId(student.getId())
        .content(request.getContent())
        .status(isLate ? SubmissionStatus.LATE : SubmissionStatus.SUBMITTED)
        .build();
        
    assignmentSubmissionRepository.save(submission);
}

// 3. 스터디 지원
public void applyForStudy(Long studyId, ApplicationRequest request, User applicant) {
    StudyRecruitment study = studyRecruitmentRepository.findById(studyId)
        .orElseThrow(() -> new EntityNotFoundException("스터디를 찾을 수 없습니다"));
    
    if (study.getRecruitmentStatus() != RecruitmentStatus.RECRUITING) {
        throw new InvalidStateException("모집이 종료된 스터디입니다");
    }
    
    StudyApplication application = StudyApplication.builder()
        .studyRecruitmentId(studyId)
        .applicantId(applicant.getId())
        .applicationMessage(request.getMessage())
        .status(ApplicationStatus.PENDING)
        .build();
        
    studyApplicationRepository.save(application);
}
```

### 🚀 개발 핵심 포인트

**1️⃣ 통합 테이블 활용**
- 모든 게시글을 `posts` 테이블에 저장
- `post_type`으로 게시글 유형 구분

**2️⃣ RBAC 권한 체크**  
- 컨트롤러에서 `@PreAuthorize` 활용
- 서비스 레벨에서 2차 권한 검증

**3️⃣ 정규화 원칙 준수**
- 1NF, 2NF, 3NF 모두 만족
- 인덱스 최적화로 성능 향상

**4️⃣ 하이브리드 삭제 정책**

**Soft Delete (논리적 삭제)**: 복구 가능한 핵심 콘텐츠
- **게시글, 댓글, 카테고리, 해시태그**
- `is_deleted = true` + `deleted_at = NOW()`
- 관리자가 복구 가능

**Hard Delete (물리적 삭제)**: 복구 불필요한 부속 데이터
- **첨부파일, 좋아요, 북마크**
- DB + 파일 시스템에서 완전 삭제
- 저장 공간 절약 목적

| 엔티티 | 삭제 방식 | 복구 | 비고 |
|--------|-----------|------|------|
| 게시글/댓글 | Soft Delete | ✅ | 이력 추적 필요 |
| 첨부파일 | Hard Delete | ❌ | 파일+DB 모두 삭제 |
| 좋아요/북마크 | Hard Delete | ❌ | 토글 기능 |

**구현**:
```java
// Soft Delete
post.delete();  // is_deleted = true, deleted_at = NOW()

// Hard Delete
attachmentRepository.delete(attachment);  // DB 완전 삭제
Files.deleteIfExists(filePath);           // 파일 시스템 삭제
```

**관리자 권한**: Soft Delete 데이터 복구 가능, 30일 경과 후 자동 Hard Delete

**이 350줄 완전 가이드로 LMS 게시판 시스템 완벽 구축!** 🎯