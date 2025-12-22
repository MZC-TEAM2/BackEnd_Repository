package com.mzc.backend.lms.domains.message.conversation.service

import com.mzc.backend.lms.domains.message.conversation.entity.Conversation
import com.mzc.backend.lms.domains.message.conversation.repository.ConversationRepository
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile
import com.mzc.backend.lms.domains.user.user.entity.User
import com.mzc.backend.lms.domains.user.user.repository.UserRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

/**
 * ConversationService 테스트
 * 대화방 CRUD 기능 테스트
 */
class ConversationServiceSpec extends Specification {

    def conversationRepository = Mock(ConversationRepository)
    def userRepository = Mock(UserRepository)

    @Subject
    def conversationService = new ConversationService(
            conversationRepository,
            userRepository
    )

    def userProfile1
    def userProfile2
    def user1
    def user2
    def conversation

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
            getProfileImage() >> null
            getEmail() >> "user1@test.com"
        }
        user2 = Mock(User) {
            getId() >> 2L
            getUserProfile() >> userProfile2
            getProfileImage() >> null
            getEmail() >> "user2@test.com"
        }
        conversation = Mock(Conversation) {
            getId() >> 1L
            getUser1() >> user1
            getUser2() >> user2
            getOtherUser(1L) >> user2
            getOtherUser(2L) >> user1
            isDeletedFor(_) >> false
            isDeletedByBoth() >> false
            getCreatedAt() >> LocalDateTime.now()
        }
    }

    // ==================== 대화방 목록 조회 테스트 ====================

    def "대화방 목록을 조회한다"() {
        when: "대화방 목록을 조회하면"
        def result = conversationService.getConversations(1L)

        then: "대화방 목록이 반환된다"
        result.size() == 0
        1 * conversationRepository.findByUserIdOrderByLastMessageAtDesc(1L) >> []
    }

    // ==================== 대화방 생성/조회 테스트 ====================

    def "기존 대화방이 있으면 조회한다"() {
        when: "대화방을 조회/생성하면"
        conversationService.getOrCreateConversation(1L, 2L)

        then: "기존 대화방이 조회되고 저장되지 않는다"
        1 * userRepository.findActiveById(1L) >> Optional.of(user1)
        1 * userRepository.findActiveById(2L) >> Optional.of(user2)
        1 * conversationRepository.findByTwoUsers(1L, 2L) >> Optional.of(conversation)
        0 * conversationRepository.save(_)
    }

    def "기존 대화방이 없으면 생성한다"() {
        when: "대화방을 조회/생성하면"
        conversationService.getOrCreateConversation(1L, 2L)

        then: "새 대화방이 생성된다"
        1 * userRepository.findActiveById(1L) >> Optional.of(user1)
        1 * userRepository.findActiveById(2L) >> Optional.of(user2)
        1 * conversationRepository.findByTwoUsers(1L, 2L) >> Optional.empty()
        1 * conversationRepository.save(_) >> conversation
    }

    def "자기 자신과의 대화방은 생성할 수 없다"() {
        when: "자기 자신과 대화방을 생성하면"
        conversationService.getOrCreateConversation(1L, 1L)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "존재하지 않는 사용자와의 대화방은 생성할 수 없다"() {
        given: "존재하지 않는 상대방"
        userRepository.findActiveById(1L) >> Optional.of(user1)
        userRepository.findActiveById(999L) >> Optional.empty()

        when: "대화방을 생성하면"
        conversationService.getOrCreateConversation(1L, 999L)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    // ==================== 대화방 상세 조회 테스트 ====================

    def "대화방 상세를 조회한다"() {
        when: "대화방 상세를 조회하면"
        conversationService.getConversation(1L, 1L)

        then: "대화방이 조회된다"
        1 * conversationRepository.findById(1L) >> Optional.of(conversation)
    }

    def "참여하지 않은 대화방은 조회할 수 없다"() {
        given: "다른 사용자의 대화방"
        conversationRepository.findById(1L) >> Optional.of(conversation)

        when: "참여하지 않은 대화방을 조회하면"
        conversationService.getConversation(1L, 999L)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "삭제된 대화방은 조회할 수 없다"() {
        given: "삭제된 대화방"
        def deletedConversation = Mock(Conversation) {
            getId() >> 1L
            getUser1() >> user1
            getUser2() >> user2
            isDeletedFor(1L) >> true
        }
        conversationRepository.findById(1L) >> Optional.of(deletedConversation)

        when: "삭제된 대화방을 조회하면"
        conversationService.getConversation(1L, 1L)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    // ==================== 대화방 삭제 테스트 ====================

    def "대화방을 삭제한다"() {
        given: "존재하는 대화방"
        conversationRepository.findById(1L) >> Optional.of(conversation)

        when: "대화방을 삭제하면"
        conversationService.deleteConversation(1L, 1L)

        then: "소프트 삭제가 수행된다"
        1 * conversation.deleteFor(1L)
    }

    def "양쪽 모두 삭제하면 하드 삭제된다"() {
        given: "양쪽 모두 삭제한 대화방"
        def bothDeletedConversation = Mock(Conversation) {
            getId() >> 1L
            getUser1() >> user1
            getUser2() >> user2
            isDeletedByBoth() >> true
        }
        conversationRepository.findById(1L) >> Optional.of(bothDeletedConversation)

        when: "대화방을 삭제하면"
        conversationService.deleteConversation(1L, 1L)

        then: "하드 삭제가 수행된다"
        1 * conversationRepository.delete(bothDeletedConversation)
    }

    // ==================== 읽음 처리 테스트 ====================

    def "메시지를 읽음 처리한다"() {
        given: "존재하는 대화방"
        conversationRepository.findById(1L) >> Optional.of(conversation)

        when: "읽음 처리하면"
        conversationService.markAsRead(1L, 1L)

        then: "읽음 처리가 수행된다"
        1 * conversation.markAsRead(1L)
    }

    // ==================== 전체 안읽음 수 조회 테스트 ====================

    def "전체 안읽음 수를 조회한다"() {
        given: "안읽은 메시지가 있는 상태"
        conversationRepository.getTotalUnreadCount(1L) >> 5

        when: "안읽음 수를 조회하면"
        def result = conversationService.getTotalUnreadCount(1L)

        then: "안읽음 수가 반환된다"
        result == 5
    }
}
