-- 게시글/댓글 좋아요 통합 테이블 생성 (ERD 설계 기준)
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
