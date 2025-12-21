-- 대화방 테이블 생성
-- 1:1 대화방을 관리하며, 각 사용자별 삭제 상태와 안읽음 수를 추적

CREATE TABLE conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    user1_unread_count INT NOT NULL DEFAULT 0,
    user2_unread_count INT NOT NULL DEFAULT 0,
    user1_deleted_at TIMESTAMP NULL,
    user2_deleted_at TIMESTAMP NULL,
    last_message_content VARCHAR(500) NULL,
    last_message_at TIMESTAMP NULL,
    last_message_sender_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_conversations_user1 FOREIGN KEY (user1_id) REFERENCES users(id),
    CONSTRAINT fk_conversations_user2 FOREIGN KEY (user2_id) REFERENCES users(id),
    CONSTRAINT uk_conversations_users UNIQUE (user1_id, user2_id),

    INDEX idx_conversations_user1 (user1_id),
    INDEX idx_conversations_user2 (user2_id),
    INDEX idx_conversations_last_message_at (last_message_at DESC)
);
