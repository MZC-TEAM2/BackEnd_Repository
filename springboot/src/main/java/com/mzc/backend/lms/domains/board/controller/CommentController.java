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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
	public ResponseEntity<CommentResponseDto> createComment(
			@AuthenticationPrincipal Long userId,
			@Valid @RequestBody CommentCreateRequestDto request) {
		log.info("댓글 생성 API 호출: userId={}, postId={}, parentCommentId={}",
				userId, request.getPostId(), request.getParentCommentId());
		
		// 인증된 사용자 ID를 작성자로 설정
		request.setAuthorIdFromAuth(userId);
		
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
			@Valid @RequestBody CommentUpdateRequestDto request,
			Authentication authentication) {
		Long updatedBy = (Long) authentication.getPrincipal();
		log.info("댓글 수정 API 호출: commentId={}, updatedBy={}", id, updatedBy);
		CommentResponseDto response = commentService.updateComment(id, request, updatedBy);
		return ResponseEntity.ok(response);
	}
	
	// 댓글 삭제
	@DeleteMapping("/comments/{id}")
	public ResponseEntity<Void> deleteComment(
			@PathVariable Long id,
			Authentication authentication) {
		Long deletedBy = (Long) authentication.getPrincipal();
		log.info("댓글 삭제 API 호출: commentId={}, deletedBy={}", id, deletedBy);
		commentService.deleteComment(id, deletedBy);
		return ResponseEntity.noContent().build();
	}
}
