package com.mzc.backend.lms.domains.board.assignment.repository;

import com.mzc.backend.lms.common.config.JpaConfig;
import com.mzc.backend.lms.domains.board.assignment.entity.Assignment;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.enums.PostType;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AssignmentRepository 테스트
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(JpaConfig.class)
class AssignmentRepositoryTest {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BoardCategoryRepository boardCategoryRepository;

    private BoardCategory assignmentCategory;
    private Post post1;
    private Post post2;
    private Assignment assignment1;
    private Assignment assignment2;

    @BeforeEach
    void setUp() {
        // 게시판 카테고리 조회 (DB에 이미 존재)
        assignmentCategory = boardCategoryRepository.findByBoardType(BoardType.ASSIGNMENT)
                .orElseGet(() -> {
                    BoardCategory category = new BoardCategory(BoardType.ASSIGNMENT, true, true, false);
                    return boardCategoryRepository.save(category);
                });

        // 게시글 생성
        post1 = Post.builder()
                .category(assignmentCategory)
                .title("데이터구조 과제 #1")
                .content("연결리스트 구현 과제입니다.")
                .postType(PostType.ASSIGNMENT)
                .isAnonymous(false)
                .authorId(1L)
                .build();
        postRepository.save(post1);

        post2 = Post.builder()
                .category(assignmentCategory)
                .title("알고리즘 과제 #1")
                .content("정렬 알고리즘 분석 과제입니다.")
                .postType(PostType.ASSIGNMENT)
                .isAnonymous(false)
                .authorId(1L)
                .build();
        postRepository.save(post2);

        // 과제 생성
        assignment1 = Assignment.builder()
                .post(post1)
                .courseId(1L)
                .dueDate(LocalDateTime.now().plusDays(7))
                .maxScore(new BigDecimal("100.00"))
                .submissionMethod("UPLOAD")
                .lateSubmissionAllowed(true)
                .latePenaltyPercent(new BigDecimal("5.00"))
                .maxFileSizeMb(10)
                .allowedFileTypes("java,cpp,py")
                .instructions("소스코드와 보고서를 제출하세요.")
                .createdBy(1L)
                .build();
        assignmentRepository.save(assignment1);

        assignment2 = Assignment.builder()
                .post(post2)
                .courseId(1L)
                .dueDate(LocalDateTime.now().plusDays(14))
                .maxScore(new BigDecimal("100.00"))
                .submissionMethod("UPLOAD")
                .lateSubmissionAllowed(false)
                .maxFileSizeMb(20)
                .allowedFileTypes("pdf,docx")
                .instructions("분석 보고서를 제출하세요.")
                .createdBy(1L)
                .build();
        assignmentRepository.save(assignment2);
    }

    @Test
    @DisplayName("과제 저장 테스트")
    void save() {
        // given
        Post newPost = Post.builder()
                .category(assignmentCategory)
                .title("웹개발 과제")
                .content("React 포트폴리오 제작")
                .postType(PostType.ASSIGNMENT)
                .isAnonymous(false)
                .authorId(1L)
                .build();
        postRepository.save(newPost);

        Assignment newAssignment = Assignment.builder()
                .post(newPost)
                .courseId(2L)
                .dueDate(LocalDateTime.now().plusDays(30))
                .maxScore(new BigDecimal("100.00"))
                .submissionMethod("LINK")
                .lateSubmissionAllowed(true)
                .createdBy(1L)
                .build();

        // when
        Assignment savedAssignment = assignmentRepository.save(newAssignment);

        // then
        assertThat(savedAssignment.getId()).isNotNull();
        assertThat(savedAssignment.getPost().getTitle()).isEqualTo("웹개발 과제");
        assertThat(savedAssignment.getCourseId()).isEqualTo(2L);
        assertThat(savedAssignment.getSubmissionMethod()).isEqualTo("LINK");
    }

    @Test
    @DisplayName("게시글 ID로 과제 조회")
    void findByPostId() {
        // when
        Optional<Assignment> found = assignmentRepository.findByPostId(post1.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getPost().getTitle()).isEqualTo("데이터구조 과제 #1");
        assertThat(found.get().getCourseId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("강의 ID로 과제 목록 조회")
    void findByCourseId() {
        // when
        List<Assignment> assignments = assignmentRepository.findByCourseId(1L);

        // then
        assertThat(assignments).hasSize(2);
        assertThat(assignments.get(0).getDueDate()).isAfter(assignments.get(1).getDueDate());
    }

    @Test
    @DisplayName("마감일 임박 과제 목록 조회")
    void findUpcomingAssignments() {
        // given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(10);

        // when
        List<Assignment> upcomingAssignments = assignmentRepository
                .findUpcomingAssignments(startDate, endDate);

        // then
        assertThat(upcomingAssignments).hasSize(1);
        assertThat(upcomingAssignments.get(0).getPost().getTitle())
                .isEqualTo("데이터구조 과제 #1");
    }

    @Test
    @DisplayName("마감일 지난 과제 목록 조회")
    void findOverdueAssignments() {
        // given
        Post overduePost = Post.builder()
                .category(assignmentCategory)
                .title("과거 과제")
                .content("이미 마감된 과제")
                .postType(PostType.ASSIGNMENT)
                .isAnonymous(false)
                .authorId(1L)
                .build();
        postRepository.save(overduePost);

        Assignment overdueAssignment = Assignment.builder()
                .post(overduePost)
                .courseId(1L)
                .dueDate(LocalDateTime.now().minusDays(1))
                .maxScore(new BigDecimal("100.00"))
                .submissionMethod("UPLOAD")
                .lateSubmissionAllowed(false)
                .createdBy(1L)
                .build();
        assignmentRepository.save(overdueAssignment);

        // when
        List<Assignment> overdueAssignments = assignmentRepository
                .findOverdueAssignments(LocalDateTime.now());

        // then
        assertThat(overdueAssignments).hasSize(1);
        assertThat(overdueAssignments.get(0).getPost().getTitle())
                .isEqualTo("과거 과제");
    }

    @Test
    @DisplayName("강의별 과제 수 조회")
    void countByCourseId() {
        // when
        Long count = assignmentRepository.countByCourseId(1L);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("생성자 ID로 과제 목록 조회")
    void findByCreatorId() {
        // when
        List<Assignment> assignments = assignmentRepository.findByCreatorId(1L);

        // then
        assertThat(assignments).hasSize(2);
        assertThat(assignments.get(0).getCreatedAt()).isAfter(assignments.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("과제 정보 수정")
    void update() {
        // given
        Assignment assignment = assignmentRepository.findById(assignment1.getId()).orElseThrow();
        LocalDateTime newDueDate = LocalDateTime.now().plusDays(10);
        BigDecimal newMaxScore = new BigDecimal("150.00");

        // when
        assignment.update(
                newDueDate,
                newMaxScore,
                "TEXT",
                false,
                null,
                15,
                "txt,md",
                "수정된 지침",
                1L
        );
        Assignment updatedAssignment = assignmentRepository.save(assignment);

        // then
        assertThat(updatedAssignment.getDueDate()).isEqualTo(newDueDate);
        assertThat(updatedAssignment.getMaxScore()).isEqualTo(newMaxScore);
        assertThat(updatedAssignment.getSubmissionMethod()).isEqualTo("TEXT");
        assertThat(updatedAssignment.getLateSubmissionAllowed()).isFalse();
    }

    @Test
    @DisplayName("과제 마감 여부 확인")
    void isOverdue() {
        // given
        Post pastPost = Post.builder()
                .category(assignmentCategory)
                .title("과거 과제")
                .content("마감된 과제")
                .postType(PostType.ASSIGNMENT)
                .isAnonymous(false)
                .authorId(1L)
                .build();
        postRepository.save(pastPost);

        Assignment pastAssignment = Assignment.builder()
                .post(pastPost)
                .courseId(1L)
                .dueDate(LocalDateTime.now().minusHours(1))
                .maxScore(new BigDecimal("100.00"))
                .submissionMethod("UPLOAD")
                .lateSubmissionAllowed(false)
                .createdBy(1L)
                .build();
        assignmentRepository.save(pastAssignment);

        // when & then
        assertThat(assignment1.isOverdue()).isFalse();
        assertThat(pastAssignment.isOverdue()).isTrue();
    }

    @Test
    @DisplayName("과제 Soft Delete 테스트")
    void delete() {
        // given
        Long assignmentId = assignment1.getId();

        // when
        assignment1.delete();
        assignmentRepository.save(assignment1);

        // then
        Optional<Assignment> found = assignmentRepository.findByPostId(post1.getId());
        assertThat(found).isEmpty(); // isDeleted = true이므로 조회되지 않음

        // 실제 DB에는 존재
        Optional<Assignment> foundById = assignmentRepository.findById(assignmentId);
        assertThat(foundById).isPresent();
        assertThat(foundById.get().getIsDeleted()).isTrue();
    }
}
