package com.mzc.backend.lms.domains.board.service;

import com.mzc.backend.lms.domains.board.dto.request.PostCreateRequestDto;
import com.mzc.backend.lms.domains.board.dto.request.PostUpdateRequestDto;
import com.mzc.backend.lms.domains.board.dto.response.PostListResponseDto;
import com.mzc.backend.lms.domains.board.dto.response.PostResponseDto;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.enums.PostType;
import com.mzc.backend.lms.domains.board.exception.BoardException;
import com.mzc.backend.lms.domains.board.repository.BoardCategoryRepository;
import com.mzc.backend.lms.domains.board.repository.PostRepository;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("PostService 통합 테스트")
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BoardCategoryRepository boardCategoryRepository;

    private BoardCategory testCategory;

    @BeforeEach
    void setUp() {
        // 기존 카테고리 조회 또는 생성
        testCategory = boardCategoryRepository.findByBoardType(BoardType.NOTICE)
                .orElseGet(() -> {
                    BoardCategory category = new BoardCategory(BoardType.NOTICE, true, true, true);
                    return boardCategoryRepository.save(category);
                });
    }

    @Test
    @DisplayName("게시글 생성 성공")
    @Rollback(false)
    void createPost_Success() {
        // given
        PostCreateRequestDto request = PostCreateRequestDto.builder()
                .categoryId(testCategory.getId())
                .title("새 게시글")
                .content("새 게시글 내용")
                .postType(PostType.NORMAL)
                .isAnonymous(false)
                .build();

        // when
        PostResponseDto response = postService.createPost("NOTICE", request, null);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("새 게시글");
        assertThat(response.getContent()).isEqualTo("새 게시글 내용");
    }

    @Test
    @DisplayName("게시글 생성 실패 - 존재하지 않는 카테고리")
    void createPost_Fail_CategoryNotFound() {
        // given
        PostCreateRequestDto request = PostCreateRequestDto.builder()
                .categoryId(999L)
                .title("게시글")
                .content("내용")
                .postType(PostType.NORMAL)
                .isAnonymous(false)
                .build();

        // when & then
        assertThatThrownBy(() -> postService.createPost("NOTICE", request, null))
                .isInstanceOf(BoardException.class);
    }

    @Test
    @DisplayName("게시글 조회 성공 - 조회수 증가")
    void getPost_Success() {
        // given
        Post post = new Post(testCategory, "조회 테스트", "내용", PostType.NORMAL, false);
        post = postRepository.save(post);
        Long postId = post.getId();
        int initialViewCount = post.getViewCount();

        // when
        PostResponseDto response = postService.getPost(postId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("조회 테스트");
        
        Post updatedPost = postRepository.findById(postId).orElseThrow();
        assertThat(updatedPost.getViewCount()).isEqualTo(initialViewCount + 1);
    }

    @Test
    @DisplayName("게시글 목록 조회 - 페이징")
    void getPostList_Success() {
        // given
        postRepository.save(new Post(testCategory, "게시글 1", "내용 1", PostType.NORMAL, false));
        postRepository.save(new Post(testCategory, "게시글 2", "내용 2", PostType.NORMAL, false));
        postRepository.save(new Post(testCategory, "게시글 3", "내용 3", PostType.NORMAL, false));

        // when
        Page<PostListResponseDto> result = postService.getPostList(testCategory.getId(), null, PageRequest.of(0, 10));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("게시글 검색 성공")
    void searchPost_Success() {
        // given
        postRepository.save(new Post(testCategory, "검색 대상 게시글", "내용", PostType.NORMAL, false));
        postRepository.save(new Post(testCategory, "다른 게시글", "내용", PostType.NORMAL, false));

        // when
        Page<PostListResponseDto> result = postService.getPostList(testCategory.getId(), "검색", PageRequest.of(0, 10));

        // then
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("검색 대상 게시글");
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_Success() {
        // given
        Post post = new Post(testCategory, "원본 제목", "원본 내용", PostType.NORMAL, false);
        post = postRepository.save(post);
        
        PostUpdateRequestDto request = PostUpdateRequestDto.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        // when
        PostResponseDto response = postService.updatePost(post.getId(), request, null);

        // then
        assertThat(response.getTitle()).isEqualTo("수정된 제목");
        assertThat(response.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("게시글 삭제 성공 - Soft Delete")
    void deletePost_Success() {
        // given
        Post post = new Post(testCategory, "삭제할 게시글", "내용", PostType.NORMAL, false);
        post = postRepository.save(post);
        Long postId = post.getId();

        // when
        postService.deletePost(postId);

        // then
        Post deletedPost = postRepository.findById(postId).orElseThrow();
        assertThat(deletedPost.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("좋아요 증가")
    void increaseLikeCount_Success() {
        // given
        Post post = new Post(testCategory, "게시글", "내용", PostType.NORMAL, false);
        post = postRepository.save(post);
        Long postId = post.getId();

        // when
        postService.increaseLikeCount(postId);

        // then
        Post updatedPost = postRepository.findById(postId).orElseThrow();
        assertThat(updatedPost.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("좋아요 감소")
    void decreaseLikeCount_Success() {
        // given
        Post post = new Post(testCategory, "게시글", "내용", PostType.NORMAL, false);
        post = postRepository.save(post);
        postService.increaseLikeCount(post.getId()); // 좋아요 1개
        Long postId = post.getId();

        // when
        postService.decreaseLikeCount(postId);

        // then
        Post updatedPost = postRepository.findById(postId).orElseThrow();
        assertThat(updatedPost.getLikeCount()).isEqualTo(0);
    }
}
