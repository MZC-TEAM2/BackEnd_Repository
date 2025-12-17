-- =====================================================
-- V16: 과제 제출 첨부파일 관계 테이블 생성
-- =====================================================

-- 과제 제출-첨부파일 매핑 테이블
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
