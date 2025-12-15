package com.mzc.backend.lms.domains.message.message.entity;

import com.mzc.backend.lms.domains.message.conversation.entity.Conversation;
import com.mzc.backend.lms.domains.user.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 메시지 엔티티
 */
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_messages_conversation_created", columnList = "conversation_id, created_at DESC"),
    @Index(name = "idx_messages_sender", columnList = "sender_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "deleted_by_sender", nullable = false)
    private boolean deletedBySender = false;

    @Column(name = "deleted_by_receiver", nullable = false)
    private boolean deletedByReceiver = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Message(Conversation conversation, User sender, String content) {
        this.conversation = conversation;
        this.sender = sender;
        this.content = content;
    }

    /**
     * 메시지 생성
     */
    public static Message create(Conversation conversation, User sender, String content) {
        return Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .build();
    }

    /**
     * 메시지 읽음 처리
     */
    public void markAsRead() {
        if (this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }

    /**
     * 읽음 여부 확인
     */
    public boolean isRead() {
        return this.readAt != null;
    }

    /**
     * 메시지 삭제 (발신자)
     */
    public void deleteBySender() {
        this.deletedBySender = true;
    }

    /**
     * 메시지 삭제 (수신자)
     */
    public void deleteByReceiver() {
        this.deletedByReceiver = true;
    }

    /**
     * 특정 사용자가 삭제했는지 확인
     */
    public boolean isDeletedBy(Long userId) {
        if (userId.equals(sender.getId())) {
            return deletedBySender;
        } else {
            return deletedByReceiver;
        }
    }

    /**
     * 양쪽 모두 삭제했는지 확인
     */
    public boolean isDeletedByBoth() {
        return deletedBySender && deletedByReceiver;
    }

    /**
     * 특정 사용자에게 보여야 하는지 확인
     */
    public boolean isVisibleTo(Long userId) {
        return !isDeletedBy(userId);
    }

    /**
     * 발신자 ID 조회
     */
    public Long getSenderId() {
        return sender.getId();
    }

    /**
     * 수신자 ID 조회
     */
    public Long getReceiverId() {
        User user1 = conversation.getUser1();
        User user2 = conversation.getUser2();
        return sender.getId().equals(user1.getId()) ? user2.getId() : user1.getId();
    }
}
