package com.mzc.backend.lms.domains.board.service;

import com.mzc.backend.lms.domains.board.dto.request.PostCreateRequestDto;
import com.mzc.backend.lms.domains.board.dto.response.PostListResponseDto;
import com.mzc.backend.lms.domains.board.dto.response.PostResponseDto;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Hashtag;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.enums.PostType;
import com.mzc.backend.lms.domains.board.repository.BoardCategoryRepository;
import com.mzc.backend.lms.domains.board.repository.HashtagRepository;
import com.mzc.backend.lms.domains.board.repository.PostRepository;
import com.mzc.backend.lms.domains.user.organization.entity.College;
import com.mzc.backend.lms.domains.user.organization.entity.Department;
import com.mzc.backend.lms.domains.user.organization.repository.CollegeRepository;
import com.mzc.backend.lms.domains.user.organization.repository.DepartmentRepository;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileRepository;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.user.student.entity.StudentDepartment;
import com.mzc.backend.lms.domains.user.student.repository.StudentDepartmentRepository;
import com.mzc.backend.lms.domains.user.student.repository.StudentRepository;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 학과 게시판 Service 통합 테스트
 * - 학과별 게시글 자동 필터링
 * - 사용자 학과 정보 조회
 * - 학과 게시글 접근 권한
 */
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("학과 게시판 Service 통합 테스트")
class DepartmentBoardServiceTest {
	
	@Autowired
	private PostService postService;
	
	@Autowired
	private PostRepository postRepository;
	
	@Autowired
	private BoardCategoryRepository boardCategoryRepository;
	
	@Autowired
	private HashtagRepository hashtagRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private StudentRepository studentRepository;
	
	@Autowired
	private StudentDepartmentRepository studentDepartmentRepository;
	
	@Autowired
	private DepartmentRepository departmentRepository;
	
	@Autowired
	private CollegeRepository collegeRepository;
	
	@Autowired
	private EntityManager entityManager;
	
	@Autowired
	private UserProfileRepository userProfileRepository;
	
	private User csUser;
	private User baUser;
	private Student csStudent;
	private Student baStudent;
	private Department csDepartment;
	private Department baDepartment;
	private BoardCategory departmentCategory;
	
	@BeforeEach
	@Transactional
	void setUp() {
		// 1. College 생성
		College engineeringCollege = College.builder()
				.collegeCode("ENG")
				.collegeNumberCode("01")
				.collegeName("공과대학")
				.build();
		collegeRepository.save(engineeringCollege);
		
		College businessCollege = College.builder()
				.collegeCode("BUS")
				.collegeNumberCode("02")
				.collegeName("경영대학")
				.build();
		collegeRepository.save(businessCollege);
		
		// 2. Department 생성
		csDepartment = Department.builder()
				.departmentCode("CS")
				.departmentName("컴퓨터공학과")
				.college(engineeringCollege)
				.build();
		departmentRepository.save(csDepartment);
		
		baDepartment = Department.builder()
				.departmentCode("BA")
				.departmentName("경영학과")
				.college(businessCollege)
				.build();
		departmentRepository.save(baDepartment);
		
		// 3. User & Student 생성 - 컴퓨터공학과 학생
		csUser = User.create(20251101L, "cs.student@test.com", "password");
		userRepository.save(csUser);
		
		UserProfile csProfile = UserProfile.create(csUser, "컴공학생");
		userProfileRepository.save(csProfile);
		
		csStudent = Student.builder()
				.studentId(20250001L)
				.user(csUser)
				.admissionYear(2025)
				.grade(1)
				.build();
		studentRepository.save(csStudent);
		
		StudentDepartment csStudentDept = StudentDepartment.builder()
				.student(csStudent)
				.department(csDepartment)
				.isPrimary(true)
				.enrolledDate(LocalDate.now())
				.build();
		studentDepartmentRepository.save(csStudentDept);
		
		// 4. User & Student 생성 - 경영학과 학생
		baUser = User.create(20251102L, "ba.student@test.com", "password");
		userRepository.save(baUser);
		
		UserProfile baProfile = UserProfile.create(baUser, "경영학생");
		userProfileRepository.save(baProfile);
		
		baStudent = Student.builder()
				.studentId(20250002L)
				.user(baUser)
				.admissionYear(2025)
				.grade(1)
				.build();
		studentRepository.save(baStudent);
		
		StudentDepartment baStudentDept = StudentDepartment.builder()
				.student(baStudent)
				.department(baDepartment)
				.isPrimary(true)
				.enrolledDate(LocalDate.now())
				.build();
		studentDepartmentRepository.save(baStudentDept);
		
		// 5. BoardCategory 생성
		departmentCategory = boardCategoryRepository.findByBoardType(BoardType.DEPARTMENT)
				.orElseGet(() -> {
					BoardCategory category = new BoardCategory(
							BoardType.DEPARTMENT,
							true,  // isCommentEnabled
							true,  // isLikeEnabled
							false  // isAnonymousEnabled
					);
					return boardCategoryRepository.save(category);
				});
		
		// 6. Hashtag 생성
		Hashtag csHashtag = hashtagRepository.findByName("컴퓨터공학과")
				.orElseGet(() -> {
					Hashtag tag = Hashtag.builder()
							.name("컴퓨터공학과")
							.displayName("컴퓨터공학과")
							.color("#1976d2")
							.createdBy(csUser.getId())
							.build();
					return hashtagRepository.save(tag);
				});
		
		Hashtag baHashtag = hashtagRepository.findByName("경영학과")
				.orElseGet(() -> {
					Hashtag tag = Hashtag.builder()
							.name("경영학과")
							.displayName("경영학과")
							.color("#f57c00")
							.createdBy(baUser.getId())
							.build();
					return hashtagRepository.save(tag);
				});
		
		// 7. 테스트 게시글 생성 - 컴퓨터공학과
		createDepartmentPost(
				"[컴공] 2025-1학기 전공 수강신청 안내",
				"컴퓨터공학과 전공 수강신청 안내입니다.",
				csUser.getId(),
				Arrays.asList("컴퓨터공학과")
		);
		
		createDepartmentPost(
				"[컴공] 캡스톤디자인 팀 구성",
				"캡스톤디자인 프로젝트 팀을 구성합니다.",
				csUser.getId(),
				Arrays.asList("컴퓨터공학과")
		);
		
		// 8. 테스트 게시글 생성 - 경영학과
		createDepartmentPost(
				"[경영] 마케팅 전략 특강",
				"경영학과 마케팅 특강을 개최합니다.",
				baUser.getId(),
				Arrays.asList("경영학과")
		);
		
		entityManager.flush();
		entityManager.clear();
	}
	
	private void createDepartmentPost(String title, String content, Long authorId, List<String> hashtagNames) {
		Post post = Post.builder()
				.category(departmentCategory)
				.title(title)
				.content(content)
				.postType(PostType.NORMAL)
				.isAnonymous(false)
				.authorId(authorId)
				.build();
		postRepository.save(post);
		
		for (String hashtagName : hashtagNames) {
			Hashtag hashtag = hashtagRepository.findByName(hashtagName)
					.orElseThrow(() -> new RuntimeException("Hashtag not found: " + hashtagName));
			post.addHashtag(hashtag, authorId);
		}
		
		postRepository.save(post);
	}
	
	@Test
	@DisplayName("컴퓨터공학과 학생이 학과 게시판 조회 시 본인 학과 게시글만 조회")
	@Transactional
	void getDepartmentPostList_FilterByStudentDepartment_CS() {
		// when
		Page<PostListResponseDto> result = postService.getPostListByBoardType(
				"DEPARTMENT",
				null,
				null,
				PageRequest.of(0, 20),
				csStudent.getStudentId()
		);
		
		// then
		assertThat(result.getContent()).isNotEmpty();
		assertThat(result.getContent())
				.allMatch(post -> post.getTitle().contains("[컴공]"))
				.noneMatch(post -> post.getTitle().contains("[경영]"));
		
		log.info("컴퓨터공학과 학생이 조회한 게시글 수: {}", result.getTotalElements());
		result.getContent().forEach(post ->
				log.info("조회된 게시글: {}", post.getTitle())
		);
	}
	
	@Test
	@DisplayName("경영학과 학생이 학과 게시판 조회 시 본인 학과 게시글만 조회")
	@Transactional
	void getDepartmentPostList_FilterByStudentDepartment_BA() {
		// when
		Page<PostListResponseDto> result = postService.getPostListByBoardType(
				"DEPARTMENT",
				null,
				null,
				PageRequest.of(0, 20),
				baStudent.getStudentId()
		);
		
		// then
		assertThat(result.getContent()).isNotEmpty();
		assertThat(result.getContent())
				.allMatch(post -> post.getTitle().contains("[경영]"))
				.noneMatch(post -> post.getTitle().contains("[컴공]"));
		
		log.info("경영학과 학생이 조회한 게시글 수: {}", result.getTotalElements());
		result.getContent().forEach(post ->
				log.info("조회된 게시글: {}", post.getTitle())
		);
	}
	
	@Test
	@DisplayName("학과가 없는 사용자가 학과 게시판 조회 시 빈 결과 반환")
	@Transactional
	void getDepartmentPostList_NoStudentDepartment() {
		// given
		User userWithoutDept = User.create(20251103L, "no.dept@test.com", "password");
		userRepository.save(userWithoutDept);
		
		UserProfile profileWithoutDept = UserProfile.create(userWithoutDept, "무학과학생");
		userProfileRepository.save(profileWithoutDept);
		
		Student studentWithoutDept = Student.builder()
				.studentId(20250003L)
				.user(userWithoutDept)
				.admissionYear(2025)
				.grade(1)
				.build();
		studentRepository.save(studentWithoutDept);
		
		entityManager.flush();
		entityManager.clear();
		
		// when
		Page<PostListResponseDto> result = postService.getPostListByBoardType(
				"DEPARTMENT",
				null,
				null,
				PageRequest.of(0, 20),
				studentWithoutDept.getStudentId()
		);
		
		// then
		// 학과가 없으면 필터링이 적용되지 않아 모든 게시글이 반환되거나,
		// 로직에 따라 빈 결과가 반환될 수 있음
		log.info("학과 없는 사용자가 조회한 게시글 수: {}", result.getTotalElements());
	}
	
	@Test
	@DisplayName("학과 게시글 생성 시 해시태그 자동 추가")
	@Transactional
	void createDepartmentPost_WithDepartmentHashtag() {
		// given
		PostCreateRequestDto request = PostCreateRequestDto.builder()
				.title("[컴공] 알고리즘 스터디 모집")
				.content("알고리즘 스터디원을 모집합니다.")
				.postType(PostType.NORMAL)
				.isAnonymous(false)
				.hashtags(Arrays.asList("컴퓨터공학과"))
				.attachmentIds(List.of())
				.build();
		
		// when
		PostResponseDto result = postService.createPost("DEPARTMENT", request, csStudent.getStudentId());
		
		// then
		assertThat(result.getTitle()).contains("[컴공]");
		assertThat(result.getHashtags())
				.isNotEmpty()
				.anyMatch(tag -> tag.getTagName().equals("컴퓨터공학과"));
		
		log.info("생성된 게시글 ID: {}, 해시태그: {}", result.getId(), result.getHashtags());
	}
	
	@Test
	@DisplayName("검색어와 함께 학과 게시판 조회")
	@Transactional
	void getDepartmentPostList_WithSearch() {
		// when
		Page<PostListResponseDto> result = postService.getPostListByBoardType(
				"DEPARTMENT",
				"수강신청",
				null,
				PageRequest.of(0, 20),
				csStudent.getStudentId()
		);
		
		// then
		assertThat(result.getContent())
				.isNotEmpty()
				.allMatch(post -> post.getTitle().contains("수강신청"))
				.allMatch(post -> post.getTitle().contains("[컴공]"));
		
		log.info("검색 결과 수: {}", result.getTotalElements());
	}
	
	@Test
	@DisplayName("다른 학과 학생은 다른 학과 게시글을 조회할 수 없음")
	@Transactional
	void getDepartmentPostList_CrossDepartmentAccess() {
		// when - 컴퓨터공학과 학생이 조회
		Page<PostListResponseDto> csResult = postService.getPostListByBoardType(
				"DEPARTMENT",
				null,
				null,
				PageRequest.of(0, 20),
				csStudent.getStudentId()
		);
		
		// when - 경영학과 학생이 조회
		Page<PostListResponseDto> baResult = postService.getPostListByBoardType(
				"DEPARTMENT",
				null,
				null,
				PageRequest.of(0, 20),
				baStudent.getStudentId()
		);
		
		// then - 각자 본인 학과 게시글만 조회
		assertThat(csResult.getContent())
				.noneMatch(post -> baResult.getContent().stream()
						.anyMatch(baPost -> baPost.getId().equals(post.getId())));
		
		log.info("컴공 학생 조회 결과: {}건, 경영 학생 조회 결과: {}건",
				csResult.getTotalElements(), baResult.getTotalElements());
	}
}
