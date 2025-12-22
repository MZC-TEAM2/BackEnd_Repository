package com.mzc.backend.lms.domains.board.service

import com.mzc.backend.lms.domains.board.entity.Attachment
import com.mzc.backend.lms.domains.board.enums.AttachmentType
import com.mzc.backend.lms.domains.board.repository.AttachmentRepository
import spock.lang.Specification
import spock.lang.Subject

import java.lang.reflect.Field

/**
 * AttachmentService 테스트
 * 첨부파일 조회, 삭제, 다운로드 기능 테스트
 */
class AttachmentServiceSpec extends Specification {

    def attachmentRepository = Mock(AttachmentRepository)

    @Subject
    def attachmentService = new AttachmentService(attachmentRepository)

    def setup() {
        // uploadDir 값 설정 (리플렉션 사용)
        Field uploadDirField = AttachmentService.getDeclaredField("uploadDir")
        uploadDirField.setAccessible(true)
        uploadDirField.set(attachmentService, "test-uploads")
    }

    def "첨부파일을 ID로 조회한다"() {
        given: "존재하는 첨부파일"
        def attachmentId = 1L
        def attachment = Mock(Attachment) {
            getId() >> attachmentId
            getOriginalName() >> "test.pdf"
            getStoredName() >> "uuid-test.pdf"
            getFilePath() >> "/uploads/uuid-test.pdf"
            getFileSize() >> 1024L
            getAttachmentType() >> AttachmentType.DOCUMENT
            getDownloadCount() >> 0
            getCreatedAt() >> null
        }

        attachmentRepository.findById(attachmentId) >> Optional.of(attachment)

        when: "첨부파일을 조회하면"
        def result = attachmentService.getAttachment(attachmentId)

        then: "첨부파일 정보가 반환된다"
        result != null
        result.originalName == "test.pdf"
    }

    def "존재하지 않는 첨부파일 조회 시 예외가 발생한다"() {
        given: "존재하지 않는 첨부파일 ID"
        def attachmentId = 999L
        attachmentRepository.findById(attachmentId) >> Optional.empty()

        when: "첨부파일을 조회하면"
        attachmentService.getAttachment(attachmentId)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "첨부파일을 삭제한다"() {
        given: "삭제할 첨부파일"
        def attachmentId = 1L
        def attachment = Mock(Attachment) {
            getId() >> attachmentId
            getStoredName() >> "uuid-test.pdf"
        }

        attachmentRepository.findById(attachmentId) >> Optional.of(attachment)

        when: "첨부파일을 삭제하면"
        attachmentService.deleteAttachment(attachmentId)

        then: "DB에서 삭제된다"
        1 * attachmentRepository.delete(attachment)
    }

    def "존재하지 않는 첨부파일 삭제 시 예외가 발생한다"() {
        given: "존재하지 않는 첨부파일 ID"
        def attachmentId = 999L
        attachmentRepository.findById(attachmentId) >> Optional.empty()

        when: "첨부파일을 삭제하면"
        attachmentService.deleteAttachment(attachmentId)

        then: "예외가 발생한다"
        thrown(RuntimeException)
    }

    def "첨부파일 ID 목록으로 조회한다"() {
        given: "여러 첨부파일 ID"
        def attachmentIds = [1L, 2L, 3L]
        def attachments = attachmentIds.collect { id ->
            Mock(Attachment) {
                getId() >> id
            }
        }

        attachmentRepository.findAllById(attachmentIds) >> attachments

        when: "첨부파일 목록을 조회하면"
        def result = attachmentService.getAttachmentsByIds(attachmentIds)

        then: "모든 첨부파일이 반환된다"
        result.size() == 3
    }

    def "빈 ID 목록으로 조회하면 빈 결과가 반환된다"() {
        given: "빈 첨부파일 ID 목록"
        def attachmentIds = []

        attachmentRepository.findAllById(attachmentIds) >> []

        when: "첨부파일 목록을 조회하면"
        def result = attachmentService.getAttachmentsByIds(attachmentIds)

        then: "빈 결과가 반환된다"
        result.isEmpty()
    }

    def "파일 다운로드 시 존재하지 않는 첨부파일이면 예외가 발생한다"() {
        given: "존재하지 않는 첨부파일 ID"
        def attachmentId = 999L
        attachmentRepository.findById(attachmentId) >> Optional.empty()

        when: "파일을 다운로드하면"
        attachmentService.getFile(attachmentId)

        then: "예외가 발생한다"
        thrown(RuntimeException)
    }
}
