-- 메시지 테이블 생성
-- 대화방 내 메시지를 저장하며, 읽음 확인 및 소프트 삭제 지원

CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    read_at TIMESTAMP NULL,
    deleted_by_sender BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by_receiver BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id),

    INDEX idx_messages_conversation_created (conversation_id, created_at DESC),
    INDEX idx_messages_sender (sender_id)
);
