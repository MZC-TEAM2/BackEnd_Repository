-- -----------------------------------------------------
-- V21: 강의 공지사항 테이블 생성
-- 강의별 공지사항 및 댓글 관리
-- -----------------------------------------------------

-- Course Notices Table (강의 공지사항)
CREATE TABLE IF NOT EXISTS course_notices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '공지사항 ID',
    course_id BIGINT NOT NULL COMMENT '강의 ID',
    title VARCHAR(200) NOT NULL COMMENT '제목',
    content TEXT NOT NULL COMMENT '내용',
    allow_comments BOOLEAN NOT NULL DEFAULT TRUE COMMENT '댓글 허용 여부',

    -- Auditing fields (BaseEntity + AuditableEntity)
    created_by BIGINT COMMENT '생성자 ID (교수)',
    updated_by BIGINT COMMENT '수정자 ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at TIMESTAMP NULL COMMENT '삭제일시',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '삭제 여부',

    CONSTRAINT fk_course_notice_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for course_notices
CREATE INDEX idx_course_notice_course_id ON course_notices(course_id);
CREATE INDEX idx_course_notice_is_deleted ON course_notices(is_deleted);
CREATE INDEX idx_course_notice_created_at ON course_notices(created_at DESC);


-- Course Notice Comments Table (공지사항 댓글)
CREATE TABLE IF NOT EXISTS course_notice_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '댓글 ID',
    notice_id BIGINT NOT NULL COMMENT '공지사항 ID',
    parent_id BIGINT NULL COMMENT '부모 댓글 ID (대댓글용)',
    content TEXT NOT NULL COMMENT '댓글 내용',
    author_id BIGINT NOT NULL COMMENT '작성자 ID',

    -- Auditing fields (BaseEntity + AuditableEntity)
    created_by BIGINT COMMENT '생성자 ID',
    updated_by BIGINT COMMENT '수정자 ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at TIMESTAMP NULL COMMENT '삭제일시',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '삭제 여부',

    CONSTRAINT fk_course_notice_comment_notice FOREIGN KEY (notice_id) REFERENCES course_notices(id) ON DELETE CASCADE,
    CONSTRAINT fk_course_notice_comment_parent FOREIGN KEY (parent_id) REFERENCES course_notice_comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for course_notice_comments
CREATE INDEX idx_course_notice_comment_notice_id ON course_notice_comments(notice_id);
CREATE INDEX idx_course_notice_comment_parent_id ON course_notice_comments(parent_id);
CREATE INDEX idx_course_notice_comment_is_deleted ON course_notice_comments(is_deleted);
CREATE INDEX idx_course_notice_comment_author_id ON course_notice_comments(author_id);


-- Add notification type for course notice
INSERT INTO notification_types (type_code, type_name, category, default_message_template, is_active) VALUES
('COURSE_NOTICE_CREATED', '강의 공지사항', '공지', '새 공지사항이 등록되었습니다.', true);
