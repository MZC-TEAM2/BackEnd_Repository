package com.mzc.backend.lms.domains.board.assignment.service;

import com.mzc.backend.lms.domains.board.assignment.dto.request.AssignmentCreateRequestDto;
import com.mzc.backend.lms.domains.board.assignment.dto.request.AssignmentGradeRequestDto;
import com.mzc.backend.lms.domains.board.assignment.dto.request.AssignmentSubmissionRequestDto;
import com.mzc.backend.lms.domains.board.assignment.dto.request.AssignmentUpdateRequestDto;
import com.mzc.backend.lms.domains.board.assignment.dto.response.AssignmentResponseDto;
import com.mzc.backend.lms.domains.board.assignment.dto.response.AssignmentSubmissionResponseDto;
import com.mzc.backend.lms.domains.board.assignment.entity.Assignment;
import com.mzc.backend.lms.domains.board.assignment.entity.AssignmentSubmission;
import com.mzc.backend.lms.domains.board.assignment.repository.AssignmentRepository;
import com.mzc.backend.lms.domains.board.assignment.repository.AssignmentSubmissionRepository;
import com.mzc.backend.lms.domains.board.entity.Attachment;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.enums.AttachmentType;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.enums.PostType;
import com.mzc.backend.lms.domains.board.exception.BoardException;
import com.mzc.backend.lms.domains.board.repository.AttachmentRepository;
import com.mzc.backend.lms.domains.board.repository.BoardCategoryRepository;
import com.mzc.backend.lms.domains.board.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AssignmentService 통합 테스트
 */
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
@DisplayName("AssignmentService 통합 테스트")
class AssignmentServiceTest {
	
	@Autowired
	private AssignmentService assignmentService;
	
	@Autowired
	private AssignmentRepository assignmentRepository;
	
	@Autowired
	private AssignmentSubmissionRepository submissionRepository;
	
	@Autowired
	private PostRepository postRepository;
	
	@Autowired
	private BoardCategoryRepository boardCategoryRepository;
	
	@Autowired
	private AttachmentRepository attachmentRepository;
	
	private BoardCategory assignmentCategory;
	private Post testPost;
	private Assignment testAssignment;
	
	@BeforeEach
	void setUp() {
		// 과제 게시판 카테고리 조회 또는 생성
		assignmentCategory = boardCategoryRepository.findByBoardType(BoardType.ASSIGNMENT)
				.orElseGet(() -> {
					BoardCategory category = new BoardCategory(BoardType.ASSIGNMENT, true, true, false);
					return boardCategoryRepository.save(category);
				});
		
		// 테스트용 게시글 생성
		testPost = Post.builder()
				.category(assignmentCategory)
				.title("데이터구조 과제 #1")
				.content("연결리스트 구현 과제입니다.")
				.postType(PostType.ASSIGNMENT)
				.isAnonymous(false)
				.authorId(1L)
				.build();
		testPost = postRepository.save(testPost);
		
		// 테스트용 과제 생성
		testAssignment = Assignment.builder()
				.post(testPost)
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
		testAssignment = assignmentRepository.save(testAssignment);
	}
	
	@Test
	@DisplayName("과제 등록 성공")
	void createAssignment_Success() {
		// given
		AssignmentCreateRequestDto request = AssignmentCreateRequestDto.builder()
				.title("알고리즘 과제 #1")
				.content("정렬 알고리즘 분석")
				.courseId(1L)
				.dueDate(LocalDateTime.now().plusDays(14))
				.maxScore(new BigDecimal("100.00"))
				.submissionMethod("UPLOAD")
				.lateSubmissionAllowed(false)
				.maxFileSizeMb(20)
				.allowedFileTypes("pdf,docx")
				.instructions("분석 보고서를 제출하세요.")
				.build();
		
		// when
		AssignmentResponseDto response = assignmentService.createAssignment(request, 1L);
		
		// then
		assertThat(response).isNotNull();
		assertThat(response.getId()).isNotNull();
		assertThat(response.getPostId()).isNotNull();
		assertThat(response.getCourseId()).isEqualTo(1L);
		assertThat(response.getMaxScore()).isEqualByComparingTo(new BigDecimal("100.00"));
		assertThat(response.getSubmissionMethod()).isEqualTo("UPLOAD");
		assertThat(response.getLateSubmissionAllowed()).isFalse();
		assertThat(response.getPost().getTitle()).isEqualTo("알고리즘 과제 #1");
	}
	
	@Test
	@DisplayName("과제 등록 실패 - 필수 필드 누락")
	void createAssignment_Fail_MissingRequiredField() {
		// given - title이 null인 경우
		AssignmentCreateRequestDto request = AssignmentCreateRequestDto.builder()
				.content("내용")
				.courseId(1L)
				.dueDate(LocalDateTime.now().plusDays(7))
				.maxScore(new BigDecimal("100.00"))
				.submissionMethod("UPLOAD")
				.build();
		
		// when & then
		assertThatThrownBy(() -> assignmentService.createAssignment(request, 1L))
				.isInstanceOf(Exception.class);
	}
	
	@Test
	@DisplayName("과제 조회 성공")
	void getAssignment_Success() {
		// when
		AssignmentResponseDto response = assignmentService.getAssignment(testAssignment.getId());
		
		// then
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(testAssignment.getId());
		assertThat(response.getCourseId()).isEqualTo(1L);
		assertThat(response.getPost()).isNotNull();
		assertThat(response.getPost().getTitle()).isEqualTo("데이터구조 과제 #1");
	}
	
	@Test
	@DisplayName("게시글 ID로 과제 조회 성공")
	void getAssignmentByPostId_Success() {
		// when
		AssignmentResponseDto response = assignmentService.getAssignmentByPostId(testPost.getId());
		
		// then
		assertThat(response).isNotNull();
		assertThat(response.getPostId()).isEqualTo(testPost.getId());
		assertThat(response.getId()).isEqualTo(testAssignment.getId());
	}
	
	@Test
	@DisplayName("강의별 과제 목록 조회 성공")
	void getAssignmentsByCourse_Success() {
		// given
		Post anotherPost = Post.builder()
				.category(assignmentCategory)
				.title("데이터구조 과제 #2")
				.content("트리 구현 과제")
				.postType(PostType.ASSIGNMENT)
				.isAnonymous(false)
				.authorId(1L)
				.build();
		anotherPost = postRepository.save(anotherPost);
		
		Assignment anotherAssignment = Assignment.builder()
				.post(anotherPost)
				.courseId(1L)
				.dueDate(LocalDateTime.now().plusDays(14))
				.maxScore(new BigDecimal("100.00"))
				.submissionMethod("UPLOAD")
				.lateSubmissionAllowed(false)
				.createdBy(1L)
				.build();
		assignmentRepository.save(anotherAssignment);
		
		// when
		List<AssignmentResponseDto> assignments = assignmentService.getAssignmentsByCourse(1L);
		
		// then
		assertThat(assignments).hasSize(2);
		assertThat(assignments.get(0).getCourseId()).isEqualTo(1L);
	}
	
	@Test
	@DisplayName("과제 수정 성공")
	void updateAssignment_Success() {
		// given
		LocalDateTime newDueDate = LocalDateTime.now().plusDays(10);
		BigDecimal newMaxScore = new BigDecimal("120.00");
		
		AssignmentUpdateRequestDto request = AssignmentUpdateRequestDto.builder()
				.dueDate(newDueDate)
				.maxScore(newMaxScore)
				.submissionMethod("TEXT")
				.lateSubmissionAllowed(false)
				.instructions("수정된 지침")
				.build();
		
		// when
		AssignmentResponseDto response = assignmentService.updateAssignment(testAssignment.getId(), request, 1L);
		
		// then
		assertThat(response).isNotNull();
		assertThat(response.getDueDate()).isEqualTo(newDueDate);
		assertThat(response.getMaxScore()).isEqualByComparingTo(newMaxScore);
		assertThat(response.getSubmissionMethod()).isEqualTo("TEXT");
		assertThat(response.getLateSubmissionAllowed()).isFalse();
		assertThat(response.getInstructions()).isEqualTo("수정된 지침");
	}
	
	@Test
	@DisplayName("과제 삭제 성공")
	void deleteAssignment_Success() {
		// when
		assignmentService.deleteAssignment(testAssignment.getId());
		
		// then
		Assignment deletedAssignment = assignmentRepository.findById(testAssignment.getId()).orElseThrow();
		assertThat(deletedAssignment.getIsDeleted()).isTrue();
	}
	
	@Test
	@DisplayName("과제 제출 성공 - 정상 제출")
	void submitAssignment_Success_OnTime() {
		// given
		AssignmentSubmissionRequestDto request = AssignmentSubmissionRequestDto.builder()
				.content("연결리스트 구현 완료했습니다.")
				.build();
		
		// when
		AssignmentSubmissionResponseDto response = assignmentService.submitAssignment(testAssignment.getId(), request, 10L);
		
		// then
		assertThat(response).isNotNull();
		assertThat(response.getId()).isNotNull();
		assertThat(response.getAssignmentId()).isEqualTo(testAssignment.getId());
		assertThat(response.getUserId()).isEqualTo(10L);
		assertThat(response.getStatus()).isEqualTo("SUBMITTED");
		assertThat(response.getContent()).isEqualTo("연결리스트 구현 완료했습니다.");
		assertThat(response.getAttachments()).isEmpty();
	}
	
	@Test
	@DisplayName("과제 제출 성공 - 지각 제출")
	void submitAssignment_Success_Late() {
		// given - 마감일이 이미 지난 과제 생성
		Post latePost = Post.builder()
				.category(assignmentCategory)
				.title("지난 과제")
				.content("이미 마감된 과제입니다.")
				.postType(PostType.ASSIGNMENT)
				.isAnonymous(false)
				.authorId(1L)
				.build();
		latePost = postRepository.save(latePost);
		
		Assignment lateAssignment = Assignment.builder()
				.post(latePost)
				.courseId(1L)
				.dueDate(LocalDateTime.now().minusDays(1)) // 마감일이 하루 전
				.maxScore(new BigDecimal("100.00"))
				.submissionMethod("UPLOAD")
				.lateSubmissionAllowed(true)
				.createdBy(1L)
				.build();
		lateAssignment = assignmentRepository.save(lateAssignment);
		
		AssignmentSubmissionRequestDto request = AssignmentSubmissionRequestDto.builder()
				.content("늦게 제출합니다.")
				.build();
		
		// when
		AssignmentSubmissionResponseDto response = assignmentService.submitAssignment(lateAssignment.getId(), request, 11L);
		
		// then
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo("LATE");
	}
	
	@Test
	@DisplayName("과제 제출 실패 - 중복 제출")
	void submitAssignment_Fail_AlreadySubmitted() {
		// given
		AssignmentSubmissionRequestDto request1 = AssignmentSubmissionRequestDto.builder()
				.content("첫 번째 제출")
				.build();
		assignmentService.submitAssignment(testAssignment.getId(), request1, 12L);
		
		AssignmentSubmissionRequestDto request2 = AssignmentSubmissionRequestDto.builder()
				.content("두 번째 제출 (중복)")
				.build();
		
		// when & then
		assertThatThrownBy(() -> assignmentService.submitAssignment(testAssignment.getId(), request2, 12L))
				.isInstanceOf(BoardException.class);
	}
	
	@Test
	@DisplayName("과제 재제출 성공 - 채점 전 (수정 모드)")
	void resubmitAssignment_Success_BeforeGrading() {
		// given
		AssignmentSubmissionRequestDto submitRequest = AssignmentSubmissionRequestDto.builder()
				.content("첫 번째 제출")
				.build();
		AssignmentSubmissionResponseDto firstSubmission = assignmentService.submitAssignment(testAssignment.getId(), submitRequest, 13L);
		LocalDateTime originalSubmittedAt = firstSubmission.getSubmittedAt();
		
		String newContent = "수정된 제출 내용입니다.";
		
		// when
		AssignmentSubmissionResponseDto response = assignmentService.resubmitAssignment(
				firstSubmission.getId(), newContent);
		
		// then
		assertThat(response).isNotNull();
		assertThat(response.getContent()).isEqualTo(newContent);
		assertThat(response.getSubmittedAt()).isEqualTo(originalSubmittedAt); // 제출 시간 유지
		assertThat(response.getStatus()).isEqualTo("SUBMITTED"); // 상태 유지
		assertThat(response.getScore()).isNull();
		assertThat(response.getFeedback()).isNull();
	}
	
	@Test
	@DisplayName("과제 재제출 성공 - 채점 후 (재제출 모드)")
	void resubmitAssignment_Success_AfterGrading() {
		// given
		AssignmentSubmissionRequestDto submitRequest = AssignmentSubmissionRequestDto.builder()
				.content("첫 번째 제출")
				.build();
		AssignmentSubmissionResponseDto firstSubmission = assignmentService.submitAssignment(testAssignment.getId(), submitRequest, 15L);
		LocalDateTime originalSubmittedAt = firstSubmission.getSubmittedAt();
		
		// 채점
		AssignmentGradeRequestDto gradeRequest = AssignmentGradeRequestDto.builder()
				.score(new BigDecimal("80.00"))
				.feedback("Good")
				.build();
		assignmentService.gradeSubmission(firstSubmission.getId(), gradeRequest, 1L);
		
		String newContent = "재제출 내용입니다.";
		
		// when
		AssignmentSubmissionResponseDto response = assignmentService.resubmitAssignment(
				firstSubmission.getId(), newContent);
		
		// then
		assertThat(response).isNotNull();
		assertThat(response.getContent()).isEqualTo(newContent);
		assertThat(response.getSubmittedAt()).isNotEqualTo(originalSubmittedAt); // 제출 시간 갱신
		assertThat(response.getStatus()).isEqualTo("SUBMITTED"); // 상태 재계산
		assertThat(response.getScore()).isNull(); // 점수 초기화
		assertThat(response.getFeedback()).isNull(); // 피드백 초기화
		assertThat(response.getGradedAt()).isNull();
		assertThat(response.getGradedBy()).isNull();
	}
	
	@Test
	@DisplayName("과제 채점 성공")
	void gradeSubmission_Success() {
		// given
		AssignmentSubmissionRequestDto submitRequest = AssignmentSubmissionRequestDto.builder()
				.content("제출 완료")
				.build();
		AssignmentSubmissionResponseDto submission = assignmentService.submitAssignment(testAssignment.getId(), submitRequest, 14L);
		
		BigDecimal score = new BigDecimal("95.50");
		String feedback = "잘 작성했습니다.";
		
		AssignmentGradeRequestDto gradeRequest = AssignmentGradeRequestDto.builder()
				.score(score)
				.feedback(feedback)
				.build();
		
		// when
		AssignmentSubmissionResponseDto response = assignmentService.gradeSubmission(
				submission.getId(), gradeRequest, 1L);
		
		// then
		assertThat(response).isNotNull();
		assertThat(response.getScore()).isEqualByComparingTo(score);
		assertThat(response.getFeedback()).isEqualTo(feedback);
		assertThat(response.getStatus()).isEqualTo("GRADED");
		assertThat(response.getGradedBy()).isEqualTo(1L);
		assertThat(response.getGradedAt()).isNotNull();
	}
	
	@Test
	@DisplayName("과제별 제출 목록 조회")
	void getSubmissions_Success() {
		// given
		for (int i = 20; i < 23; i++) {
			AssignmentSubmissionRequestDto request = AssignmentSubmissionRequestDto.builder()
					.content("학생 " + i + " 제출")
					.build();
			assignmentService.submitAssignment(testAssignment.getId(), request, (long) i);
		}
		
		// when
		List<AssignmentSubmissionResponseDto> submissions =
				assignmentService.getSubmissions(testAssignment.getId());
		
		// then
		assertThat(submissions).hasSize(3);
		assertThat(submissions).allMatch(s -> s.getAssignmentId().equals(testAssignment.getId()));
	}
	
	@Test
	@DisplayName("내 제출 조회 성공")
	void getMySubmission_Success() {
		// given
		Long userId = 30L;
		AssignmentSubmissionRequestDto request = AssignmentSubmissionRequestDto.builder()
				.content("내 제출입니다.")
				.build();
		assignmentService.submitAssignment(testAssignment.getId(), request, userId);
		
		// when
		AssignmentSubmissionResponseDto response =
				assignmentService.getMySubmission(testAssignment.getId(), userId);
		
		// then
		assertThat(response).isNotNull();
		assertThat(response.getUserId()).isEqualTo(userId);
		assertThat(response.getAssignmentId()).isEqualTo(testAssignment.getId());
	}
	
	@Test
	@DisplayName("채점 대기 목록 조회")
	void getPendingGrading_Success() {
		// given
		// 3개 제출
		for (int i = 40; i < 43; i++) {
			AssignmentSubmissionRequestDto request = AssignmentSubmissionRequestDto.builder()
					.content("학생 " + i + " 제출")
					.build();
			assignmentService.submitAssignment(testAssignment.getId(), request, (long) i);
		}
		
		// 1개만 채점
		List<AssignmentSubmission> allSubmissions = submissionRepository.findByAssignmentId(testAssignment.getId());
		AssignmentSubmission firstSubmission = allSubmissions.get(0);
		
		AssignmentGradeRequestDto gradeRequest = AssignmentGradeRequestDto.builder()
				.score(new BigDecimal("90.00"))
				.feedback("Good")
				.build();
		assignmentService.gradeSubmission(firstSubmission.getId(), gradeRequest, 1L);
		
		// when
		List<AssignmentSubmissionResponseDto> pending =
				assignmentService.getPendingGrading(testAssignment.getId());
		
		// then
		assertThat(pending).hasSize(2); // 3개 중 1개 채점되어 2개 대기
		assertThat(pending).allMatch(s ->
				s.getStatus().equals("SUBMITTED") || s.getStatus().equals("LATE"));
	}
	
	@Test
	@DisplayName("과제 제출 성공 - 첨부파일 포함")
	void submitAssignment_WithAttachments_Success() {
		// given
		Attachment attachment1 = Attachment.builder()
				.originalName("homework.pdf")
				.storedName("20250101_homework.pdf")
				.filePath("/uploads/2025/20250101_homework.pdf")
				.fileSize(1024L)
				.attachmentType(AttachmentType.DOCUMENT)
				.build();
		Attachment attachment2 = Attachment.builder()
				.originalName("code.zip")
				.storedName("20250101_code.zip")
				.filePath("/uploads/2025/20250101_code.zip")
				.fileSize(2048L)
				.attachmentType(AttachmentType.ARCHIVE)
				.build();
		attachmentRepository.save(attachment1);
		attachmentRepository.save(attachment2);
		
		AssignmentSubmissionRequestDto request = AssignmentSubmissionRequestDto.builder()
				.content("첨부파일과 함께 제출합니다.")
				.attachmentIds(List.of(attachment1.getId(), attachment2.getId()))
				.build();
		
		// when
		AssignmentSubmissionResponseDto response = assignmentService.submitAssignment(
				testAssignment.getId(), request, 50L);
		
		// then
		assertThat(response).isNotNull();
		assertThat(response.getAttachments()).hasSize(2);
		assertThat(response.getAttachments())
				.extracting("originalName")
				.containsExactlyInAnyOrder("homework.pdf", "code.zip");
	}
	
	@Test
	@DisplayName("과제 제출 조회 - 첨부파일 포함")
	void getSubmission_WithAttachments_Success() {
		// given
		Attachment attachment = Attachment.builder()
				.originalName("report.pdf")
				.storedName("20250102_report.pdf")
				.filePath("/uploads/2025/20250102_report.pdf")
				.fileSize(3072L)
				.attachmentType(AttachmentType.DOCUMENT)
				.build();
		attachmentRepository.save(attachment);
		
		AssignmentSubmissionRequestDto submitRequest = AssignmentSubmissionRequestDto.builder()
				.content("리포트 제출")
				.attachmentIds(List.of(attachment.getId()))
				.build();
		AssignmentSubmissionResponseDto submission = assignmentService.submitAssignment(
				testAssignment.getId(), submitRequest, 60L);
		
		// when
		AssignmentSubmissionResponseDto response = assignmentService.getMySubmission(
				testAssignment.getId(), 60L);
		
		// then
		assertThat(response).isNotNull();
		assertThat(response.getAttachments()).hasSize(1);
		assertThat(response.getAttachments().get(0).getOriginalName()).isEqualTo("report.pdf");
	}
}
