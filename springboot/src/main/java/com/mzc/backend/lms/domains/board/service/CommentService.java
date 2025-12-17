package com.mzc.backend.lms.domains.board.service;

import com.mzc.backend.lms.domains.board.dto.request.CommentCreateRequestDto;
import com.mzc.backend.lms.domains.board.dto.request.CommentUpdateRequestDto;
import com.mzc.backend.lms.domains.board.dto.response.AttachmentResponseDto;
import com.mzc.backend.lms.domains.board.dto.response.CommentResponseDto;
import com.mzc.backend.lms.domains.board.entity.Attachment;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Comment;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.exception.BoardErrorCode;
import com.mzc.backend.lms.domains.board.exception.BoardException;
import com.mzc.backend.lms.domains.board.repository.AttachmentRepository;
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
    private final AttachmentRepository attachmentRepository;
    
    private static final int MAX_COMMENT_DEPTH = 1; // 대댓글까지만 허용 (depth 0, 1)

    /**
     * 댓글 생성
     */
    @Transactional
    public CommentResponseDto createComment(CommentCreateRequestDto request, Long authorId) {
        log.info("댓글 생성: postId={}, parentCommentId={}, authorId={}", request.getPostId(), request.getParentCommentId(), authorId);

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
                .authorId(authorId)
                .build();

        // 5. 저장
        Comment savedComment = commentRepository.save(comment);
        
        // 6. 첨부파일 연결
        List<Attachment> savedAttachments = null;
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            List<Attachment> attachments = attachmentRepository.findAllById(request.getAttachmentIds());
            
            for (Attachment attachment : attachments) {
                attachment.attachToComment(savedComment);
            }
            
            savedAttachments = attachmentRepository.saveAll(attachments);
            
            log.info("댓글에 첨부파일 연결: commentId={}, attachmentCount={}", 
                    savedComment.getId(), attachments.size());
        }
        
        log.info("댓글 생성 완료: commentId={}, depth={}", savedComment.getId(), savedComment.getDepth());

        // 7. 응답 생성 (첨부파일 포함)
        CommentResponseDto response = CommentResponseDto.from(savedComment);
        
        // 첨부파일이 있으면 수동으로 추가 (지연 로딩 문제 해결)
        if (savedAttachments != null && !savedAttachments.isEmpty()) {
            List<AttachmentResponseDto> attachmentDtos = savedAttachments.stream()
                    .map(AttachmentResponseDto::from)
                    .collect(java.util.stream.Collectors.toList());
            
            // CommentResponseDto를 새로 빌드
            return CommentResponseDto.builder()
                    .id(response.getId())
                    .postId(response.getPostId())
                    .parentCommentId(response.getParentCommentId())
                    .content(response.getContent())
                    .depth(response.getDepth())
                    .isDeletedByAdmin(response.isDeletedByAdmin())
                    .createdAt(response.getCreatedAt())
                    .updatedAt(response.getUpdatedAt())
                    .childComments(response.getChildComments())
                    .attachments(attachmentDtos)
                    .build();
        }

        return response;
    }

    /**
     * 게시글의 모든 댓글 조회
     */
    public List<CommentResponseDto> getCommentsByPost(Long postId) {
        log.info("게시글의 댓글 조회: postId={}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        List<Comment> comments = commentRepository.findByPost(post);
        
        // 최상위 댓글만 반환 (하위 댓글은 childComments에 포함됨)
        return comments.stream()
                .filter(comment -> comment.getParentComment() == null)
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentUpdateRequestDto request, Long updatedBy) {
        log.info("댓글 수정: commentId={}, updatedBy={}", commentId, updatedBy);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.COMMENT_NOT_FOUND));

        if (comment.isDeleted()) {
            throw new BoardException(BoardErrorCode.COMMENT_ALREADY_DELETED);
        }

        // Entity의 비즈니스 로직 사용
        comment.updateContent(request.getContent());

        // 삭제할 첨부파일 처리
        if (request.getRemovedAttachmentIds() != null && !request.getRemovedAttachmentIds().isEmpty()) {
            List<Attachment> attachmentsToRemove = attachmentRepository.findAllById(request.getRemovedAttachmentIds());
            for (Attachment attachment : attachmentsToRemove) {
                // 댓글에서 첨부파일 제거
                comment.getAttachments().remove(attachment);
                // 첨부파일 삭제
                attachmentRepository.delete(attachment);
            }
            log.info("삭제된 첨부파일 수: {}", attachmentsToRemove.size());
        }

        // 새로운 첨부파일 추가
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            List<Attachment> newAttachments = attachmentRepository.findAllById(request.getAttachmentIds());
            for (Attachment attachment : newAttachments) {
                attachment.attachToComment(comment);
                comment.getAttachments().add(attachment);
            }
            attachmentRepository.saveAll(newAttachments);
            log.info("추가된 첨부파일 수: {}", newAttachments.size());
        }

        return CommentResponseDto.from(comment);
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
