package com.mzc.backend.lms.domains.board.controller;

import com.mzc.backend.lms.domains.board.dto.request.CommentCreateRequestDto;
import com.mzc.backend.lms.domains.board.dto.request.CommentUpdateRequestDto;
import com.mzc.backend.lms.domains.board.dto.response.CommentResponseDto;
import com.mzc.backend.lms.domains.board.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/board")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 생성
    @PostMapping("/comments")
    public ResponseEntity<CommentResponseDto> createComment(@Valid @RequestBody CommentCreateRequestDto request) {
        log.info("댓글 생성 API 호출: postId={}, parentCommentId={}", request.getPostId(), request.getParentCommentId());
        CommentResponseDto response = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 댓글 목록 조회 (특정 게시글의 댓글들)
    @GetMapping("/comments")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPost(@RequestParam Long postId) {
        log.info("댓글 목록 조회 API 호출: postId={}", postId);
        List<CommentResponseDto> response = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(response);
    }

    // 댓글 수정
    @PutMapping("/comments/{id}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentUpdateRequestDto request) {
        log.info("댓글 수정 API 호출: commentId={}", id);
        CommentResponseDto response = commentService.updateComment(id, request);
        return ResponseEntity.ok(response);
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        log.info("댓글 삭제 API 호출: commentId={}", id);
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
