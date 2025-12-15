package com.mzc.backend.lms.domains.message.conversation.controller;

import com.mzc.backend.lms.domains.message.conversation.dto.ConversationListResponseDto;
import com.mzc.backend.lms.domains.message.conversation.dto.ConversationResponseDto;
import com.mzc.backend.lms.domains.message.conversation.service.ConversationService;
import com.mzc.backend.lms.domains.message.conversation.swagger.ConversationControllerSwagger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 대화방 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController implements ConversationControllerSwagger {

    private final ConversationService conversationService;

    @Override
    @GetMapping
    public ResponseEntity<List<ConversationListResponseDto>> getConversations(
            @AuthenticationPrincipal Long userId
    ) {
        List<ConversationListResponseDto> result = conversationService.getConversations(userId);
        return ResponseEntity.ok(result);
    }

    @Override
    @PostMapping("/with/{otherUserId}")
    public ResponseEntity<ConversationResponseDto> getOrCreateConversation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long otherUserId
    ) {
        ConversationResponseDto result = conversationService.getOrCreateConversation(userId, otherUserId);
        return ResponseEntity.ok(result);
    }

    @Override
    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationResponseDto> getConversation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long conversationId
    ) {
        ConversationResponseDto result = conversationService.getConversation(conversationId, userId);
        return ResponseEntity.ok(result);
    }

    @Override
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long conversationId
    ) {
        conversationService.deleteConversation(conversationId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long conversationId
    ) {
        conversationService.markAsRead(conversationId, userId);
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getTotalUnreadCount(
            @AuthenticationPrincipal Long userId
    ) {
        int count = conversationService.getTotalUnreadCount(userId);
        return ResponseEntity.ok(count);
    }
}
