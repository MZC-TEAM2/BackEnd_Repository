package com.mzc.backend.lms.domains.message.conversation.controller;

import com.mzc.backend.lms.domains.message.conversation.dto.ConversationListResponseDto;
import com.mzc.backend.lms.domains.message.conversation.dto.ConversationResponseDto;
import com.mzc.backend.lms.domains.message.conversation.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 대화방 컨트롤러
 */
@Tag(name = "Conversation", description = "대화방 관리 API")
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @Operation(summary = "대화방 목록 조회", description = "내 대화방 목록을 조회합니다. (최근 메시지 순)")
    @GetMapping
    public ResponseEntity<List<ConversationListResponseDto>> getConversations(
            @AuthenticationPrincipal Long userId
    ) {
        List<ConversationListResponseDto> result = conversationService.getConversations(userId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "대화방 조회/생성", description = "상대방과의 대화방을 조회하거나, 없으면 새로 생성합니다.")
    @PostMapping("/with/{otherUserId}")
    public ResponseEntity<ConversationResponseDto> getOrCreateConversation(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "상대방 ID") @PathVariable Long otherUserId
    ) {
        ConversationResponseDto result = conversationService.getOrCreateConversation(userId, otherUserId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "대화방 상세 조회", description = "특정 대화방 정보를 조회합니다.")
    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationResponseDto> getConversation(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "대화방 ID") @PathVariable Long conversationId
    ) {
        ConversationResponseDto result = conversationService.getConversation(conversationId, userId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "대화방 삭제", description = "대화방을 삭제합니다. (소프트 삭제, 양쪽 모두 삭제 시 완전 삭제)")
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "대화방 ID") @PathVariable Long conversationId
    ) {
        conversationService.deleteConversation(conversationId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "대화방 읽음 처리", description = "대화방의 메시지를 모두 읽음 처리합니다.")
    @PostMapping("/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "대화방 ID") @PathVariable Long conversationId
    ) {
        conversationService.markAsRead(conversationId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "전체 안읽음 수 조회", description = "모든 대화방의 안읽음 메시지 수 합계를 조회합니다.")
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getTotalUnreadCount(
            @AuthenticationPrincipal Long userId
    ) {
        int count = conversationService.getTotalUnreadCount(userId);
        return ResponseEntity.ok(count);
    }
}
