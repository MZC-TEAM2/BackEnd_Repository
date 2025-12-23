package com.mzc.backend.lms.domains.message.message.swagger;

import com.mzc.backend.lms.domains.message.message.dto.MessageBulkSendRequestDto;
import com.mzc.backend.lms.domains.message.message.dto.MessageCursorResponseDto;
import com.mzc.backend.lms.domains.message.message.dto.MessageResponseDto;
import com.mzc.backend.lms.domains.message.message.dto.MessageSendRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Message", description = "메시지 송수신 API")
public interface MessageControllerSwagger {
	
	@Operation(summary = "메시지 전송", description = "대화방에 메시지를 전송합니다.")
	ResponseEntity<MessageResponseDto> sendMessage(
			@Parameter(hidden = true) Long userId,
			MessageSendRequestDto request);
	
	@Operation(summary = "다중 메시지 전송", description = "여러 사용자에게 각각 1:1 대화방으로 메시지를 전송합니다.")
	ResponseEntity<List<MessageResponseDto>> sendBulkMessages(
			@Parameter(hidden = true) Long userId,
			MessageBulkSendRequestDto request);
	
	@Operation(summary = "대화방 메시지 목록 조회",
			description = "대화방의 메시지 목록을 커서 기반으로 조회합니다. (최신순, 무한스크롤)")
	ResponseEntity<MessageCursorResponseDto> getMessages(
			@Parameter(hidden = true) Long userId,
			@Parameter(description = "대화방 ID") Long conversationId,
			@Parameter(description = "커서 (마지막 메시지 ID, 첫 페이지는 생략)") Long cursor,
			@Parameter(description = "조회 개수 (기본값: 20)") Integer size);
	
	@Operation(summary = "메시지 삭제", description = "메시지를 삭제합니다. (소프트 삭제, 양쪽 모두 삭제 시 완전 삭제)")
	ResponseEntity<Void> deleteMessage(
			@Parameter(hidden = true) Long userId,
			@Parameter(description = "메시지 ID") Long messageId);
	
	@Operation(summary = "대화방 메시지 읽음 처리", description = "대화방의 모든 메시지를 읽음 처리합니다.")
	ResponseEntity<Void> markMessagesAsRead(
			@Parameter(hidden = true) Long userId,
			@Parameter(description = "대화방 ID") Long conversationId);
}
