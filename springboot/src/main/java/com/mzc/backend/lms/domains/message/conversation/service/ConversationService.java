package com.mzc.backend.lms.domains.message.conversation.service;

import com.mzc.backend.lms.domains.message.conversation.dto.ConversationListResponseDto;
import com.mzc.backend.lms.domains.message.conversation.dto.ConversationResponseDto;
import com.mzc.backend.lms.domains.message.conversation.entity.Conversation;
import com.mzc.backend.lms.domains.message.conversation.repository.ConversationRepository;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 대화방 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    /**
     * 대화방 목록 조회
     */
    public List<ConversationListResponseDto> getConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findByUserIdOrderByLastMessageAtDesc(userId);

        return conversations.stream()
                .map(conversation -> ConversationListResponseDto.from(conversation, userId))
                .toList();
    }

    /**
     * 대화방 조회 또는 생성
     * 기존 대화방이 있으면 반환, 없으면 새로 생성
     */
    @Transactional
    public ConversationResponseDto getOrCreateConversation(Long myUserId, Long otherUserId) {
        if (myUserId.equals(otherUserId)) {
            throw new IllegalArgumentException("자기 자신과의 대화방은 생성할 수 없습니다.");
        }

        User myUser = userRepository.findActiveById(myUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + myUserId));

        User otherUser = userRepository.findActiveById(otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("상대방을 찾을 수 없습니다: " + otherUserId));

        Conversation conversation = conversationRepository.findByTwoUsers(myUserId, otherUserId)
                .orElseGet(() -> {
                    Conversation newConversation = Conversation.create(myUser, otherUser);
                    return conversationRepository.save(newConversation);
                });

        // 삭제된 대화방이면 복구
        if (conversation.isDeletedFor(myUserId)) {
            conversation.restoreFor(myUserId);
        }

        return ConversationResponseDto.from(conversation, myUserId);
    }

    /**
     * 대화방 상세 조회
     */
    public ConversationResponseDto getConversation(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("대화방을 찾을 수 없습니다: " + conversationId));

        validateParticipant(conversation, userId);

        if (conversation.isDeletedFor(userId)) {
            throw new IllegalArgumentException("삭제된 대화방입니다.");
        }

        return ConversationResponseDto.from(conversation, userId);
    }

    /**
     * 대화방 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteConversation(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("대화방을 찾을 수 없습니다: " + conversationId));

        validateParticipant(conversation, userId);

        conversation.deleteFor(userId);

        // 양쪽 모두 삭제했으면 하드 삭제
        if (conversation.isDeletedByBoth()) {
            conversationRepository.delete(conversation);
            log.info("대화방 완전 삭제: conversationId={}", conversationId);
        }
    }

    /**
     * 메시지 읽음 처리
     */
    @Transactional
    public void markAsRead(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("대화방을 찾을 수 없습니다: " + conversationId));

        validateParticipant(conversation, userId);

        conversation.markAsRead(userId);
    }

    /**
     * 전체 안읽음 수 조회
     */
    public int getTotalUnreadCount(Long userId) {
        return conversationRepository.getTotalUnreadCount(userId);
    }

    /**
     * 대화방 참여자 검증
     */
    private void validateParticipant(Conversation conversation, Long userId) {
        if (!userId.equals(conversation.getUser1().getId()) && !userId.equals(conversation.getUser2().getId())) {
            throw new IllegalArgumentException("대화방에 참여하지 않은 사용자입니다.");
        }
    }
}
