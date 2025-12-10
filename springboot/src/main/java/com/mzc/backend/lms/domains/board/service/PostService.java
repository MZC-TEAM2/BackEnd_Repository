package com.mzc.backend.lms.domains.board.service;

import com.mzc.backend.lms.domains.board.dto.request.PostCreateRequest;
import com.mzc.backend.lms.domains.board.dto.request.PostUpdateRequest;
import com.mzc.backend.lms.domains.board.dto.response.PostListResponse;
import com.mzc.backend.lms.domains.board.dto.response.PostResponse;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.exception.BoardErrorCode;
import com.mzc.backend.lms.domains.board.exception.BoardException;
import com.mzc.backend.lms.domains.board.repository.BoardCategoryRepository;
import com.mzc.backend.lms.domains.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final BoardCategoryRepository boardCategoryRepository;

    /**
     * 게시글 생성
     */
    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        log.info("게시글 생성 요청: categoryId={}, title={}", request.getCategoryId(), request.getTitle());

        // 1. 카테고리 조회
        BoardCategory category = boardCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BoardException(BoardErrorCode.BOARD_CATEGORY_NOT_FOUND));

        // 2. 카테고리 정책 검증
        validateCategoryPolicy(category, request.getIsAnonymous());

        // 3. 게시글 생성
        Post post = Post.builder()
                .category(category)
                .title(request.getTitle())
                .content(request.getContent())
                .postType(request.getPostType())
                .isAnonymous(request.getIsAnonymous())
                .build();

        // 4. 저장
        Post savedPost = postRepository.save(post);
        log.info("게시글 생성 완료: postId={}", savedPost.getId());

        return PostResponse.from(savedPost);
    }

    /**
     * 게시글 상세 조회 (조회수 증가)
     */
    @Transactional
    public PostResponse getPost(Long postId) {
        log.info("게시글 조회: postId={}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        // soft delete 체크
        if (post.isDeleted()) {
            throw new BoardException(BoardErrorCode.POST_ALREADY_DELETED);
        }

        // 조회수 증가
        post.increaseViewCount();

        return PostResponse.from(post);
    }

    /**
     * 게시글 목록 조회 (페이징)
     */
    public Page<PostListResponse> getPostList(Long categoryId, Pageable pageable) {
        log.info("게시글 목록 조회: categoryId={}, page={}", categoryId, pageable.getPageNumber());

        Page<Post> posts;
        if (categoryId != null) {
            // 특정 카테고리의 게시글 조회
            BoardCategory category = boardCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BoardException(BoardErrorCode.BOARD_CATEGORY_NOT_FOUND));
            posts = postRepository.search(category, pageable);
        } else {
            // 전체 게시글 조회
            posts = postRepository.findAll(pageable);
        }

        return posts.map(PostListResponse::from);
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request) {
        log.info("게시글 수정: postId={}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        if (post.isDeleted()) {
            throw new BoardException(BoardErrorCode.POST_ALREADY_DELETED);
        }

        // Entity의 비즈니스 로직 사용
        post.update(request.getTitle(), request.getContent(), post.isAnonymous());

        return PostResponse.from(post);
    }

    /**
     * 게시글 삭제 (Soft Delete)
     */
    @Transactional
    public void deletePost(Long postId) {
        log.info("게시글 삭제: postId={}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        if (post.isDeleted()) {
            throw new BoardException(BoardErrorCode.POST_ALREADY_DELETED);
        }

        post.delete();
    }

    /**
     * 게시글 좋아요 증가
     */
    @Transactional
    public void increaseLikeCount(Long postId) {
        log.info("게시글 좋아요 증가: postId={}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        post.increaseLikeCount();
    }

    /**
     * 게시글 좋아요 감소
     */
    @Transactional
    public void decreaseLikeCount(Long postId) {
        log.info("게시글 좋아요 감소: postId={}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        post.decreaseLikeCount();
    }

    /**
     * 카테고리 정책 검증
     */
    private void validateCategoryPolicy(BoardCategory category, Boolean isAnonymous) {
        if (isAnonymous && !category.isAllowAnonymous()) {
            throw new BoardException(BoardErrorCode.POST_ANONYMOUS_NOT_ALLOWED);
        }
    }
}
