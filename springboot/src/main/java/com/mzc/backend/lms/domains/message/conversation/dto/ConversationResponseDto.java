package com.mzc.backend.lms.domains.message.conversation.dto;

import com.mzc.backend.lms.domains.message.conversation.entity.Conversation;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfileImage;
import com.mzc.backend.lms.domains.user.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 대화방 상세 응답 DTO
 */
@Getter
@Builder
public class ConversationResponseDto {

    private Long conversationId;

    private Long otherUserId;

    private String otherUserName;

    private String otherUserEmail;

    private String otherUserThumbnailUrl;

    private LocalDateTime createdAt;

    public static ConversationResponseDto from(Conversation conversation, Long myUserId) {
        User otherUser = conversation.getOtherUser(myUserId);
        UserProfile otherProfile = otherUser.getUserProfile();
        UserProfileImage otherProfileImage = otherUser.getProfileImage();

        return ConversationResponseDto.builder()
                .conversationId(conversation.getId())
                .otherUserId(otherUser.getId())
                .otherUserName(otherProfile != null ? otherProfile.getName() : null)
                .otherUserEmail(otherUser.getEmail())
                .otherUserThumbnailUrl(otherProfileImage != null ? otherProfileImage.getThumbnailUrl() : null)
                .createdAt(conversation.getCreatedAt())
                .build();
    }
}
