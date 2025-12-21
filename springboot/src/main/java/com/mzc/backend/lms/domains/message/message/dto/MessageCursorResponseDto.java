package com.mzc.backend.lms.domains.message.message.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 메시지 목록 커서 페이징 응답 DTO.
 */
@Getter
@Builder
public class MessageCursorResponseDto {

    private List<MessageResponseDto> messages;

    private Long nextCursor;

    private boolean hasMore;

    public static MessageCursorResponseDto of(List<MessageResponseDto> messages, int requestedSize) {
        boolean hasMore = messages.size() > requestedSize;

        List<MessageResponseDto> resultMessages = hasMore
                ? messages.subList(0, requestedSize)
                : messages;

        Long nextCursor = null;
        if (hasMore && !resultMessages.isEmpty()) {
            nextCursor = resultMessages.get(resultMessages.size() - 1).getMessageId();
        }

        return MessageCursorResponseDto.builder()
                .messages(resultMessages)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }
}
