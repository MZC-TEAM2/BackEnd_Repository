package com.mzc.backend.lms.domains.message.message.service;

import com.mzc.backend.lms.domains.message.conversation.entity.Conversation;
import com.mzc.backend.lms.domains.message.conversation.repository.ConversationRepository;
import com.mzc.backend.lms.domains.message.message.dto.MessageBulkSendRequestDto;
import com.mzc.backend.lms.domains.message.message.dto.MessageCursorResponseDto;
import com.mzc.backend.lms.domains.message.message.dto.MessageResponseDto;
import com.mzc.backend.lms.domains.message.message.dto.MessageSendRequestDto;
import com.mzc.backend.lms.domains.message.message.entity.Message;
import com.mzc.backend.lms.domains.message.message.repository.MessageRepository;
import com.mzc.backend.lms.domains.message.sse.dto.MessageNotificationDto;
import com.mzc.backend.lms.domains.message.sse.service.SseService;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 메시지 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {
	
	private final MessageRepository messageRepository;
	private final ConversationRepository conversationRepository;
	private final UserRepository userRepository;
	private final SseService sseService;
	
	/**
	 * 메시지 전송
	 */
	@Transactional
	public MessageResponseDto sendMessage(Long senderId, MessageSendRequestDto request) {
		User sender = userRepository.findActiveById(senderId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + senderId));
		
		Conversation conversation = conversationRepository.findById(request.getConversationId())
				.orElseThrow(() -> new IllegalArgumentException("대화방을 찾을 수 없습니다: " + request.getConversationId()));
		
		validateParticipant(conversation, senderId);
		
		Message message = Message.create(conversation, sender, request.getContent());
		messageRepository.save(message);
		
		// 대화방 마지막 메시지 업데이트
		conversation.updateLastMessage(request.getContent(), senderId);
		
		String senderName = getSenderName(sender);
		
		// SSE 알림 전송
		Long receiverId = message.getReceiverId();
		sendNotification(receiverId, conversation.getId(), message.getId(), senderId, senderName, request.getContent());
		
		return MessageResponseDto.from(message, senderId, senderName);
	}
	
	/**
	 * 다중 메시지 전송 (여러 사용자에게 각각 1:1 대화방으로)
	 */
	@Transactional
	public List<MessageResponseDto> sendBulkMessages(Long senderId, MessageBulkSendRequestDto request) {
		User sender = userRepository.findActiveById(senderId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + senderId));
		
		List<MessageResponseDto> results = new ArrayList<>();
		String senderName = getSenderName(sender);
		
		for (Long receiverId : request.getReceiverIds()) {
			if (senderId.equals(receiverId)) {
				continue; // 자기 자신에게는 전송하지 않음
			}
			
			User receiver = userRepository.findActiveById(receiverId).orElse(null);
			if (receiver == null) {
				log.warn("수신자를 찾을 수 없습니다: {}", receiverId);
				continue;
			}
			
			// 대화방 조회 또는 생성
			Conversation conversation = conversationRepository.findByTwoUsers(senderId, receiverId)
					.orElseGet(() -> {
						Conversation newConversation = Conversation.create(sender, receiver);
						return conversationRepository.save(newConversation);
					});
			
			// 삭제된 대화방이면 복구
			if (conversation.isDeletedFor(senderId)) {
				conversation.restoreFor(senderId);
			}
			
			// 메시지 생성 및 저장
			Message message = Message.create(conversation, sender, request.getContent());
			messageRepository.save(message);
			
			// 대화방 마지막 메시지 업데이트
			conversation.updateLastMessage(request.getContent(), senderId);
			
			// SSE 알림 전송
			sendNotification(receiverId, conversation.getId(), message.getId(), senderId, senderName, request.getContent());
			
			results.add(MessageResponseDto.from(message, senderId, senderName));
		}
		
		return results;
	}
	
	/**
	 * 대화방 메시지 목록 조회 (커서 기반 페이징)
	 */
	public MessageCursorResponseDto getMessages(Long conversationId, Long userId, Long cursor, Integer size) {
		Conversation conversation = conversationRepository.findById(conversationId)
				.orElseThrow(() -> new IllegalArgumentException("대화방을 찾을 수 없습니다: " + conversationId));
		
		validateParticipant(conversation, userId);
		
		int requestedSize = size != null ? size : 20;
		int fetchSize = requestedSize + 1; // hasMore 판단용
		
		List<Message> messages;
		if (cursor == null) {
			// 첫 페이지
			messages = messageRepository.findByConversationIdWithLimit(
					conversationId, PageRequest.of(0, fetchSize));
		} else {
			// 커서 이후 페이지
			messages = messageRepository.findByConversationIdWithCursor(
					conversationId, cursor, PageRequest.of(0, fetchSize));
		}
		
		List<MessageResponseDto> responseDtos = messages.stream()
				.filter(message -> message.isVisibleTo(userId))
				.map(message -> {
					String senderName = getSenderName(message.getSender());
					return MessageResponseDto.from(message, userId, senderName);
				})
				.toList();
		
		return MessageCursorResponseDto.of(responseDtos, requestedSize);
	}
	
	/**
	 * 메시지 삭제 (소프트 삭제)
	 */
	@Transactional
	public void deleteMessage(Long messageId, Long userId) {
		Message message = messageRepository.findById(messageId)
				.orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다: " + messageId));
		
		Conversation conversation = message.getConversation();
		validateParticipant(conversation, userId);
		
		if (userId.equals(message.getSenderId())) {
			message.deleteBySender();
		} else {
			message.deleteByReceiver();
		}
		
		// 양쪽 모두 삭제했으면 하드 삭제
		if (message.isDeletedByBoth()) {
			messageRepository.delete(message);
			log.info("메시지 완전 삭제: messageId={}", messageId);
		}
	}
	
	/**
	 * 대화방 메시지 읽음 처리
	 */
	@Transactional
	public void markMessagesAsRead(Long conversationId, Long userId) {
		Conversation conversation = conversationRepository.findById(conversationId)
				.orElseThrow(() -> new IllegalArgumentException("대화방을 찾을 수 없습니다: " + conversationId));
		
		validateParticipant(conversation, userId);
		
		// 내가 수신한 메시지들 읽음 처리
		int count = messageRepository.markAllAsRead(conversationId, userId);
		
		// 대화방 안읽음 수 초기화
		conversation.markAsRead(userId);
		
		log.debug("메시지 읽음 처리: conversationId={}, userId={}, count={}", conversationId, userId, count);
	}
	
	/**
	 * 대화방 참여자 검증
	 */
	private void validateParticipant(Conversation conversation, Long userId) {
		if (!userId.equals(conversation.getUser1().getId()) && !userId.equals(conversation.getUser2().getId())) {
			throw new IllegalArgumentException("대화방에 참여하지 않은 사용자입니다.");
		}
	}
	
	/**
	 * 발신자 이름 조회
	 */
	private String getSenderName(User sender) {
		UserProfile profile = sender.getUserProfile();
		return profile != null ? profile.getName() : null;
	}
	
	/**
	 * SSE 알림 전송
	 */
	private void sendNotification(Long receiverId, Long conversationId, Long messageId,
	                              Long senderId, String senderName, String content) {
		MessageNotificationDto notification = MessageNotificationDto.newMessage(
				conversationId, messageId, senderId, senderName, content);
		sseService.sendNewMessageNotification(receiverId, notification);
	}
}
