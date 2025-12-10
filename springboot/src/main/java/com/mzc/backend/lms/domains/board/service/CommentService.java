package com.mzc.backend.lms.domains.board.service;

import com.mzc.backend.lms.domains.board.dto.request.CommentCreateRequest;
import com.mzc.backend.lms.domains.board.dto.request.CommentUpdateRequest;
import com.mzc.backend.lms.domains.board.dto.response.CommentResponse;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Comment;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.exception.BoardErrorCode;
import com.mzc.backend.lms.domains.board.exception.BoardException;
import com.mzc.backend.lms.domains.board.repository.CommentRepository;
import com.mzc.backend.lms.domains.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 댓글 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    
    private static final int MAX_COMMENT_DEPTH = 2; // 대댓글까지만 허용

    /**
     * 댓글 생성
     */
    @Transactional
    public CommentResponse createComment(CommentCreateRequest request) {
        log.info("댓글 생성: postId={}, parentCommentId={}", request.getPostId(), request.getParentCommentId());

        // 1. 게시글 조회
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        // 2. 게시글의 카테고리 정책 확인
        validateCommentPolicy(post.getCategory());

        // 3. 부모 댓글이 있는 경우 (대댓글)
        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new BoardException(BoardErrorCode.PARENT_COMMENT_NOT_FOUND));
            
            // 댓글 깊이 검증
            if (parentComment.getDepth() >= MAX_COMMENT_DEPTH) {
                throw new BoardException(BoardErrorCode.COMMENT_DEPTH_EXCEEDED);
            }
        }

        // 4. 댓글 생성
        Comment comment = Comment.builder()
                .post(post)
                .parentComment(parentComment)
                .content(request.getContent())
                .build();

        // 5. 저장
        Comment savedComment = commentRepository.save(comment);
        log.info("댓글 생성 완료: commentId={}, depth={}", savedComment.getId(), savedComment.getDepth());

        return CommentResponse.from(savedComment);
    }

    /**
     * 게시글의 모든 댓글 조회
     */
    public List<CommentResponse> getCommentsByPost(Long postId) {
        log.info("게시글의 댓글 조회: postId={}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        List<Comment> comments = commentRepository.findByPost(post);
        
        // 최상위 댓글만 반환 (하위 댓글은 childComments에 포함됨)
        return comments.stream()
                .filter(comment -> comment.getParentComment() == null)
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentUpdateRequest request) {
        log.info("댓글 수정: commentId={}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.COMMENT_NOT_FOUND));

        if (comment.isDeleted()) {
            throw new BoardException(BoardErrorCode.COMMENT_ALREADY_DELETED);
        }

        // Entity의 비즈니스 로직 사용
        comment.updateContent(request.getContent());

        return CommentResponse.from(comment);
    }

    /**
     * 댓글 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteComment(Long commentId) {
        log.info("댓글 삭제: commentId={}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.COMMENT_NOT_FOUND));

        if (comment.isDeleted()) {
            throw new BoardException(BoardErrorCode.COMMENT_ALREADY_DELETED);
        }

        comment.delete();
    }

    /**
     * 댓글 허용 정책 검증
     */
    private void validateCommentPolicy(BoardCategory category) {
        if (!category.isAllowComments()) {
            throw new BoardException(BoardErrorCode.COMMENT_NOT_ALLOWED);
        }
    }
}
