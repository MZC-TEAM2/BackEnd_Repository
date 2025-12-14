package com.mzc.backend.lms.domains.board.controller;

import com.mzc.backend.lms.domains.board.dto.request.PostCreateRequestDto;
import com.mzc.backend.lms.domains.board.dto.request.PostUpdateRequestDto;
import com.mzc.backend.lms.domains.board.dto.response.LikeCheckResponseDto;
import com.mzc.backend.lms.domains.board.dto.response.LikeToggleResponseDto;
import com.mzc.backend.lms.domains.board.dto.response.PostListResponseDto;
import com.mzc.backend.lms.domains.board.dto.response.PostResponseDto;
import com.mzc.backend.lms.domains.board.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 게시글 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 생성 (2단계 업로드: 파일은 별도로 업로드하고 attachmentIds 전달)
    @PostMapping("/{boardType}/posts")
    public ResponseEntity<PostResponseDto> createPost(
            @PathVariable String boardType,
            @Valid @RequestBody PostCreateRequestDto request
    ) {
        log.info("게시글 생성 API 호출: boardType={}, title={}, attachmentIds={}",
                boardType, request.getTitle(), request.getAttachmentIds());
        PostResponseDto response = postService.createPost(boardType, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 게시글 상세 조회
    @GetMapping("/{boardType}/posts/{id}")
    public ResponseEntity<PostResponseDto> getPost(
            @PathVariable String boardType,
            @PathVariable Long id) {
        log.info("게시글 상세조회 API 호출: boardType={}, postId={}", boardType, id);
        PostResponseDto response = postService.getPost(id);
        return ResponseEntity.ok(response);
    }
    
    // 게시글 목록 조회 (검색어 포함)
    @GetMapping("/{boardType}/posts")
    public ResponseEntity<Page<PostListResponseDto>> getPostList(
            @PathVariable String boardType,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("게시글 목록 조회 API 호출: boardType={}, search={}, page={}", boardType, search, pageable.getPageNumber());
        Page<PostListResponseDto> response = postService.getPostListByBoardType(boardType, search, pageable);
        return ResponseEntity.ok(response);
    }

    // 게시글 수정 (Multipart - 파일 포함)
    @PutMapping(value = "/posts/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long id,
            @Valid @RequestPart("request") PostUpdateRequestDto request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        log.info("게시글 수정 API 호출 (Multipart): postId={}, title={}, fileCount={}, deleteAttachmentIds={}", 
                id, request.getTitle(), files != null ? files.size() : 0, request.getDeleteAttachmentIds());
        PostResponseDto response = postService.updatePost(id, request, files);
        return ResponseEntity.ok(response);
    }

    // 게시글 수정 (JSON - 첨부파일 ID만 포함)
    @PutMapping(value = "/posts/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostResponseDto> updatePostJson(
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequestDto request) {
        log.info("게시글 수정 API 호출 (JSON): postId={}, title={}, attachmentIds={}, deleteAttachmentIds={}", 
                id, request.getTitle(), request.getAttachmentIds(), request.getDeleteAttachmentIds());
        PostResponseDto response = postService.updatePost(id, request, null);
        return ResponseEntity.ok(response);
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        log.info("게시글 삭제 API 호출: postId={}", id);
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    // 게시글 좋아요 토글 (중복 방지)
    @PostMapping("/posts/{id}/like")
    public ResponseEntity<LikeToggleResponseDto> toggleLike(
            @PathVariable Long id,
            @RequestParam Long userId) {
        log.info("게시글 좋아요 토글 API 호출: postId={}, userId={}", id, userId);
        boolean isLiked = postService.toggleLike(id, userId);
        
        LikeToggleResponseDto response = LikeToggleResponseDto.of(isLiked);
        
        return ResponseEntity.ok(response);
    }

    // 게시글 좋아요 여부 조회
    @GetMapping("/posts/{id}/liked")
    public ResponseEntity<LikeCheckResponseDto> checkLiked(
            @PathVariable Long id,
            @RequestParam Long userId) {
        log.info("게시글 좋아요 여부 조회 API 호출: postId={}, userId={}", id, userId);
        boolean isLiked = postService.isLikedByUser(id, userId);
        
        LikeCheckResponseDto response = LikeCheckResponseDto.of(isLiked);
        
        return ResponseEntity.ok(response);
    }
}
