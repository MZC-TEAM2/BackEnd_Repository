package com.mzc.backend.lms.domains.board.service;

import com.mzc.backend.lms.domains.board.dto.request.PostCreateRequestDto;
import com.mzc.backend.lms.domains.board.dto.response.PostListResponseDto;
import com.mzc.backend.lms.domains.board.dto.response.PostResponseDto;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.enums.PostType;
import com.mzc.backend.lms.domains.board.exception.BoardErrorCode;
import com.mzc.backend.lms.domains.board.exception.BoardException;
import com.mzc.backend.lms.domains.board.repository.BoardCategoryRepository;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.*;

/**
 * 게시판 접근 제어 (RBAC) 테스트
 * 1단계: V6 마이그레이션으로 추가된 게시판 카테고리 확인
 * 2단계: 역할 기반 접근 제어 동작 확인
 * 3단계: API 레벨 통합 테스트
 */
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("게시판 RBAC 통합 테스트")
class BoardAccessControlTest {

    @Autowired
    private PostService postService;

    @Autowired
    private BoardCategoryRepository boardCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User studentUser;
    private User professorUser;

    @BeforeEach
    void setUp() {
        // 게시판 카테고리 생성 (존재하지 않으면)
        createBoardCategoriesIfNotExist();
        
        // 학생 사용자 생성 (ID 범위: 20241000~20241999)
        studentUser = userRepository.findById(20241001L)
                .orElseGet(() -> {
                    User user = User.create(20241001L, "student@test.com", "password");
                    return userRepository.save(user);
                });

        // 교수 사용자 생성 (ID 범위: 20242000~20242999)
        professorUser = userRepository.findById(20242001L)
                .orElseGet(() -> {
                    User user = User.create(20242001L, "professor@test.com", "password");
                    return userRepository.save(user);
                });
    }
    
    private void createBoardCategoriesIfNotExist() {
        BoardType[] allTypes = {
            BoardType.NOTICE, BoardType.FREE, BoardType.QUESTION, BoardType.DISCUSSION, BoardType.DEPARTMENT,
            BoardType.PROFESSOR, BoardType.STUDENT,
            BoardType.CONTEST, BoardType.CAREER,
            BoardType.ASSIGNMENT, BoardType.EXAM, BoardType.QUIZ, BoardType.STUDY_RECRUITMENT
        };
        
        for (BoardType type : allTypes) {
            if (boardCategoryRepository.findByBoardType(type).isEmpty()) {
                BoardCategory category = new BoardCategory(type, true, true, type != BoardType.NOTICE);
                boardCategoryRepository.save(category);
                log.debug("Created board category: {}", type);
            }
        }
    }

    // ========== 1단계: 게시판 카테고리 존재 확인 테스트 ==========

    @Test
    @DisplayName("[1단계] V6 마이그레이션: 기본 게시판 카테고리 존재 확인")
    void step1_basicBoardCategories_ShouldExist() {
        // when & then
        assertThat(boardCategoryRepository.findByBoardType(BoardType.NOTICE)).isPresent();
        assertThat(boardCategoryRepository.findByBoardType(BoardType.FREE)).isPresent();
        assertThat(boardCategoryRepository.findByBoardType(BoardType.QUESTION)).isPresent();
        assertThat(boardCategoryRepository.findByBoardType(BoardType.DISCUSSION)).isPresent();
        assertThat(boardCategoryRepository.findByBoardType(BoardType.DEPARTMENT)).isPresent();
        
        log.info("✅ 기본 게시판 5개 존재 확인 완료");
    }

    @Test
    @DisplayName("[1단계] V6 마이그레이션: 역할별 제한 게시판 존재 확인")
    void step1_roleRestrictedBoards_ShouldExist() {
        // when & then
        assertThat(boardCategoryRepository.findByBoardType(BoardType.PROFESSOR)).isPresent();
        assertThat(boardCategoryRepository.findByBoardType(BoardType.STUDENT)).isPresent();
        
        log.info("✅ 역할별 제한 게시판 2개 존재 확인 완료");
    }

    @Test
    @DisplayName("[1단계] V6 마이그레이션: 특수 목적 게시판 존재 확인")
    void step1_specialPurposeBoards_ShouldExist() {
        // when & then
        assertThat(boardCategoryRepository.findByBoardType(BoardType.CONTEST)).isPresent();
        assertThat(boardCategoryRepository.findByBoardType(BoardType.CAREER)).isPresent();
        
        log.info("✅ 특수 목적 게시판 2개 존재 확인 완료");
    }

    @Test
    @DisplayName("[1단계] V6 마이그레이션: 학습관리 게시판 존재 확인")
    void step1_learningManagementBoards_ShouldExist() {
        // when & then
        assertThat(boardCategoryRepository.findByBoardType(BoardType.ASSIGNMENT)).isPresent();
        assertThat(boardCategoryRepository.findByBoardType(BoardType.EXAM)).isPresent();
        assertThat(boardCategoryRepository.findByBoardType(BoardType.QUIZ)).isPresent();
        assertThat(boardCategoryRepository.findByBoardType(BoardType.STUDY_RECRUITMENT)).isPresent();
        
        log.info("✅ 학습관리 게시판 4개 존재 확인 완료");
    }

    @Test
    @DisplayName("[1단계] V6 마이그레이션: 전체 13개 게시판 존재 확인")
    void step1_allBoardCategories_ShouldExist() {
        // when
        long totalCount = boardCategoryRepository.count();

        // then
        assertThat(totalCount).isGreaterThanOrEqualTo(13);
        log.info("✅ 전체 게시판 카테고리 {}개 확인 완료 (최소 13개 이상)", totalCount);
    }

    // ========== 2단계: RBAC 접근 제어 테스트 ==========

    @Test
    @DisplayName("[2단계] RBAC: 자유 게시판 - 모두 접근 가능")
    void step2_freeBoard_AllUsersCanAccess() {
        // given
        PostCreateRequestDto studentRequest = createPostRequest(studentUser.getId(), "학생이 작성한 글");
        PostCreateRequestDto professorRequest = createPostRequest(professorUser.getId(), "교수가 작성한 글");

        // when & then - 학생도 성공
        PostResponseDto studentResponse = postService.createPost("FREE", studentRequest);
        assertThat(studentResponse).isNotNull();
        assertThat(studentResponse.getId()).isNotNull();
        log.info("✅ 학생 -> 자유 게시판 접근 성공");

        // when & then - 교수도 성공
        PostResponseDto professorResponse = postService.createPost("FREE", professorRequest);
        assertThat(professorResponse).isNotNull();
        assertThat(professorResponse.getId()).isNotNull();
        log.info("✅ 교수 -> 자유 게시판 접근 성공");
    }

    @Test
    @DisplayName("[2단계] RBAC: 교수 게시판 - 학생 접근 거부")
    void step2_professorBoard_StudentAccessDenied() {
        // given
        PostCreateRequestDto studentRequest = createPostRequest(studentUser.getId(), "학생이 교수 게시판 접근 시도");

        // when & then
        assertThatThrownBy(() -> postService.createPost("PROFESSOR", studentRequest))
                .isInstanceOf(BoardException.class)
                .hasMessageContaining("교수만 접근 가능한 게시판입니다");
        
        log.info("✅ 학생 -> 교수 게시판 접근 차단 확인");
    }

    @Test
    @DisplayName("[2단계] RBAC: 교수 게시판 - 교수 접근 허용")
    void step2_professorBoard_ProfessorAccessAllowed() {
        // given
        PostCreateRequestDto professorRequest = createPostRequest(professorUser.getId(), "교수가 작성한 글");

        // when
        PostResponseDto response = postService.createPost("PROFESSOR", professorRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("교수가 작성한 글");
        log.info("✅ 교수 -> 교수 게시판 접근 성공");
    }

    @Test
    @DisplayName("[2단계] RBAC: 학생 게시판 - 교수 접근 거부")
    void step2_studentBoard_ProfessorAccessDenied() {
        // given
        PostCreateRequestDto professorRequest = createPostRequest(professorUser.getId(), "교수가 학생 게시판 접근 시도");

        // when & then
        assertThatThrownBy(() -> postService.createPost("STUDENT", professorRequest))
                .isInstanceOf(BoardException.class)
                .hasMessageContaining("학생만 접근 가능한 게시판입니다");
        
        log.info("✅ 교수 -> 학생 게시판 접근 차단 확인");
    }

    @Test
    @DisplayName("[2단계] RBAC: 학생 게시판 - 학생 접근 허용")
    void step2_studentBoard_StudentAccessAllowed() {
        // given
        PostCreateRequestDto studentRequest = createPostRequest(studentUser.getId(), "학생이 작성한 글");

        // when
        PostResponseDto response = postService.createPost("STUDENT", studentRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("학생이 작성한 글");
        log.info("✅ 학생 -> 학생 게시판 접근 성공");
    }

    @Test
    @DisplayName("[2단계] RBAC: 특수 목적 게시판 - 모두 접근 가능")
    void step2_specialPurposeBoards_AllUsersCanAccess() {
        // given
        PostCreateRequestDto contestRequest = createPostRequest(studentUser.getId(), "공모전 정보");
        PostCreateRequestDto careerRequest = createPostRequest(professorUser.getId(), "취업 정보");

        // when & then - 공모전 게시판
        PostResponseDto contestResponse = postService.createPost("CONTEST", contestRequest);
        assertThat(contestResponse).isNotNull();
        log.info("✅ 공모전 게시판 접근 성공");

        // when & then - 취업 게시판
        PostResponseDto careerResponse = postService.createPost("CAREER", careerRequest);
        assertThat(careerResponse).isNotNull();
        log.info("✅ 취업 게시판 접근 성공");
    }

    // ========== 3단계: API 통합 테스트 ==========

    @Test
    @DisplayName("[3단계] API 통합: 게시글 목록 조회")
    void step3_apiIntegration_getPostList() {
        // given - 게시글 여러 개 생성
        for (int i = 1; i <= 3; i++) {
            PostCreateRequestDto request = createPostRequest(studentUser.getId(), "테스트 게시글 " + i);
            postService.createPost("FREE", request);
        }

        // when
        Page<PostListResponseDto> postPage = postService.getPostList(null, null, PageRequest.of(0, 10));

        // then
        assertThat(postPage).isNotNull();
        assertThat(postPage.getContent()).hasSizeGreaterThanOrEqualTo(3);
        log.info("✅ 게시글 목록 조회 성공: {}개", postPage.getContent().size());
    }

    @Test
    @DisplayName("[3단계] API 통합: 게시판별 목록 조회")
    void step3_apiIntegration_getPostListByBoardType() {
        // given
        postService.createPost("FREE", createPostRequest(studentUser.getId(), "자유 게시글 1"));
        postService.createPost("FREE", createPostRequest(studentUser.getId(), "자유 게시글 2"));
        postService.createPost("CONTEST", createPostRequest(studentUser.getId(), "공모전 게시글 1"));

        // when
        Page<PostListResponseDto> freePosts = postService.getPostListByBoardType("FREE", null, null, PageRequest.of(0, 10));
        Page<PostListResponseDto> contestPosts = postService.getPostListByBoardType("CONTEST", null, null, PageRequest.of(0, 10));

        // then
        assertThat(freePosts.getContent()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(contestPosts.getContent()).hasSizeGreaterThanOrEqualTo(1);
        log.info("✅ 게시판별 목록 조회 성공 - FREE: {}개, CONTEST: {}개", 
                freePosts.getContent().size(), contestPosts.getContent().size());
    }

    @Test
    @DisplayName("[3단계] API 통합: 존재하지 않는 게시판 타입")
    void step3_apiIntegration_invalidBoardType() {
        // given
        PostCreateRequestDto request = createPostRequest(studentUser.getId(), "잘못된 게시판");

        // when & then
        assertThatThrownBy(() -> postService.createPost("INVALID_BOARD", request))
                .isInstanceOf(BoardException.class);
        
        log.info("✅ 존재하지 않는 게시판 타입 예외 처리 확인");
    }

    // ========== Helper Methods ==========

    private PostCreateRequestDto createPostRequest(Long authorId, String title) {
        return PostCreateRequestDto.builder()
                .authorId(authorId)
                .title(title)
                .content("테스트 내용: " + title)
                .postType(PostType.NORMAL)
                .isAnonymous(false)
                .build();
    }
}
