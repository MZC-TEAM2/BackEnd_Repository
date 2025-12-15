package com.mzc.backend.lms.domains.board.service;

import com.mzc.backend.lms.domains.board.dto.request.PostCreateRequestDto;
import com.mzc.backend.lms.domains.board.dto.request.PostUpdateRequestDto;
import com.mzc.backend.lms.domains.board.dto.response.PostListResponseDto;
import com.mzc.backend.lms.domains.board.dto.response.PostResponseDto;
import com.mzc.backend.lms.domains.board.entity.Attachment;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.entity.PostLike;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.exception.BoardErrorCode;
import com.mzc.backend.lms.domains.board.exception.BoardException;
import com.mzc.backend.lms.domains.board.repository.AttachmentRepository;
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
    private final AttachmentRepository attachmentRepository;
    private final HashtagService hashtagService;

    /**
     * 게시글 생성 (boardType 기반, 2단계 업로드)
     */
    @Transactional
    public PostResponseDto createPost(String boardTypeStr, PostCreateRequestDto request) {
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

        // 2-1. 역할 기반 접근 제어 (RBAC)
        validateBoardAccess(boardType, request.getAuthorId());

        // 3. 카테고리 정책 검증
        validateCategoryPolicy(category, request.getIsAnonymous());

        // 4. 게시글 생성
        Post post = Post.builder()
                .category(category)
                .title(request.getTitle())
                .content(request.getContent())
                .postType(request.getPostType())
                .isAnonymous(request.getIsAnonymous())
                .authorId(request.getAuthorId())
                .build();

        // 5. 저장
        Post savedPost = postRepository.save(post);
        
        // 6. 해시태그 처리
        if (request.getHashtags() != null && !request.getHashtags().isEmpty()) {
            hashtagService.attachHashtagsToPost(savedPost, request.getHashtags(), request.getAuthorId());
            log.info("게시글에 해시태그 연결: postId={}, hashtagCount={}", 
                    savedPost.getId(), request.getHashtags().size());
        }
        
        // 7. 첨부파일 연결 (이미 업로드된 파일들)
        List<Attachment> savedAttachments = null;
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            List<Attachment> attachments = attachmentRepository.findAllById(request.getAttachmentIds());
            
            for (Attachment attachment : attachments) {
                attachment.attachToPost(savedPost);
            }
            
            savedAttachments = attachmentRepository.saveAll(attachments);
            
            log.info("게시글에 첨부파일 연결: postId={}, attachmentCount={}", 
                    savedPost.getId(), attachments.size());
        }
        
        log.info("게시글 생성 완료: postId={}", savedPost.getId());

        // 8. 응답 생성 (첨부파일 포함)
        PostResponseDto response = PostResponseDto.from(savedPost);
        
        // 첨부파일이 있으면 수동으로 추가 (지연 로딩 문제 해결)
        if (savedAttachments != null && !savedAttachments.isEmpty()) {
            // PostResponseDto가 attachments 필드를 지원한다면 여기서 설정
            // 현재는 PostResponseDto.from()이 자동으로 처리할 것으로 예상
        }
        
        return response;
    }

    /**
     * 게시글 목록 조회 (boardType 기반, 해시태그 필터링 지원)
     */
    public Page<PostListResponseDto> getPostListByBoardType(String boardTypeStr, String search, String hashtag, Pageable pageable) {
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

        // 3. 해시태그 필터링 여부에 따라 분기
        Page<Post> posts;
        
        if (hashtag != null && !hashtag.isBlank()) {
            // 해시태그 + 검색어 조합
            if (search != null && !search.isBlank()) {
                posts = postRepository.findByCategoryAndTitleContainingAndHashtagName(category, search, hashtag, pageable);
                log.info("게시글 목록 조회 (해시태그+검색): boardType={}, hashtag={}, search={}, resultCount={}", 
                        boardType, hashtag, search, posts.getTotalElements());
            } else {
                // 해시태그만
                posts = postRepository.findByCategoryAndHashtagName(category, hashtag, pageable);
                log.info("게시글 목록 조회 (해시태그): boardType={}, hashtag={}, resultCount={}", 
                        boardType, hashtag, posts.getTotalElements());
            }
        } else {
            // 기존 로직 (검색어만 또는 전체)
            if (search != null && !search.isBlank()) {
                posts = postRepository.findByCategoryAndTitleContaining(category, search, pageable);
            } else {
                posts = postRepository.findByCategory(category, pageable);
            }
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

        // 3. 새 첨부파일 추가 (두 가지 방식 지원)
        // 3-1. MultipartFile로 직접 업로드 (Multipart 방식)
        if (files != null && !files.isEmpty()) {
            fileStorageService.uploadFiles(post, files);
            log.info("첨부파일 {}개 추가 완료 (MultipartFile)", files.size());
        }
        
        // 3-2. 미리 업로드된 첨부파일 ID로 연결 (JSON 방식)
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            List<Attachment> attachments = attachmentRepository.findAllById(request.getAttachmentIds());
            for (Attachment attachment : attachments) {
                attachment.attachToPost(post);
            }
            log.info("첨부파일 {}개 추가 완료 (AttachmentIds)", attachments.size());
        }

        // 4. 해시태그 업데이트
        if (request.getHashtags() != null) {
            hashtagService.updatePostHashtags(post, request.getHashtags(), post.getAuthorId());
            log.info("게시글 해시태그 업데이트: postId={}, hashtagCount={}", 
                    post.getId(), request.getHashtags().size());
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
    
    /**
     * 역할 기반 게시판 접근 권한 검증 (RBAC)
     * 
     * TODO: 향후 개선 사항
     * 1. UserTypeMapping 리포지토리 주입하여 정확한 사용자 역할 조회
     * 2. Spring Security @PreAuthorize 어노테이션과 통합
     * 3. ADMIN 역할은 모든 게시판 접근 가능하도록 확장
     * 4. JWT 토큰에서 역할 정보 추출하여 사용 (현재는 userId로만 판별)
     */
    private void validateBoardAccess(BoardType boardType, Long userId) {
        // 역할 제한이 없는 게시판은 모두 접근 가능
        if (!boardType.isRoleRestrictedBoard()) {
            return;
        }
        
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        
        // TODO: 임시 구현 - determineUserType()을 UserTypeMapping 서비스로 대체 필요
        String userType = determineUserType(userId);
        
        // 교수 게시판 접근 제어
        if (boardType == BoardType.PROFESSOR && !"PROFESSOR".equals(userType)) {
            log.warn("교수 게시판 접근 거부: userId={}, userType={}", userId, userType);
            throw new BoardException(BoardErrorCode.PROFESSOR_ONLY_BOARD);
        }
        
        // 학생 게시판 접근 제어
        if (boardType == BoardType.STUDENT && !"STUDENT".equals(userType)) {
            log.warn("학생 게시판 접근 거부: userId={}, userType={}", userId, userType);
            throw new BoardException(BoardErrorCode.STUDENT_ONLY_BOARD);
        }
        
        log.debug("게시판 접근 권한 확인 완료: boardType={}, userId={}, userType={}", boardType, userId, userType);
    }
    
    /**
     * 사용자 타입 판별 (STUDENT 또는 PROFESSOR)
     * 
     * TODO: 임시 구현 - 반드시 개선 필요
     * 현재 방식: userId 범위로 임시 구분
     * 개선 방안:
     * 1. UserTypeMapping 테이블 조회
     *    SELECT utm.user_type_id FROM user_type_mappings utm WHERE utm.user_id = ?
     * 2. UserType 테이블과 조인하여 type_code 확인
     * 3. 캐싱 적용 (Redis 또는 로컬 캐시)
     * 
     * 예시 코드:
     * userTypeMappingRepository.findByUserId(userId)
     *     .map(mapping -> mapping.getUserType().getTypeCode())
     *     .orElse("STUDENT");
     */
    private String determineUserType(Long userId) {
        // 임시 구현: ID 범위로 구분 (20241xxx = 학생, 20242xxx = 교수)
        if (userId >= 20241000 && userId < 20242000) {
            return "STUDENT";
        } else if (userId >= 20242000 && userId < 20243000) {
            return "PROFESSOR";
        }
        
        // 기본값은 학생으로 처리
        return "STUDENT";
    }
}
