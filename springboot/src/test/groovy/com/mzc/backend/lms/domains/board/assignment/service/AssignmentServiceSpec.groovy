package com.mzc.backend.lms.domains.board.assignment.service

import com.mzc.backend.lms.domains.board.assignment.dto.request.AssignmentCreateRequestDto
import com.mzc.backend.lms.domains.board.assignment.dto.request.AssignmentGradeRequestDto
import com.mzc.backend.lms.domains.board.assignment.dto.request.AssignmentSubmissionRequestDto
import com.mzc.backend.lms.domains.board.assignment.dto.request.AssignmentUpdateRequestDto
import com.mzc.backend.lms.domains.board.assignment.entity.Assignment
import com.mzc.backend.lms.domains.board.assignment.entity.AssignmentSubmission
import com.mzc.backend.lms.domains.board.assignment.repository.AssignmentRepository
import com.mzc.backend.lms.domains.board.assignment.repository.AssignmentSubmissionRepository
import com.mzc.backend.lms.domains.board.entity.BoardCategory
import com.mzc.backend.lms.domains.board.entity.Post
import com.mzc.backend.lms.domains.board.enums.BoardType
import com.mzc.backend.lms.domains.board.enums.PostType
import com.mzc.backend.lms.domains.board.exception.BoardException
import com.mzc.backend.lms.domains.board.repository.AttachmentRepository
import com.mzc.backend.lms.domains.board.repository.BoardCategoryRepository
import com.mzc.backend.lms.domains.board.repository.PostRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

/**
 * AssignmentService Spock 테스트
 * 과제 CRUD, 제출, 채점 기능 테스트
 */
class AssignmentServiceSpec extends Specification {

    def assignmentRepository = Mock(AssignmentRepository)
    def submissionRepository = Mock(AssignmentSubmissionRepository)
    def postRepository = Mock(PostRepository)
    def boardCategoryRepository = Mock(BoardCategoryRepository)
    def attachmentRepository = Mock(AttachmentRepository)

    @Subject
    def assignmentService = new AssignmentService(
            assignmentRepository,
            submissionRepository,
            postRepository,
            boardCategoryRepository,
            attachmentRepository
    )

    def boardCategory
    def post
    def assignment

    def setup() {
        boardCategory = Mock(BoardCategory) {
            getId() >> 1L
            getBoardType() >> BoardType.ASSIGNMENT
        }

        post = Mock(Post) {
            getId() >> 1L
            getTitle() >> "과제 제목"
            getContent() >> "과제 내용"
            getAuthorId() >> 100L
            getCreatedBy() >> 100L
            getUpdatedBy() >> null
            getPostType() >> PostType.ASSIGNMENT
            isAnonymous() >> false
            getViewCount() >> 0
            getLikeCount() >> 0
            getCreatedAt() >> LocalDateTime.now()
            getUpdatedAt() >> null
            getCategory() >> boardCategory
            getComments() >> []
            getAttachments() >> []
            getPostHashtags() >> []
        }

        assignment = Mock(Assignment) {
            getId() >> 1L
            getPost() >> post
            getCourseId() >> 10L
            getDueDate() >> LocalDateTime.now().plusDays(7)
            getMaxScore() >> BigDecimal.valueOf(100)
            getSubmissionMethod() >> "FILE"
            getLateSubmissionAllowed() >> true
            getLatePenaltyPercent() >> BigDecimal.valueOf(10)
            getMaxFileSizeMb() >> 10
            getAllowedFileTypes() >> "pdf,docx"
            getInstructions() >> "제출 안내"
            getIsDeleted() >> false
            getCreatedBy() >> 100L
            getUpdatedBy() >> null
            getCreatedAt() >> LocalDateTime.now()
            getUpdatedAt() >> null
        }
    }

    def createSubmissionMock(Long id, boolean isGraded, boolean canResubmit) {
        return Mock(AssignmentSubmission) {
            getId() >> id
            getAssignment() >> assignment
            getUserId() >> 200L
            getContent() >> "제출 내용"
            getSubmittedAt() >> LocalDateTime.now()
            getStatus() >> (isGraded ? "GRADED" : "SUBMITTED")
            getScore() >> (isGraded ? BigDecimal.valueOf(85) : null)
            getFeedback() >> (isGraded ? "피드백" : null)
            getGradedAt() >> (isGraded ? LocalDateTime.now() : null)
            getGradedBy() >> (isGraded ? 100L : null)
            getAllowResubmission() >> canResubmit
            getResubmissionDeadline() >> null
            getCreatedAt() >> LocalDateTime.now()
            getUpdatedAt() >> null
            getAttachments() >> []
            it.isGraded() >> isGraded
            it.canResubmit() >> canResubmit
        }
    }

    def "과제를 정상적으로 등록한다"() {
        given: "과제 등록 요청"
        def request = Mock(AssignmentCreateRequestDto) {
            getTitle() >> "새 과제"
            getContent() >> "과제 설명"
            getCourseId() >> 10L
            getDueDate() >> LocalDateTime.now().plusDays(7)
            getMaxScore() >> BigDecimal.valueOf(100)
            getSubmissionMethod() >> "FILE"
            getLateSubmissionAllowed() >> true
            getLatePenaltyPercent() >> BigDecimal.valueOf(10)
            getMaxFileSizeMb() >> 10
            getAllowedFileTypes() >> "pdf,docx"
            getInstructions() >> "제출 안내"
        }
        def professorId = 100L

        boardCategoryRepository.findByBoardType(BoardType.ASSIGNMENT) >> Optional.of(boardCategory)

        when: "과제를 등록하면"
        assignmentService.createAssignment(request, professorId)

        then: "Post가 저장된다"
        1 * postRepository.save({ Post p ->
            p.getTitle() == "새 과제" &&
                    p.getContent() == "과제 설명" &&
                    p.getCategory() == boardCategory
        }) >> post

        and: "과제가 저장된다"
        1 * assignmentRepository.save({ Assignment a ->
            a.getCourseId() == 10L &&
                    a.getPost() == post
        }) >> assignment
    }

    def "과제 게시판 카테고리가 없으면 예외가 발생한다"() {
        given: "카테고리가 없는 상태"
        def request = Mock(AssignmentCreateRequestDto)
        boardCategoryRepository.findByBoardType(BoardType.ASSIGNMENT) >> Optional.empty()

        when: "과제를 등록하면"
        assignmentService.createAssignment(request, 100L)

        then: "BoardException이 발생한다"
        thrown(BoardException)
    }

    def "과제를 정상적으로 조회한다"() {
        given: "존재하는 과제"
        assignmentRepository.findById(1L) >> Optional.of(assignment)

        when: "과제를 조회하면"
        def result = assignmentService.getAssignment(1L)

        then: "과제 정보가 반환된다"
        result != null
        result.id == 1L
    }

    def "존재하지 않는 과제 조회 시 예외가 발생한다"() {
        given: "존재하지 않는 과제 ID"
        assignmentRepository.findById(999L) >> Optional.empty()

        when: "과제를 조회하면"
        assignmentService.getAssignment(999L)

        then: "BoardException이 발생한다"
        thrown(BoardException)
    }

    def "삭제된 과제 조회 시 예외가 발생한다"() {
        given: "삭제된 과제"
        def deletedAssignment = Mock(Assignment) {
            getIsDeleted() >> true
        }
        assignmentRepository.findById(1L) >> Optional.of(deletedAssignment)

        when: "과제를 조회하면"
        assignmentService.getAssignment(1L)

        then: "BoardException이 발생한다"
        thrown(BoardException)
    }

    def "Post ID로 과제를 조회한다"() {
        given: "Post ID로 과제 조회"
        assignmentRepository.findById(1L) >> Optional.empty()
        assignmentRepository.findByPostId(1L) >> Optional.of(assignment)

        when: "Post ID로 과제를 조회하면"
        def result = assignmentService.getAssignmentByPostId(1L)

        then: "과제 정보가 반환된다"
        result != null
    }

    def "강의별 과제 목록을 조회한다"() {
        given: "강의에 속한 과제들"
        def courseId = 10L
        assignmentRepository.findByCourseId(courseId) >> [assignment]

        when: "강의별 과제 목록을 조회하면"
        def result = assignmentService.getAssignmentsByCourse(courseId)

        then: "과제 목록이 반환된다"
        result.size() == 1
    }

    def "전체 과제 목록을 조회한다"() {
        given: "전체 과제"
        assignmentRepository.findAll() >> [assignment]

        when: "전체 과제를 조회하면"
        def result = assignmentService.getAllAssignments()

        then: "과제 목록이 반환된다"
        result.size() == 1
    }

    def "과제를 정상적으로 수정한다"() {
        given: "수정 요청"
        def request = Mock(AssignmentUpdateRequestDto) {
            getTitle() >> "수정된 제목"
            getContent() >> "수정된 내용"
            getDueDate() >> null
            getMaxScore() >> null
            getSubmissionMethod() >> null
            getLateSubmissionAllowed() >> null
            getLatePenaltyPercent() >> null
            getMaxFileSizeMb() >> null
            getAllowedFileTypes() >> null
            getInstructions() >> null
        }
        def professorId = 100L

        assignmentRepository.findById(1L) >> Optional.of(assignment)

        when: "과제를 수정하면"
        def result = assignmentService.updateAssignment(1L, request, professorId)

        then: "수정된 과제가 반환된다"
        result != null
        1 * post.update(_, _, _)
        1 * assignment.update(_, _, _, _, _, _, _, _, _)
    }

    def "삭제된 과제 수정 시 예외가 발생한다"() {
        given: "삭제된 과제"
        def deletedAssignment = Mock(Assignment) {
            getIsDeleted() >> true
        }
        def request = Mock(AssignmentUpdateRequestDto)
        assignmentRepository.findById(1L) >> Optional.of(deletedAssignment)

        when: "과제를 수정하면"
        assignmentService.updateAssignment(1L, request, 100L)

        then: "BoardException이 발생한다"
        thrown(BoardException)
    }

    def "과제를 정상적으로 삭제한다"() {
        given: "존재하는 과제"
        assignmentRepository.findById(1L) >> Optional.of(assignment)

        when: "과제를 삭제하면"
        assignmentService.deleteAssignment(1L)

        then: "과제와 게시글이 삭제된다"
        1 * assignment.delete()
        1 * post.delete()
    }

    def "신규 과제를 제출한다"() {
        given: "과제 제출 요청"
        def request = Mock(AssignmentSubmissionRequestDto) {
            getContent() >> "제출 내용"
            getAttachmentIds() >> null
        }
        def studentId = 200L
        def submission = createSubmissionMock(1L, false, false)

        assignmentRepository.findById(1L) >> Optional.of(assignment)
        submissionRepository.findByAssignmentIdAndUserId(_, _) >> Optional.empty()

        when: "과제를 제출하면"
        assignmentService.submitAssignment(1L, request, studentId)

        then: "새 제출이 저장된다"
        1 * submissionRepository.save({ AssignmentSubmission s ->
            s.getContent() == "제출 내용" &&
                    s.getUserId() == studentId
        }) >> submission
    }

    def "기존 제출을 수정한다"() {
        given: "이미 제출한 과제"
        def request = Mock(AssignmentSubmissionRequestDto) {
            getContent() >> "수정된 제출 내용"
            getAttachmentIds() >> null
        }
        def studentId = 200L
        def existingSubmission = createSubmissionMock(1L, false, true)

        assignmentRepository.findById(1L) >> Optional.of(assignment)
        submissionRepository.findByAssignmentIdAndUserId(1L, studentId) >> Optional.of(existingSubmission)

        when: "과제를 다시 제출하면"
        def result = assignmentService.submitAssignment(1L, request, studentId)

        then: "기존 제출이 수정된다"
        result != null
        1 * existingSubmission.resubmit(_, _)
    }

    def "채점 완료 후 재제출이 허용되지 않은 경우 예외가 발생한다"() {
        given: "채점 완료된 제출 (재제출 불가)"
        def request = Mock(AssignmentSubmissionRequestDto)
        def studentId = 200L
        def gradedSubmission = createSubmissionMock(1L, true, false)

        assignmentRepository.findById(1L) >> Optional.of(assignment)
        submissionRepository.findByAssignmentIdAndUserId(1L, studentId) >> Optional.of(gradedSubmission)

        when: "재제출을 시도하면"
        assignmentService.submitAssignment(1L, request, studentId)

        then: "BoardException이 발생한다"
        thrown(BoardException)
    }

    def "과제를 채점한다"() {
        given: "채점 요청"
        def request = Mock(AssignmentGradeRequestDto) {
            getScore() >> BigDecimal.valueOf(85)
            getFeedback() >> "잘했습니다"
        }
        def professorId = 100L
        def submission = createSubmissionMock(1L, false, false)

        submissionRepository.findById(1L) >> Optional.of(submission)

        when: "과제를 채점하면"
        def result = assignmentService.gradeSubmission(1L, request, professorId)

        then: "채점이 완료된다"
        result != null
        1 * submission.grade(_, _, professorId)
    }

    def "과제별 제출 목록을 조회한다"() {
        given: "과제에 대한 제출들"
        def submission = createSubmissionMock(1L, false, false)
        submissionRepository.findByAssignmentId(1L) >> [submission]

        when: "제출 목록을 조회하면"
        def result = assignmentService.getSubmissions(1L)

        then: "제출 목록이 반환된다"
        result.size() == 1
    }

    def "내 제출을 조회한다"() {
        given: "내 제출"
        def studentId = 200L
        def submission = createSubmissionMock(1L, false, false)
        submissionRepository.findByAssignmentIdAndUserId(1L, studentId) >> Optional.of(submission)

        when: "내 제출을 조회하면"
        def result = assignmentService.getMySubmission(1L, studentId)

        then: "내 제출이 반환된다"
        result != null
    }

    def "내 제출이 없으면 예외가 발생한다"() {
        given: "제출이 없는 경우"
        def studentId = 200L
        submissionRepository.findByAssignmentIdAndUserId(1L, studentId) >> Optional.empty()

        when: "내 제출을 조회하면"
        assignmentService.getMySubmission(1L, studentId)

        then: "BoardException이 발생한다"
        thrown(BoardException)
    }

    def "채점 대기 목록을 조회한다"() {
        given: "채점 대기 제출들"
        def submission = createSubmissionMock(1L, false, false)
        submissionRepository.findPendingGradingByAssignment(1L) >> [submission]

        when: "채점 대기 목록을 조회하면"
        def result = assignmentService.getPendingGrading(1L)

        then: "채점 대기 목록이 반환된다"
        result.size() == 1
    }

    def "재제출을 허용한다"() {
        given: "재제출 허용 요청"
        def deadline = LocalDateTime.now().plusDays(3)
        def professorId = 100L
        def submission = createSubmissionMock(1L, true, true)

        submissionRepository.findById(1L) >> Optional.of(submission)

        when: "재제출을 허용하면"
        def result = assignmentService.allowResubmission(1L, deadline, professorId)

        then: "재제출이 허용된다"
        result != null
        1 * submission.allowResubmission(deadline)
        1 * submission.updateModifier(professorId)
    }

    def "과제 재제출/수정한다"() {
        given: "재제출 요청"
        def submission = createSubmissionMock(1L, false, false)
        submissionRepository.findById(1L) >> Optional.of(submission)

        when: "재제출하면"
        def result = assignmentService.resubmitAssignment(1L, "수정된 내용")

        then: "재제출이 완료된다"
        result != null
        1 * submission.resubmit("수정된 내용", null)
    }

    def "존재하지 않는 제출 재제출 시 예외가 발생한다"() {
        given: "존재하지 않는 제출"
        submissionRepository.findById(999L) >> Optional.empty()

        when: "재제출하면"
        assignmentService.resubmitAssignment(999L, "수정된 내용")

        then: "BoardException이 발생한다"
        thrown(BoardException)
    }

    def "존재하지 않는 제출 채점 시 예외가 발생한다"() {
        given: "존재하지 않는 제출"
        def request = Mock(AssignmentGradeRequestDto)
        submissionRepository.findById(999L) >> Optional.empty()

        when: "채점하면"
        assignmentService.gradeSubmission(999L, request, 100L)

        then: "BoardException이 발생한다"
        thrown(BoardException)
    }
}
