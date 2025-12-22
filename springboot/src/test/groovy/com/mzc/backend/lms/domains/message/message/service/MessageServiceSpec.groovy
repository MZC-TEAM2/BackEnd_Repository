package com.mzc.backend.lms.domains.message.message.service

import com.mzc.backend.lms.domains.message.conversation.entity.Conversation
import com.mzc.backend.lms.domains.message.conversation.repository.ConversationRepository
import com.mzc.backend.lms.domains.message.message.dto.MessageSendRequestDto
import com.mzc.backend.lms.domains.message.message.entity.Message
import com.mzc.backend.lms.domains.message.message.repository.MessageRepository
import com.mzc.backend.lms.domains.message.sse.service.SseService
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile
import com.mzc.backend.lms.domains.user.user.entity.User
import com.mzc.backend.lms.domains.user.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

/**
 * MessageService 테스트
 * 메시지 전송, 조회, 삭제 기능 테스트
 */
class MessageServiceSpec extends Specification {

    def messageRepository = Mock(MessageRepository)
    def conversationRepository = Mock(ConversationRepository)
    def userRepository = Mock(UserRepository)
    def sseService = Mock(SseService)

    @Subject
    def messageService = new MessageService(
            messageRepository,
            conversationRepository,
            userRepository,
            sseService
    )

    def userProfile1
    def userProfile2
    def user1
    def user2
    def conversation
    def message

    def setup() {
        userProfile1 = Mock(UserProfile) {
            getName() >> "사용자1"
        }
        userProfile2 = Mock(UserProfile) {
            getName() >> "사용자2"
        }
        user1 = Mock(User) {
            getId() >> 1L
            getUserProfile() >> userProfile1
        }
        user2 = Mock(User) {
            getId() >> 2L
            getUserProfile() >> userProfile2
        }
        conversation = Mock(Conversation) {
            getId() >> 1L
            getUser1() >> user1
            getUser2() >> user2
        }
        message = Mock(Message) {
            getId() >> 1L
            getConversation() >> conversation
            getSender() >> user1
            getSenderId() >> 1L
            getReceiverId() >> 2L
            getContent() >> "테스트 메시지"
            getCreatedAt() >> LocalDateTime.now()
            isRead() >> false
            isVisibleTo(_) >> true
            isDeletedByBoth() >> false
        }
    }

    // ==================== 메시지 전송 테스트 ====================

    def "메시지를 전송한다"() {
        given: "메시지 전송 요청"
        def request = Mock(MessageSendRequestDto) {
            getConversationId() >> 1L
            getContent() >> "안녕하세요"
        }

        userRepository.findActiveById(1L) >> Optional.of(user1)
        conversationRepository.findById(1L) >> Optional.of(conversation)
        messageRepository.save(_) >> message

        when: "메시지를 전송하면"
        def result = messageService.sendMessage(1L, request)

        then: "메시지가 저장되고 알림이 전송된다"
        result != null
        1 * messageRepository.save(_) >> message
        1 * conversation.updateLastMessage("안녕하세요", 1L)
        1 * sseService.sendNewMessageNotification(2L, _)
    }

    def "존재하지 않는 사용자는 메시지를 전송할 수 없다"() {
        given: "존재하지 않는 사용자"
        def request = Mock(MessageSendRequestDto) {
            getConversationId() >> 1L
            getContent() >> "안녕하세요"
        }
        userRepository.findActiveById(999L) >> Optional.empty()

        when: "메시지를 전송하면"
        messageService.sendMessage(999L, request)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "존재하지 않는 대화방에는 메시지를 전송할 수 없다"() {
        given: "존재하지 않는 대화방"
        def request = Mock(MessageSendRequestDto) {
            getConversationId() >> 999L
            getContent() >> "안녕하세요"
        }
        userRepository.findActiveById(1L) >> Optional.of(user1)
        conversationRepository.findById(999L) >> Optional.empty()

        when: "메시지를 전송하면"
        messageService.sendMessage(1L, request)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "참여하지 않은 대화방에는 메시지를 전송할 수 없다"() {
        given: "참여하지 않은 대화방"
        def request = Mock(MessageSendRequestDto) {
            getConversationId() >> 1L
            getContent() >> "안녕하세요"
        }
        def otherUser = Mock(User) { getId() >> 999L }
        userRepository.findActiveById(999L) >> Optional.of(otherUser)
        conversationRepository.findById(1L) >> Optional.of(conversation)

        when: "메시지를 전송하면"
        messageService.sendMessage(999L, request)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    // ==================== 메시지 목록 조회 테스트 ====================

    def "대화방 메시지 목록을 조회한다"() {
        given: "메시지가 있는 대화방"
        conversationRepository.findById(1L) >> Optional.of(conversation)
        messageRepository.findByConversationIdWithLimit(1L, _) >> [message]

        when: "메시지 목록을 조회하면"
        def result = messageService.getMessages(1L, 1L, null, 20)

        then: "메시지 목록이 반환된다"
        result != null
        result.messages.size() == 1
    }

    def "커서 기반으로 이전 메시지를 조회한다"() {
        given: "커서가 있는 요청"
        conversationRepository.findById(1L) >> Optional.of(conversation)
        messageRepository.findByConversationIdWithCursor(1L, 100L, _) >> [message]

        when: "커서로 메시지를 조회하면"
        def result = messageService.getMessages(1L, 1L, 100L, 20)

        then: "커서 이전 메시지가 반환된다"
        result != null
    }

    // ==================== 메시지 삭제 테스트 ====================

    def "발신자가 메시지를 삭제한다"() {
        given: "발신자의 메시지"
        messageRepository.findById(1L) >> Optional.of(message)

        when: "발신자가 삭제하면"
        messageService.deleteMessage(1L, 1L)

        then: "발신자 삭제가 수행된다"
        1 * message.deleteBySender()
    }

    def "수신자가 메시지를 삭제한다"() {
        given: "수신자의 메시지"
        messageRepository.findById(1L) >> Optional.of(message)

        when: "수신자가 삭제하면"
        messageService.deleteMessage(1L, 2L)

        then: "수신자 삭제가 수행된다"
        1 * message.deleteByReceiver()
    }

    def "양쪽 모두 삭제하면 하드 삭제된다"() {
        given: "양쪽 모두 삭제한 메시지"
        def bothDeletedMessage = Mock(Message) {
            getId() >> 1L
            getConversation() >> conversation
            getSenderId() >> 1L
            isDeletedByBoth() >> true
        }
        messageRepository.findById(1L) >> Optional.of(bothDeletedMessage)

        when: "메시지를 삭제하면"
        messageService.deleteMessage(1L, 1L)

        then: "하드 삭제가 수행된다"
        1 * messageRepository.delete(bothDeletedMessage)
    }

    // ==================== 메시지 읽음 처리 테스트 ====================

    def "대화방 메시지를 읽음 처리한다"() {
        given: "존재하는 대화방"
        conversationRepository.findById(1L) >> Optional.of(conversation)
        messageRepository.markAllAsRead(1L, 1L) >> 5

        when: "읽음 처리하면"
        messageService.markMessagesAsRead(1L, 1L)

        then: "메시지가 읽음 처리된다"
        1 * messageRepository.markAllAsRead(1L, 1L)
        1 * conversation.markAsRead(1L)
    }
}
