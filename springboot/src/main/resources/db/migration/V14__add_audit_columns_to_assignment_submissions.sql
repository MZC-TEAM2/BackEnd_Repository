-- =====================================================
-- V14: assignment_submissions 테이블에 감사 컬럼 추가
-- =====================================================

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
