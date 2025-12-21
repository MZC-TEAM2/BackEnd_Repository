package com.mzc.backend.lms.domains.message.conversation.dto;

import com.mzc.backend.lms.domains.message.conversation.entity.Conversation;
import com.mzc.backend.lms.domains.user.auth.encryption.annotation.Encrypted;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfileImage;
import com.mzc.backend.lms.domains.user.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 대화방 목록 응답 DTO
 */
@Getter
@Builder
public class ConversationListResponseDto {

    private Long conversationId;

    private Long otherUserId;

    @Encrypted
    private String otherUserName;

    private String otherUserThumbnailUrl;

    private String lastMessageContent;

    private LocalDateTime lastMessageAt;

    private boolean isLastMessageMine;

    private int unreadCount;

    public static ConversationListResponseDto from(Conversation conversation, Long myUserId) {
        User otherUser = conversation.getOtherUser(myUserId);
        UserProfile otherProfile = otherUser.getUserProfile();
        UserProfileImage otherProfileImage = otherUser.getProfileImage();

        return ConversationListResponseDto.builder()
                .conversationId(conversation.getId())
                .otherUserId(otherUser.getId())
                .otherUserName(otherProfile != null ? otherProfile.getName() : null)
                .otherUserThumbnailUrl(otherProfileImage != null ? otherProfileImage.getThumbnailUrl() : null)
                .lastMessageContent(conversation.getLastMessageContent())
                .lastMessageAt(conversation.getLastMessageAt())
                .isLastMessageMine(myUserId.equals(conversation.getLastMessageSenderId()))
                .unreadCount(conversation.getUnreadCount(myUserId))
                .build();
    }
}
