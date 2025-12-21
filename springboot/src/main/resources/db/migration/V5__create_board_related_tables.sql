-- =====================================================
-- V5: Board 관련 추가 테이블 생성 (통합 파일)
-- 원본: V8 + V14 + V16 + V17 통합
-- =====================================================

-- -----------------------------------------------------
-- 1. 게시글/댓글 좋아요 통합 테이블 (원본: V8)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS post_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '좋아요 고유 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    post_id BIGINT COMMENT '게시글 ID (좋아요 유형이 POST인 경우)',
    comment_id BIGINT COMMENT '댓글 ID (좋아요 유형이 COMMENT인 경우)',
    like_type VARCHAR(20) NOT NULL COMMENT '좋아요 유형 (POST/COMMENT)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '좋아요 생성일시',
    deleted_at TIMESTAMP NULL COMMENT '좋아요 취소일시 (Soft Delete)',
    
    -- 외래키 제약조건
    CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_likes_comment FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    
    -- CHECK 제약조건 (like_type 값 제한)
    CONSTRAINT chk_like_type CHECK (like_type IN ('POST', 'COMMENT')),
    
    -- 유니크 제약조건 (한 사용자는 한 게시글에 좋아요 1개만)
    CONSTRAINT uk_user_post_like UNIQUE (user_id, post_id),
    
    -- 유니크 제약조건 (한 사용자는 한 댓글에 좋아요 1개만)
    CONSTRAINT uk_user_comment_like UNIQUE (user_id, comment_id),
    
    -- 인덱스
    INDEX idx_post_likes_user_id (user_id),
    INDEX idx_post_likes_post_id (post_id),
    INDEX idx_post_likes_comment_id (comment_id),
    INDEX idx_post_likes_type (like_type),
    INDEX idx_post_likes_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글/댓글 좋아요 통합 테이블';

-- -----------------------------------------------------
-- 2. assignment_submissions 테이블 감사 컬럼 추가 (원본: V14)
-- -----------------------------------------------------
-- created_by, updated_by, is_deleted 컬럼 추가
ALTER TABLE assignment_submissions
ADD COLUMN created_by BIGINT DEFAULT NULL COMMENT '생성자 ID' AFTER deleted_at,
ADD COLUMN updated_by BIGINT DEFAULT NULL COMMENT '수정자 ID' AFTER created_by,
ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부' AFTER updated_by;

-- 인덱스 추가
ALTER TABLE assignment_submissions
ADD INDEX idx_created_by (created_by),
ADD INDEX idx_is_deleted (is_deleted);

-- 외래키 추가
ALTER TABLE assignment_submissions
ADD CONSTRAINT fk_assignment_submissions_creator FOREIGN KEY (created_by) REFERENCES users (id),
ADD CONSTRAINT fk_assignment_submissions_updater FOREIGN KEY (updated_by) REFERENCES users (id);

-- -----------------------------------------------------
-- 3. 과제 제출 첨부파일 관계 테이블 (원본: V16)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS submission_attachments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    submission_id BIGINT NOT NULL COMMENT '과제 제출 ID',
    attachment_id BIGINT NOT NULL COMMENT '첨부파일 ID',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_submission_attachment (submission_id, attachment_id),
    KEY idx_submission_id (submission_id),
    KEY idx_attachment_id (attachment_id),
    CONSTRAINT fk_submission_attachments_submission FOREIGN KEY (submission_id) REFERENCES assignment_submissions (id) ON DELETE CASCADE,
    CONSTRAINT fk_submission_attachments_attachment FOREIGN KEY (attachment_id) REFERENCES attachments (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='과제 제출 첨부파일';

-- -----------------------------------------------------
-- 4. assignment_submissions 재제출 컬럼 추가 (원본: V17)
-- -----------------------------------------------------
ALTER TABLE assignment_submissions
    ADD COLUMN allow_resubmission BOOLEAN DEFAULT FALSE COMMENT '재제출 허용 여부',
    ADD COLUMN resubmission_deadline DATETIME(6) COMMENT '재제출 마감일';

-- 기존 데이터에 대한 기본값 설정
UPDATE assignment_submissions
SET allow_resubmission = FALSE
WHERE allow_resubmission IS NULL;
