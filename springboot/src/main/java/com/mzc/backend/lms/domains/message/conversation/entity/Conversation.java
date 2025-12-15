package com.mzc.backend.lms.domains.message.conversation.entity;

import com.mzc.backend.lms.domains.user.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 대화방 엔티티
 * 1:1 대화방을 관리하며, 각 사용자별 삭제 상태와 안읽음 수를 추적
 */
@Entity
@Table(name = "conversations", indexes = {
    @Index(name = "idx_conversations_user1", columnList = "user1_id"),
    @Index(name = "idx_conversations_user2", columnList = "user2_id"),
    @Index(name = "idx_conversations_last_message_at", columnList = "last_message_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @Column(name = "user1_unread_count", nullable = false)
    private int user1UnreadCount = 0;

    @Column(name = "user2_unread_count", nullable = false)
    private int user2UnreadCount = 0;

    @Column(name = "user1_deleted_at")
    private LocalDateTime user1DeletedAt;

    @Column(name = "user2_deleted_at")
    private LocalDateTime user2DeletedAt;

    @Column(name = "last_message_content", length = 500)
    private String lastMessageContent;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "last_message_sender_id")
    private Long lastMessageSenderId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private Conversation(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
    }

    /**
     * 대화방 생성 (user1_id < user2_id 순서로 저장하여 중복 방지)
     */
    public static Conversation create(User userA, User userB) {
        if (userA.getId() < userB.getId()) {
            return Conversation.builder()
                    .user1(userA)
                    .user2(userB)
                    .build();
        } else {
            return Conversation.builder()
                    .user1(userB)
                    .user2(userA)
                    .build();
        }
    }

    /**
     * 마지막 메시지 업데이트
     */
    public void updateLastMessage(String content, Long senderId) {
        this.lastMessageContent = content != null && content.length() > 500
                ? content.substring(0, 500)
                : content;
        this.lastMessageAt = LocalDateTime.now();
        this.lastMessageSenderId = senderId;

        // 상대방 안읽음 수 증가
        if (senderId.equals(user1.getId())) {
            this.user2UnreadCount++;
        } else {
            this.user1UnreadCount++;
        }

        // 삭제된 대화방 복구 (새 메시지가 오면 다시 보이게)
        if (senderId.equals(user1.getId()) && user2DeletedAt != null) {
            this.user2DeletedAt = null;
        } else if (senderId.equals(user2.getId()) && user1DeletedAt != null) {
            this.user1DeletedAt = null;
        }
    }

    /**
     * 메시지 읽음 처리
     */
    public void markAsRead(Long userId) {
        if (userId.equals(user1.getId())) {
            this.user1UnreadCount = 0;
        } else if (userId.equals(user2.getId())) {
            this.user2UnreadCount = 0;
        }
    }

    /**
     * 대화방 삭제 (소프트 삭제)
     */
    public void deleteFor(Long userId) {
        if (userId.equals(user1.getId())) {
            this.user1DeletedAt = LocalDateTime.now();
        } else if (userId.equals(user2.getId())) {
            this.user2DeletedAt = LocalDateTime.now();
        }
    }

    /**
     * 대화방 복구
     */
    public void restoreFor(Long userId) {
        if (userId.equals(user1.getId())) {
            this.user1DeletedAt = null;
        } else if (userId.equals(user2.getId())) {
            this.user2DeletedAt = null;
        }
    }

    /**
     * 양쪽 모두 삭제했는지 확인
     */
    public boolean isDeletedByBoth() {
        return user1DeletedAt != null && user2DeletedAt != null;
    }

    /**
     * 특정 사용자에 대해 삭제되었는지 확인
     */
    public boolean isDeletedFor(Long userId) {
        if (userId.equals(user1.getId())) {
            return user1DeletedAt != null;
        } else if (userId.equals(user2.getId())) {
            return user2DeletedAt != null;
        }
        return false;
    }

    /**
     * 상대방 조회
     */
    public User getOtherUser(Long myUserId) {
        return myUserId.equals(user1.getId()) ? user2 : user1;
    }

    /**
     * 안읽음 수 조회
     */
    public int getUnreadCount(Long userId) {
        if (userId.equals(user1.getId())) {
            return user1UnreadCount;
        } else if (userId.equals(user2.getId())) {
            return user2UnreadCount;
        }
        return 0;
    }
}
