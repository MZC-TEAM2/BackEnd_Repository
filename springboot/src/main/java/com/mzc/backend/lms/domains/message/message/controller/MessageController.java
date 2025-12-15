package com.mzc.backend.lms.domains.message.message.controller;

import com.mzc.backend.lms.domains.message.message.dto.MessageBulkSendRequestDto;
import com.mzc.backend.lms.domains.message.message.dto.MessageResponseDto;
import com.mzc.backend.lms.domains.message.message.dto.MessageSendRequestDto;
import com.mzc.backend.lms.domains.message.message.service.MessageService;
import com.mzc.backend.lms.domains.message.message.swagger.MessageControllerSwagger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 메시지 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController implements MessageControllerSwagger {

    private final MessageService messageService;

    @Override
    @PostMapping
    public ResponseEntity<MessageResponseDto> sendMessage(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody MessageSendRequestDto request
    ) {
        MessageResponseDto result = messageService.sendMessage(userId, request);
        return ResponseEntity.ok(result);
    }

    @Override
    @PostMapping("/bulk")
    public ResponseEntity<List<MessageResponseDto>> sendBulkMessages(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody MessageBulkSendRequestDto request
    ) {
        List<MessageResponseDto> results = messageService.sendBulkMessages(userId, request);
        return ResponseEntity.ok(results);
    }

    @Override
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<List<MessageResponseDto>> getMessages(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long conversationId
    ) {
        List<MessageResponseDto> results = messageService.getMessages(conversationId, userId);
        return ResponseEntity.ok(results);
    }

    @Override
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long messageId
    ) {
        messageService.deleteMessage(messageId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long conversationId
    ) {
        messageService.markMessagesAsRead(conversationId, userId);
        return ResponseEntity.ok().build();
    }
}
