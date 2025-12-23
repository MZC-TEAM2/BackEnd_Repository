package com.mzc.backend.lms.domains.message.conversation.swagger;

import com.mzc.backend.lms.domains.message.conversation.dto.ConversationListResponseDto;
import com.mzc.backend.lms.domains.message.conversation.dto.ConversationResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Conversation", description = "대화방 관리 API")
public interface ConversationControllerSwagger {
	
	@Operation(summary = "대화방 목록 조회", description = "내 대화방 목록을 조회합니다. (최근 메시지 순)")
	ResponseEntity<List<ConversationListResponseDto>> getConversations(
			@Parameter(hidden = true) Long userId);
	
	@Operation(summary = "대화방 조회/생성", description = "상대방과의 대화방을 조회하거나, 없으면 새로 생성합니다.")
	ResponseEntity<ConversationResponseDto> getOrCreateConversation(
			@Parameter(hidden = true) Long userId,
			@Parameter(description = "상대방 ID") Long otherUserId);
	
	@Operation(summary = "대화방 상세 조회", description = "특정 대화방 정보를 조회합니다.")
	ResponseEntity<ConversationResponseDto> getConversation(
			@Parameter(hidden = true) Long userId,
			@Parameter(description = "대화방 ID") Long conversationId);
	
	@Operation(summary = "대화방 삭제", description = "대화방을 삭제합니다. (소프트 삭제, 양쪽 모두 삭제 시 완전 삭제)")
	ResponseEntity<Void> deleteConversation(
			@Parameter(hidden = true) Long userId,
			@Parameter(description = "대화방 ID") Long conversationId);
	
	@Operation(summary = "대화방 읽음 처리", description = "대화방의 메시지를 모두 읽음 처리합니다.")
	ResponseEntity<Void> markAsRead(
			@Parameter(hidden = true) Long userId,
			@Parameter(description = "대화방 ID") Long conversationId);
	
	@Operation(summary = "전체 안읽음 수 조회", description = "모든 대화방의 안읽음 메시지 수 합계를 조회합니다.")
	ResponseEntity<Integer> getTotalUnreadCount(
			@Parameter(hidden = true) Long userId);
}
