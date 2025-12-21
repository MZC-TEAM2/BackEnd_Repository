package com.mzc.backend.lms.domains.board.assignment.repository;

import com.mzc.backend.lms.common.config.JpaConfig;
import com.mzc.backend.lms.domains.board.assignment.entity.Assignment;
import com.mzc.backend.lms.domains.board.assignment.entity.AssignmentSubmission;
import com.mzc.backend.lms.domains.board.entity.Attachment;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.enums.AttachmentType;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.enums.PostType;
import com.mzc.backend.lms.domains.board.repository.AttachmentRepository;
import com.mzc.backend.lms.domains.board.repository.BoardCategoryRepository;
import com.mzc.backend.lms.domains.board.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AssignmentSubmissionRepository 테스트
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(JpaConfig.class)
class AssignmentSubmissionRepositoryTest {

    @Autowired
    private AssignmentSubmissionRepository submissionRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BoardCategoryRepository boardCategoryRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    private Assignment assignment;
    private AssignmentSubmission submission1;
    private AssignmentSubmission submission2;
    private Attachment attachment1;
    private Attachment attachment2;

    @BeforeEach
    void setUp() {
        // 게시판 카테고리 조회 (DB에 이미 존재)
        BoardCategory category = boardCategoryRepository.findByBoardType(BoardType.ASSIGNMENT)
                .orElseGet(() -> {
                    BoardCategory newCategory = new BoardCategory(BoardType.ASSIGNMENT, true, true, false);
                    return boardCategoryRepository.save(newCategory);
                });

        // 게시글 생성
        Post post = Post.builder()
                .category(category)
                .title("데이터구조 과제 #1")
                .content("연결리스트 구현 과제")
                .postType(PostType.ASSIGNMENT)
                .isAnonymous(false)
                .authorId(1L)
                .build();
        postRepository.save(post);

        // 과제 생성
        assignment = Assignment.builder()
                .post(post)
                .courseId(1L)
                .dueDate(LocalDateTime.now().plusDays(7))
                .maxScore(new BigDecimal("100.00"))
                .submissionMethod("UPLOAD")
                .lateSubmissionAllowed(true)
                .createdBy(1L)
                .build();
        assignmentRepository.save(assignment);

        // 첨부파일 생성
        attachment1 = Attachment.builder()
                .post(post)
                .originalName("assignment1.pdf")
                .storedName("uuid1.pdf")
                .filePath("/uploads/assignment1.pdf")
                .fileSize(1024L)
                .attachmentType(AttachmentType.DOCUMENT)
                .build();
        attachmentRepository.save(attachment1);

        attachment2 = Attachment.builder()
                .post(post)
                .originalName("assignment2.zip")
                .storedName("uuid2.zip")
                .filePath("/uploads/assignment2.zip")
                .fileSize(2048L)
                .attachmentType(AttachmentType.OTHER)
                .build();
        attachmentRepository.save(attachment2);

        // 과제 제출 생성
        submission1 = AssignmentSubmission.builder()
                .assignment(assignment)
                .userId(10L)
                .content("연결리스트 구현 소스코드입니다.")
                .submittedAt(LocalDateTime.now())
                .status("SUBMITTED")
                .createdBy(10L)
                .build();
        submissionRepository.save(submission1);
        submission1.addAttachments(List.of(attachment1));
        submissionRepository.save(submission1);

        submission2 = AssignmentSubmission.builder()
                .assignment(assignment)
                .userId(11L)
                .content("지각 제출합니다.")
                .submittedAt(LocalDateTime.now().plusDays(8))
                .status("LATE")
                .createdBy(11L)
                .build();
        submissionRepository.save(submission2);
    }

    @Test
    @DisplayName("과제 제출 저장 테스트")
    void save() {
        // given
        AssignmentSubmission newSubmission = AssignmentSubmission.builder()
                .assignment(assignment)
                .userId(12L)
                .content("새로운 제출입니다.")
                .submittedAt(LocalDateTime.now())
                .status("SUBMITTED")
                .createdBy(12L)
                .build();

        // when
        AssignmentSubmission savedSubmission = submissionRepository.save(newSubmission);

        // then
        assertThat(savedSubmission.getId()).isNotNull();
        assertThat(savedSubmission.getUserId()).isEqualTo(12L);
        assertThat(savedSubmission.getStatus()).isEqualTo("SUBMITTED");
    }

    @Test
    @DisplayName("과제 ID와 학생 ID로 제출 조회")
    void findByAssignmentIdAndUserId() {
        // when
        Optional<AssignmentSubmission> found = submissionRepository
                .findByAssignmentIdAndUserId(assignment.getId(), 10L);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(10L);
        assertThat(found.get().getStatus()).isEqualTo("SUBMITTED");
    }

    @Test
    @DisplayName("과제 ID로 전체 제출 목록 조회")
    void findByAssignmentId() {
        // when
        List<AssignmentSubmission> submissions = submissionRepository
                .findByAssignmentId(assignment.getId());

        // then
        assertThat(submissions).hasSize(2);
        assertThat(submissions.get(0).getSubmittedAt())
                .isAfter(submissions.get(1).getSubmittedAt());
    }

    @Test
    @DisplayName("학생 ID로 제출 목록 조회")
    void findByUserId() {
        // when
        List<AssignmentSubmission> submissions = submissionRepository.findByUserId(10L);

        // then
        assertThat(submissions).hasSize(1);
        assertThat(submissions.get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("과제별 제출 수 조회")
    void countByAssignmentId() {
        // when
        Long count = submissionRepository.countByAssignmentId(assignment.getId());

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("채점 대기 중인 제출 목록 조회")
    void findPendingGrading() {
        // when
        List<AssignmentSubmission> pending = submissionRepository.findPendingGrading();

        // then
        assertThat(pending).hasSize(2);
        assertThat(pending).allMatch(s -> 
                s.getStatus().equals("SUBMITTED") || s.getStatus().equals("LATE"));
    }

    @Test
    @DisplayName("과제별 채점 대기 목록 조회")
    void findPendingGradingByAssignment() {
        // when
        List<AssignmentSubmission> pending = submissionRepository
                .findPendingGradingByAssignment(assignment.getId());

        // then
        assertThat(pending).hasSize(2);
    }

    @Test
    @DisplayName("과제 채점")
    void grade() {
        // given
        BigDecimal score = new BigDecimal("95.50");
        String feedback = "잘 작성했습니다. 코드 주석을 더 추가하면 좋겠습니다.";

        // when
        submission1.grade(score, feedback, 1L);
        AssignmentSubmission gradedSubmission = submissionRepository.save(submission1);

        // then
        assertThat(gradedSubmission.getScore()).isEqualTo(score);
        assertThat(gradedSubmission.getFeedback()).isEqualTo(feedback);
        assertThat(gradedSubmission.getStatus()).isEqualTo("GRADED");
        assertThat(gradedSubmission.getGradedBy()).isEqualTo(1L);
        assertThat(gradedSubmission.getGradedAt()).isNotNull();
    }

    @Test
    @DisplayName("채점 완료된 제출 목록 조회")
    void findGraded() {
        // given
        submission1.grade(new BigDecimal("90.00"), "Good!", 1L);
        submissionRepository.save(submission1);

        // when
        List<AssignmentSubmission> graded = submissionRepository.findGraded();

        // then
        assertThat(graded).hasSize(1);
        assertThat(graded.get(0).getStatus()).isEqualTo("GRADED");
    }

    @Test
    @DisplayName("지각 제출 목록 조회")
    void findLateSubmissions() {
        // when
        List<AssignmentSubmission> lateSubmissions = submissionRepository.findLateSubmissions();

        // then
        assertThat(lateSubmissions).hasSize(1);
        assertThat(lateSubmissions.get(0).getStatus()).isEqualTo("LATE");
        assertThat(lateSubmissions.get(0).getUserId()).isEqualTo(11L);
    }

    @Test
    @DisplayName("과제별 평균 점수 조회")
    void getAverageScoreByAssignment() {
        // given
        submission1.grade(new BigDecimal("90.00"), "Good!", 1L);
        submission2.grade(new BigDecimal("80.00"), "OK", 1L);
        submissionRepository.saveAll(List.of(submission1, submission2));

        // when
        Double averageScore = submissionRepository.getAverageScoreByAssignment(assignment.getId());

        // then
        assertThat(averageScore).isEqualTo(85.00);
    }

    @Test
    @DisplayName("학생별 과제 제출 여부 확인")
    void existsByAssignmentIdAndUserId() {
        // when
        boolean exists = submissionRepository.existsByAssignmentIdAndUserId(assignment.getId(), 10L);
        boolean notExists = submissionRepository.existsByAssignmentIdAndUserId(assignment.getId(), 99L);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("과제 재제출 - 채점 전 (수정 모드)")
    void resubmit_beforeGrading() {
        // given
        String newContent = "수정된 내용입니다.";
        LocalDateTime originalSubmittedAt = submission1.getSubmittedAt();

        // when
        submission1.resubmit(newContent, null);
        AssignmentSubmission resubmitted = submissionRepository.save(submission1);

        // then
        assertThat(resubmitted.getContent()).isEqualTo(newContent);
        assertThat(resubmitted.getSubmittedAt()).isEqualTo(originalSubmittedAt); // 제출 시간 유지
        assertThat(resubmitted.getStatus()).isEqualTo("SUBMITTED"); // 상태 유지
        assertThat(resubmitted.getScore()).isNull();
        assertThat(resubmitted.getFeedback()).isNull();
    }

    @Test
    @DisplayName("과제 재제출 - 채점 후 (재제출 모드)")
    void resubmit_afterGrading() {
        // given
        submission1.grade(new BigDecimal("80.00"), "Good", 1L);
        submissionRepository.save(submission1);
        
        String newContent = "재제출 내용입니다.";
        LocalDateTime originalSubmittedAt = submission1.getSubmittedAt();

        // when
        submission1.resubmit(newContent, null);
        AssignmentSubmission resubmitted = submissionRepository.save(submission1);

        // then
        assertThat(resubmitted.getContent()).isEqualTo(newContent);
        assertThat(resubmitted.getSubmittedAt()).isNotEqualTo(originalSubmittedAt); // 제출 시간 갱신
        assertThat(resubmitted.getStatus()).isEqualTo("SUBMITTED"); // 상태 재계산
        assertThat(resubmitted.getScore()).isNull(); // 점수 초기화
        assertThat(resubmitted.getFeedback()).isNull(); // 피드백 초기화
        assertThat(resubmitted.getGradedAt()).isNull();
        assertThat(resubmitted.getGradedBy()).isNull();
    }

    @Test
    @DisplayName("첨부파일 추가 테스트")
    void addAttachments() {
        // given
        List<Attachment> newAttachments = List.of(attachment2);

        // when
        submission1.addAttachments(newAttachments);
        AssignmentSubmission updated = submissionRepository.save(submission1);

        // then
        assertThat(updated.getAttachments()).hasSize(2);
        assertThat(updated.getAttachments()).contains(attachment1, attachment2);
    }

    @Test
    @DisplayName("재제출 시 첨부파일 교체 테스트")
    void resubmit_withAttachments() {
        // given
        String newContent = "첨부파일을 교체합니다.";
        List<Attachment> newAttachments = List.of(attachment2);

        // when
        submission1.resubmit(newContent, newAttachments);
        AssignmentSubmission resubmitted = submissionRepository.save(submission1);

        // then
        assertThat(resubmitted.getAttachments()).hasSize(1);
        assertThat(resubmitted.getAttachments()).containsOnly(attachment2);
        assertThat(resubmitted.getAttachments()).doesNotContain(attachment1);
    }

    @Test
    @DisplayName("첨부파일 포함 제출 조회 테스트")
    void findSubmissionWithAttachments() {
        // when
        Optional<AssignmentSubmission> found = submissionRepository
                .findByAssignmentIdAndUserId(assignment.getId(), 10L);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getAttachments()).hasSize(1);
        assertThat(found.get().getAttachments().get(0).getOriginalName()).isEqualTo("assignment1.pdf");
    }

    @Test
    @DisplayName("지각 제출 여부 확인")
    void isLateSubmission() {
        // when & then
        assertThat(submission1.isLateSubmission()).isFalse();
        assertThat(submission2.isLateSubmission()).isTrue();
    }

    @Test
    @DisplayName("채점 완료 여부 확인")
    void isGraded() {
        // given
        submission1.grade(new BigDecimal("95.00"), "Excellent!", 1L);
        submissionRepository.save(submission1);

        // when & then
        assertThat(submission1.isGraded()).isTrue();
        assertThat(submission2.isGraded()).isFalse();
    }

    @Test
    @DisplayName("제출 Soft Delete 테스트")
    void delete() {
        // given
        Long submissionId = submission1.getId();

        // when
        submission1.delete();
        submissionRepository.save(submission1);

        // then
        Optional<AssignmentSubmission> found = submissionRepository
                .findByAssignmentIdAndUserId(assignment.getId(), 10L);
        assertThat(found).isEmpty();

        // 실제 DB에는 존재
        Optional<AssignmentSubmission> foundById = submissionRepository.findById(submissionId);
        assertThat(foundById).isPresent();
        assertThat(foundById.get().getIsDeleted()).isTrue();
    }
}
