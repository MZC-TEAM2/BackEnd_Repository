-- =====================================================
-- Board Domain - 게시판 도메인 테이블 생성
-- V4__create_board_tables.sql
-- =====================================================

-- -----------------------------------------------------
-- 1. 게시판 카테고리 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS board_categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL COMMENT '카테고리 이름',
    description VARCHAR(255) COMMENT '카테고리 설명',
    board_type VARCHAR(30) NOT NULL COMMENT '게시판 유형 (NOTICE/FREE/QUESTION/DISCUSSION/PROFESSOR/STUDENT/DEPARTMENT/CONTEST/CAREER/ASSIGNMENT/EXAM/QUIZ/STUDY_RECRUITMENT)',
    allow_comments BIT(1) DEFAULT b'1' COMMENT '댓글 허용 여부',
    allow_attachments BIT(1) DEFAULT b'1' COMMENT '첨부파일 허용 여부',
    allow_anonymous BIT(1) DEFAULT b'0' COMMENT '익명 작성 허용 여부',
    display_order INT DEFAULT 0 COMMENT '표시 순서',
    is_active BIT(1) DEFAULT b'1' COMMENT '활성화 여부',
    is_deleted BIT(1) DEFAULT b'0' COMMENT '삭제 여부',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6) DEFAULT NULL COMMENT '수정일시',
    deleted_at DATETIME(6) DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (id),
    KEY idx_board_type (board_type),
    KEY idx_display_order (display_order),
    KEY idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시판 카테고리';

-- -----------------------------------------------------
-- 2. 게시글 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS posts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL COMMENT '카테고리 ID',
    course_id BIGINT DEFAULT NULL COMMENT '강의 ID (질문 게시판용, 선택사항)',
    department_id BIGINT DEFAULT NULL COMMENT '학과 ID (학과 게시판용, 선택사항)',
    author_id BIGINT NOT NULL COMMENT '작성자 ID',
    title VARCHAR(255) NOT NULL COMMENT '제목',
    content TEXT NOT NULL COMMENT '내용',
    post_type VARCHAR(30) NOT NULL COMMENT '게시글 유형 (NOTICE/GENERAL/QUESTION/DISCUSSION/PROFESSOR/STUDENT/DEPARTMENT/CONTEST/CAREER/ASSIGNMENT/EXAM/QUIZ/STUDY_RECRUITMENT)',
    is_anonymous BIT(1) DEFAULT b'0' COMMENT '익명 게시글 여부',
    view_count INT DEFAULT 0 COMMENT '조회수',
    like_count INT DEFAULT 0 COMMENT '좋아요 수',
    is_deleted BIT(1) DEFAULT b'0' COMMENT '삭제 여부 (성능 최적화용)',
    created_by BIGINT NOT NULL COMMENT '생성자 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '수정자 ID',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6) DEFAULT NULL COMMENT '수정일시',
    deleted_at DATETIME(6) DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (id),
    KEY idx_category_active_created (category_id, is_deleted, created_at),
    KEY idx_course_active_created (course_id, is_deleted, created_at),
    KEY idx_department_active_created (department_id, is_deleted, created_at),
    KEY idx_author_created (author_id, created_at),
    KEY idx_post_type (post_type),
    KEY idx_is_deleted (is_deleted),
    KEY idx_created_by (created_by),
    CONSTRAINT fk_posts_category FOREIGN KEY (category_id) REFERENCES board_categories (id),
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_posts_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_posts_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글';

-- -----------------------------------------------------
-- 3. 관리자 전용 게시글 설정 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS post_admin_settings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL UNIQUE COMMENT '게시글 ID',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '상태 (ACTIVE/CLOSED/ARCHIVED)',
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '우선순위 (HIGH/NORMAL/LOW)',
    is_notice BIT(1) NOT NULL DEFAULT b'0' COMMENT '공지사항 여부',
    is_pinned BIT(1) NOT NULL DEFAULT b'0' COMMENT '상단 고정 여부',
    managed_by BIGINT DEFAULT NULL COMMENT '최종 관리자 ID',
    managed_at DATETIME(6) DEFAULT NULL COMMENT '최종 관리 일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_admin_settings_post_id (post_id),
    KEY idx_status (status),
    KEY idx_priority (priority),
    KEY idx_is_notice (is_notice),
    KEY idx_is_pinned (is_pinned),
    KEY idx_managed_by (managed_by),
    CONSTRAINT fk_post_admin_settings_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_post_admin_settings_manager FOREIGN KEY (managed_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관리자 전용 게시글 설정';

-- -----------------------------------------------------
-- 4. 댓글 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    author_id BIGINT NOT NULL COMMENT '작성자 ID',
    parent_comment_id BIGINT DEFAULT NULL COMMENT '부모 댓글 ID (대댓글용)',
    content TEXT NOT NULL COMMENT '댓글 내용',
    depth INT DEFAULT 0 COMMENT '댓글 깊이 (0: 댓글, 1: 대댓글)',
    is_anonymous BIT(1) DEFAULT b'0' COMMENT '익명 댓글 여부',
    is_deleted BIT(1) DEFAULT b'0' COMMENT '삭제 여부 (성능 최적화용)',
    is_deleted_by_admin BIT(1) DEFAULT b'0' COMMENT '관리자에 의한 삭제 여부',
    created_by BIGINT NOT NULL COMMENT '생성자 ID',
    updated_by BIGINT DEFAULT NULL COMMENT '수정자 ID',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6) DEFAULT NULL COMMENT '수정일시',
    deleted_at DATETIME(6) DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (id),
    KEY idx_post_active_created (post_id, is_deleted, created_at),
    KEY idx_parent_active_created (parent_comment_id, is_deleted, created_at),
    KEY idx_author_created (author_id, created_at),
    KEY idx_depth (depth),
    KEY idx_is_deleted (is_deleted),
    KEY idx_created_by (created_by),
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES comments (id),
    CONSTRAINT fk_comments_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_comments_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='댓글';

-- -----------------------------------------------------
-- 5. 첨부파일 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS attachments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT DEFAULT NULL COMMENT '게시글 ID',
    comment_id BIGINT DEFAULT NULL COMMENT '댓글 ID',
    attachment_type VARCHAR(30) NOT NULL COMMENT '첨부 유형 (POST_CONTENT/POST_BOTTOM/COMMENT)',
    original_name VARCHAR(255) NOT NULL COMMENT '원본 파일명',
    stored_name VARCHAR(255) NOT NULL COMMENT '서버 저장 파일명 (UUID)',
    file_path VARCHAR(500) NOT NULL COMMENT '파일 저장 경로',
    file_size BIGINT NOT NULL COMMENT '파일 크기 (bytes)',
    mime_type VARCHAR(100) DEFAULT NULL COMMENT 'MIME 타입',
    uploader_id BIGINT DEFAULT NULL COMMENT '업로더 ID',
    download_count INT DEFAULT 0 COMMENT '다운로드 횟수',
    is_deleted BIT(1) DEFAULT b'0' COMMENT '삭제 여부',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '업로드일시',
    updated_at DATETIME(6) DEFAULT NULL COMMENT '수정일시',
    deleted_at DATETIME(6) DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (id),
    KEY idx_post_active_attachments (post_id, deleted_at),
    KEY idx_comment_active_attachments (comment_id, deleted_at),
    KEY idx_uploader_id (uploader_id),
    KEY idx_attachment_type (attachment_type),
    KEY idx_mime_type (mime_type),
    CONSTRAINT fk_attachments_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_attachments_comment FOREIGN KEY (comment_id) REFERENCES comments (id),
    CONSTRAINT fk_attachments_uploader FOREIGN KEY (uploader_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='첨부파일';

-- -----------------------------------------------------
-- 6. 조회수 추적 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS post_views (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    user_id BIGINT DEFAULT NULL COMMENT '사용자 ID (비로그인 시 null)',
    ip_address VARCHAR(45) DEFAULT NULL COMMENT 'IP 주소 (IPv6 지원)',
    user_agent TEXT DEFAULT NULL COMMENT '브라우저 정보',
    viewed_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '조회일시',
    PRIMARY KEY (id),
    KEY idx_post_id (post_id),
    KEY idx_user_id (user_id),
    KEY idx_viewed_at (viewed_at),
    CONSTRAINT fk_post_views_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_post_views_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='조회수 추적';

-- -----------------------------------------------------
-- 7. 다운로드 추적 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS attachment_downloads (
    id BIGINT NOT NULL AUTO_INCREMENT,
    attachment_id BIGINT NOT NULL COMMENT '첨부파일 ID',
    user_id BIGINT DEFAULT NULL COMMENT '다운로더 ID (비로그인 시 null)',
    ip_address VARCHAR(45) DEFAULT NULL COMMENT 'IP 주소',
    downloaded_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '다운로드일시',
    PRIMARY KEY (id),
    KEY idx_attachment_id (attachment_id),
    KEY idx_user_id (user_id),
    KEY idx_downloaded_at (downloaded_at),
    CONSTRAINT fk_attachment_downloads_attachment FOREIGN KEY (attachment_id) REFERENCES attachments (id),
    CONSTRAINT fk_attachment_downloads_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='다운로드 추적';

-- -----------------------------------------------------
-- 8. 해시태그 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS hashtags (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL COMMENT '태그 이름 (#제외, 소문자)',
    display_name VARCHAR(50) NOT NULL COMMENT '화면 표시용 태그명',
    description VARCHAR(255) DEFAULT NULL COMMENT '태그 설명',
    color VARCHAR(7) DEFAULT '#007bff' COMMENT '태그 색상 (HEX)',
    tag_category VARCHAR(30) DEFAULT NULL COMMENT '태그 카테고리 (SUBJECT/DIFFICULTY/TYPE 등)',
    is_active BIT(1) DEFAULT b'1' COMMENT '활성화 상태',
    created_by BIGINT DEFAULT NULL COMMENT '태그 생성자 ID',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6) DEFAULT NULL COMMENT '수정일시',
    deleted_at DATETIME(6) DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (id),
    KEY idx_name (name),
    KEY idx_tag_category (tag_category),
    KEY idx_is_active (is_active),
    CONSTRAINT fk_hashtags_creator FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='해시태그';

-- -----------------------------------------------------
-- 9. 게시글-해시태그 연결 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS post_hashtags (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    hashtag_id BIGINT NOT NULL COMMENT '해시태그 ID',
    created_by BIGINT NOT NULL COMMENT '태그 추가자 ID',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '연결 생성일시',
    PRIMARY KEY (id),
    UNIQUE KEY idx_post_hashtag (post_id, hashtag_id),
    KEY idx_hashtag_id (hashtag_id),
    KEY idx_created_by (created_by),
    CONSTRAINT fk_post_hashtags_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_post_hashtags_hashtag FOREIGN KEY (hashtag_id) REFERENCES hashtags (id),
    CONSTRAINT fk_post_hashtags_creator FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글-해시태그 연결';

-- -----------------------------------------------------
-- 10. 좋아요 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS post_likes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    post_id BIGINT DEFAULT NULL COMMENT '게시글 ID',
    comment_id BIGINT DEFAULT NULL COMMENT '댓글 ID',
    like_type VARCHAR(20) NOT NULL COMMENT '좋아요 유형 (POST/COMMENT)',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '좋아요 생성일시',
    deleted_at DATETIME(6) DEFAULT NULL COMMENT '좋아요 취소일시 (Soft Delete)',
    PRIMARY KEY (id),
    UNIQUE KEY idx_user_post_like (user_id, post_id),
    UNIQUE KEY idx_user_comment_like (user_id, comment_id),
    KEY idx_like_type (like_type),
    CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_post_likes_comment FOREIGN KEY (comment_id) REFERENCES comments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='좋아요';

-- -----------------------------------------------------
-- 11. 북마크 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS post_bookmarks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    bookmark_category VARCHAR(50) DEFAULT NULL COMMENT '북마크 카테고리',
    notes TEXT DEFAULT NULL COMMENT '개인 메모',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '북마크 생성일시',
    updated_at DATETIME(6) DEFAULT NULL COMMENT '수정일시',
    deleted_at DATETIME(6) DEFAULT NULL COMMENT '북마크 삭제일시 (Soft Delete)',
    PRIMARY KEY (id),
    UNIQUE KEY idx_user_post_bookmark (user_id, post_id),
    KEY idx_user_category (user_id, bookmark_category),
    CONSTRAINT fk_post_bookmarks_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_post_bookmarks_post FOREIGN KEY (post_id) REFERENCES posts (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='북마크';

-- -----------------------------------------------------
-- 12. 과제 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS assignments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL COMMENT '게시글 ID (제목/내용은 posts 테이블 참조)',
    course_id BIGINT NOT NULL COMMENT '강의 ID',
    due_date DATETIME(6) NOT NULL COMMENT '제출 마감일',
    max_score DECIMAL(5,2) NOT NULL COMMENT '만점',
    submission_method VARCHAR(20) NOT NULL COMMENT '제출방법 (FILE_UPLOAD/TEXT_INPUT/BOTH)',
    late_submission_allowed BIT(1) NOT NULL DEFAULT b'0' COMMENT '지각 제출 허용',
    late_penalty_percent DECIMAL(3,2) DEFAULT NULL COMMENT '지각 제출 감점 비율 (%)',
    max_file_size_mb INT DEFAULT 10 COMMENT '최대 파일 크기 (MB)',
    allowed_file_types VARCHAR(255) DEFAULT NULL COMMENT '허용 파일 확장자 (쉼표 구분)',
    instructions TEXT DEFAULT NULL COMMENT '제출 지침',
    created_by BIGINT NOT NULL COMMENT '생성자 ID',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6) DEFAULT NULL COMMENT '수정일시',
    deleted_at DATETIME(6) DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_assignments_post_id (post_id),
    KEY idx_course_id (course_id),
    KEY idx_due_date (due_date),
    KEY idx_created_by (created_by),
    CONSTRAINT fk_assignments_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_assignments_creator FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='과제';

-- -----------------------------------------------------
-- 13. 과제 제출 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS assignment_submissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    assignment_id BIGINT NOT NULL COMMENT '과제 ID',
    user_id BIGINT NOT NULL COMMENT '제출자 ID',
    content TEXT DEFAULT NULL COMMENT '텍스트 제출 내용',
    submitted_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '제출일시',
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED' COMMENT '제출 상태',
    score DECIMAL(5,2) DEFAULT NULL COMMENT '획득 점수',
    feedback TEXT DEFAULT NULL COMMENT '피드백',
    graded_at DATETIME(6) DEFAULT NULL COMMENT '채점일시',
    graded_by BIGINT DEFAULT NULL COMMENT '채점자 ID',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6) DEFAULT NULL COMMENT '수정일시',
    deleted_at DATETIME(6) DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (id),
    UNIQUE KEY idx_assignment_user (assignment_id, user_id),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_submitted_at (submitted_at),
    CONSTRAINT fk_assignment_submissions_assignment FOREIGN KEY (assignment_id) REFERENCES assignments (id),
    CONSTRAINT fk_assignment_submissions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_assignment_submissions_grader FOREIGN KEY (graded_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='과제 제출';

-- -----------------------------------------------------
-- 14. 시험 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS exams (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL COMMENT '게시글 ID (제목/내용은 posts 테이블 참조)',
    course_id BIGINT NOT NULL COMMENT '강의 ID',
    exam_type VARCHAR(20) NOT NULL COMMENT '시험 유형 (MIDTERM/FINAL/QUIZ/REGULAR)',
    exam_date DATETIME(6) NOT NULL COMMENT '시험 일시',
    duration_minutes INT NOT NULL COMMENT '시험 시간 (분)',
    total_score DECIMAL(5,2) NOT NULL COMMENT '총점',
    is_online BIT(1) NOT NULL DEFAULT b'0' COMMENT '온라인 시험 여부',
    location VARCHAR(100) DEFAULT NULL COMMENT '시험 장소',
    instructions TEXT DEFAULT NULL COMMENT '시험 안내사항',
    question_count INT DEFAULT NULL COMMENT '문제 수',
    passing_score DECIMAL(5,2) DEFAULT NULL COMMENT '합격 점수',
    created_by BIGINT NOT NULL COMMENT '생성자 ID',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6) DEFAULT NULL COMMENT '수정일시',
    deleted_at DATETIME(6) DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_exams_post_id (post_id),
    KEY idx_course_id (course_id),
    KEY idx_exam_date (exam_date),
    KEY idx_exam_type (exam_type),
    KEY idx_created_by (created_by),
    CONSTRAINT fk_exams_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_exams_creator FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='시험';

-- -----------------------------------------------------
-- 15. 시험 결과 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS exam_results (
    id BIGINT NOT NULL AUTO_INCREMENT,
    exam_id BIGINT NOT NULL COMMENT '시험 ID',
    user_id BIGINT NOT NULL COMMENT '응시자 ID',
    started_at DATETIME(6) DEFAULT NULL COMMENT '시험 시작 시간',
    submitted_at DATETIME(6) DEFAULT NULL COMMENT '제출 시간',
    score DECIMAL(5,2) DEFAULT NULL COMMENT '획득 점수',
    grade VARCHAR(2) DEFAULT NULL COMMENT '등급 (A+, A, B+ 등)',
    answer_data TEXT DEFAULT NULL COMMENT '답안 데이터 (JSON)',
    feedback TEXT DEFAULT NULL COMMENT '피드백',
    graded_at DATETIME(6) DEFAULT NULL COMMENT '채점일시',
    graded_by BIGINT DEFAULT NULL COMMENT '채점자 ID',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6) DEFAULT NULL COMMENT '수정일시',
    deleted_at DATETIME(6) DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (id),
    UNIQUE KEY idx_exam_user (exam_id, user_id),
    KEY idx_user_id (user_id),
    KEY idx_submitted_at (submitted_at),
    KEY idx_score (score),
    KEY idx_grade (grade),
    CONSTRAINT fk_exam_results_exam FOREIGN KEY (exam_id) REFERENCES exams (id),
    CONSTRAINT fk_exam_results_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_exam_results_grader FOREIGN KEY (graded_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='시험 결과';

-- -----------------------------------------------------
-- 16. 스터디모집 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS study_recruitments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL UNIQUE COMMENT '게시글 ID (제목/내용은 posts 테이블 참조)',
    study_type VARCHAR(20) NOT NULL COMMENT '스터디 유형 (EXAM_PREP/PROJECT/LANGUAGE/CERTIFICATION/READING/CODING/OTHER)',
    max_participants INT NOT NULL COMMENT '최대 참여 인원',
    recruitment_status VARCHAR(20) NOT NULL DEFAULT 'RECRUITING' COMMENT '모집 상태 (RECRUITING/COMPLETED/CANCELLED/IN_PROGRESS/FINISHED)',
    recruitment_deadline DATETIME(6) DEFAULT NULL COMMENT '모집 마감일',
    requirements TEXT DEFAULT NULL COMMENT '지원 자격/조건',
    PRIMARY KEY (id),
    UNIQUE KEY uk_study_recruitments_post_id (post_id),
    KEY idx_study_type (study_type),
    KEY idx_recruitment_status (recruitment_status),
    KEY idx_recruitment_deadline (recruitment_deadline),
    CONSTRAINT fk_study_recruitments_post FOREIGN KEY (post_id) REFERENCES posts (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='스터디모집';

-- -----------------------------------------------------
-- 17. 스터디 지원 테이블
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS study_applications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    study_recruitment_id BIGINT NOT NULL COMMENT '스터디모집 ID',
    applicant_id BIGINT NOT NULL COMMENT '지원자 ID',
    application_message TEXT DEFAULT NULL COMMENT '지원 메시지',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '지원 상태 (PENDING/APPROVED/REJECTED/WITHDRAWN)',
    applied_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '지원일시',
    processed_at DATETIME(6) DEFAULT NULL COMMENT '처리일시 (승인/거절)',
    process_message TEXT DEFAULT NULL COMMENT '처리 메시지 (거절 사유 등)',
    PRIMARY KEY (id),
    UNIQUE KEY idx_study_applicant (study_recruitment_id, applicant_id),
    KEY idx_applicant_id (applicant_id),
    KEY idx_status (status),
    KEY idx_applied_at (applied_at),
    CONSTRAINT fk_study_applications_recruitment FOREIGN KEY (study_recruitment_id) REFERENCES study_recruitments (id),
    CONSTRAINT fk_study_applications_applicant FOREIGN KEY (applicant_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='스터디 지원';
