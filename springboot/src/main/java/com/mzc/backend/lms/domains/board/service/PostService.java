package com.mzc.backend.lms.domains.board.service;

import com.mzc.backend.lms.domains.board.dto.request.PostCreateRequestDto;
import com.mzc.backend.lms.domains.board.dto.request.PostUpdateRequestDto;
import com.mzc.backend.lms.domains.board.dto.response.PostListResponseDto;
import com.mzc.backend.lms.domains.board.dto.response.PostResponseDto;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.entity.PostLike;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.exception.BoardErrorCode;
import com.mzc.backend.lms.domains.board.exception.BoardException;
import com.mzc.backend.lms.domains.board.repository.BoardCategoryRepository;
import com.mzc.backend.lms.domains.board.repository.PostLikeRepository;
import com.mzc.backend.lms.domains.board.repository.PostRepository;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import com.mzc.backend.lms.util.file.FileUploadUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final FileUploadUtils fileStorageService;

    /**
     * 게시글 생성 (boardType 기반)
     */
    @Transactional
    public PostResponseDto createPost(String boardTypeStr, PostCreateRequestDto request, List<MultipartFile> files) {
        // 1. BoardType 변환
        BoardType boardType;
        try {
            boardType = BoardType.valueOf(boardTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BoardException(BoardErrorCode.BOARD_CATEGORY_NOT_FOUND);
        }

        // 2. 카테고리 조회
        BoardCategory category = boardCategoryRepository.findByBoardType(boardType)
                .orElseThrow(() -> new BoardException(BoardErrorCode.BOARD_CATEGORY_NOT_FOUND));

        // 3. 카테고리 정책 검증
        validateCategoryPolicy(category, request.getIsAnonymous());

        // 4. 게시글 생성
        Post post = Post.builder()
                .category(category)
                .title(request.getTitle())
                .content(request.getContent())
                .postType(request.getPostType())
                .isAnonymous(request.getIsAnonymous())
                .build();

        // 5. 저장
        Post savedPost = postRepository.save(post);
        
        // 6. 파일 저장
        if (files != null && !files.isEmpty()) {
            fileStorageService.uploadFiles(savedPost, files);
            log.info("첨부파일 {}개 저장 완료", files.size());
        }
        
        log.info("게시글 생성 완료: postId={}", savedPost.getId());

        return PostResponseDto.from(savedPost);
    }

    /**
     * 게시글 목록 조회 (boardType 기반)
     */
    public Page<PostListResponseDto> getPostListByBoardType(String boardTypeStr, String search, Pageable pageable) {
        // 1. BoardType 변환
        BoardType boardType;
        try {
            boardType = BoardType.valueOf(boardTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BoardException(BoardErrorCode.BOARD_CATEGORY_NOT_FOUND);
        }

        // 2. 카테고리 조회
        BoardCategory category = boardCategoryRepository.findByBoardType(boardType)
                .orElseThrow(() -> new BoardException(BoardErrorCode.BOARD_CATEGORY_NOT_FOUND));

        // 3. 검색 여부에 따라 분기
        Page<Post> posts;
        if (search != null && !search.isBlank()) {
            posts = postRepository.findByCategoryAndTitleContaining(category, search, pageable);
        } else {
            posts = postRepository.findByCategory(category, pageable);
        }

        return posts.map(PostListResponseDto::from);
    }

    /**
     * 게시글 상세 조회 (조회수 증가)
     */
    @Transactional
    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        // soft delete 체크
        if (post.isDeleted()) {
            throw new BoardException(BoardErrorCode.POST_ALREADY_DELETED);
        }

        // 조회수 증가
        post.increaseViewCount();

        return PostResponseDto.from(post);
    }

    /**
     * 게시글 목록 조회 (페이징 + 검색)
     */
    public Page<PostListResponseDto> getPostList(Long categoryId, String keyword, Pageable pageable) {
        Page<Post> posts;
        if (categoryId != null) {
            // 특정 카테고리의 게시글 조회
            BoardCategory category = boardCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BoardException(BoardErrorCode.BOARD_CATEGORY_NOT_FOUND));
            
            if (keyword != null && !keyword.isBlank()) {
                posts = postRepository.findByCategoryAndTitleContaining(category, keyword, pageable);
            } else {
                posts = postRepository.findByCategory(category, pageable);
            }
        } else {
            // 전체 게시글 조회
            if (keyword != null && !keyword.isBlank()) {
                posts = postRepository.findByTitleContaining(keyword, pageable);
            } else {
                posts = postRepository.findAll(pageable);
            }
        }

        return posts.map(PostListResponseDto::from);
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public PostResponseDto updatePost(Long postId, PostUpdateRequestDto request, List<MultipartFile> files) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        if (post.isDeleted()) {
            throw new BoardException(BoardErrorCode.POST_ALREADY_DELETED);
        }

        // 1. 텍스트 수정
        post.update(request.getTitle(), request.getContent(), post.isAnonymous());

        // 2. 기존 첨부파일 삭제 (요청에 삭제 ID 목록이 있는 경우)
        if (request.getDeleteAttachmentIds() != null && !request.getDeleteAttachmentIds().isEmpty()) {
            fileStorageService.deleteAttachmentsByIds(request.getDeleteAttachmentIds());
            log.info("첨부파일 {}개 삭제 완료", request.getDeleteAttachmentIds().size());
        }

        // 3. 새 첨부파일 추가
        if (files != null && !files.isEmpty()) {
            fileStorageService.uploadFiles(post, files);
            log.info("첨부파일 {}개 추가 완료", files.size());
        }

        return PostResponseDto.from(post);
    }

    /**
     * 게시글 삭제 (Soft Delete)
     */
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        if (post.isDeleted()) {
            throw new BoardException(BoardErrorCode.POST_ALREADY_DELETED);
        }

        // 첨부파일 삭제
        fileStorageService.deletePostFiles(post);
        
        post.delete();
    }

    /**
     * 게시글 좋아요 토글 (중복 방지)
     * 이미 좋아요한 경우 → 취소
     * 좋아요하지 않은 경우 → 추가
     */
    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        log.info("게시글 좋아요 토글: postId={}, userId={}", postId, userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이미 좋아요한 경우
        return postLikeRepository.findByUserIdAndPostId(userId, postId)
                .map(existingLike -> {
                    // 좋아요 취소
                    postLikeRepository.delete(existingLike);
                    post.decreaseLikeCount();
                    log.info("좋아요 취소: postId={}, userId={}", postId, userId);
                    return false; // 좋아요 취소됨
                })
                .orElseGet(() -> {
                    // 새로운 좋아요
                    PostLike newLike = PostLike.create(user, post);
                    postLikeRepository.save(newLike);
                    post.increaseLikeCount();
                    log.info("좋아요 추가: postId={}, userId={}", postId, userId);
                    return true; // 좋아요 추가됨
                });
    }

    /**
     * 사용자의 게시글 좋아요 여부 조회
     */
    @Transactional(readOnly = true)
    public boolean isLikedByUser(Long postId, Long userId) {
        return postLikeRepository.existsByUserIdAndPostId(userId, postId);
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
