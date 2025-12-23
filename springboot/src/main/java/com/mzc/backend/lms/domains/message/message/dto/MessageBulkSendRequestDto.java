package com.mzc.backend.lms.domains.message.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 다중 메시지 전송 요청 DTO (여러 사용자에게 각각 1:1 대화방으로 전송)
 */
@Getter
@Setter
public class MessageBulkSendRequestDto {
	
	@NotEmpty(message = "수신자 목록은 필수입니다.")
	private List<Long> receiverIds;
	
	@NotBlank(message = "메시지 내용은 필수입니다.")
	private String content;
}
