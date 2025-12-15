package com.mzc.backend.lms.domains.message.sse.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 메시지 알림 DTO (SSE 이벤트 페이로드)
 */
@Getter
@Builder
public class MessageNotificationDto {

    private String type;

    private Long conversationId;

    private Long messageId;

    private Long senderId;

    private String senderName;

    private String content;

    private LocalDateTime createdAt;

    public static MessageNotificationDto newMessage(
            Long conversationId,
            Long messageId,
            Long senderId,
            String senderName,
            String content
    ) {
        return MessageNotificationDto.builder()
                .type("NEW_MESSAGE")
                .conversationId(conversationId)
                .messageId(messageId)
                .senderId(senderId)
                .senderName(senderName)
                .content(content.length() > 100 ? content.substring(0, 100) + "..." : content)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
