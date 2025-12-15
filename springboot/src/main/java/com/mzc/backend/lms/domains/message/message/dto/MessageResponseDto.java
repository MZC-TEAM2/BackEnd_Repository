package com.mzc.backend.lms.domains.message.message.dto;

import com.mzc.backend.lms.domains.message.message.entity.Message;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 메시지 응답 DTO
 */
@Getter
@Builder
public class MessageResponseDto {

    private Long messageId;

    private Long senderId;

    private String senderName;

    private String content;

    private boolean isMine;

    private boolean isRead;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;

    public static MessageResponseDto from(Message message, Long myUserId, String senderName) {
        return MessageResponseDto.builder()
                .messageId(message.getId())
                .senderId(message.getSenderId())
                .senderName(senderName)
                .content(message.getContent())
                .isMine(myUserId.equals(message.getSenderId()))
                .isRead(message.isRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
