package com.mzc.backend.lms.domains.message.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 메시지 전송 요청 DTO
 */
@Getter
@Setter
public class MessageSendRequestDto {

    @NotNull(message = "대화방 ID는 필수입니다.")
    private Long conversationId;

    @NotBlank(message = "메시지 내용은 필수입니다.")
    private String content;
}
