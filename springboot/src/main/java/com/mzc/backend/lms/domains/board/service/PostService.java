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
import com.mzc.backend.lms.domains.board.enums.PostType;
import com.mzc.backend.lms.domains.board.exception.BoardErrorCode;
import com.mzc.backend.lms.domains.board.exception.BoardException;
import com.mzc.backend.lms.domains.board.repository.AttachmentRepository;
import com.mzc.backend.lms.domains.board.repository.BoardCategoryRepository;
import com.mzc.backend.lms.domains.board.repository.PostLikeRepository;
import com.mzc.backend.lms.domains.board.repository.PostRepository;
import com.mzc.backend.lms.domains.board.repository.UserTypeQueryRepository;
import com.mzc.backend.lms.domains.user.profile.dto.UserBasicInfoDto;
import com.mzc.backend.lms.domains.user.profile.service.UserInfoCacheService;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import com.mzc.backend.lms.util.file.FileUploadUtils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Set;

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
    private final UserTypeQueryRepository userTypeQueryRepository;
    private final UserInfoCacheService userInfoCacheService;
    private final FileUploadUtils fileStorageService;
    private final AttachmentRepository attachmentRepository;
    private final HashtagService hashtagService;
    private final EntityManager entityManager;
    private final com.mzc.backend.lms.domains.user.student.repository.StudentDepartmentRepository studentDepartmentRepository;

    /**
     * 게시글 생성 (boardType 기반, 2단계 업로드)
     */
    @Transactional
    public PostResponseDto createPost(String boardTypeStr, PostCreateRequestDto request, Long authorId) {
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
        validateBoardAccess(boardType, authorId);

        // 2-2. 게시글 유형 검증 (BoardType과 PostType 조합 확인)
        if (!boardType.isPostTypeAllowed(request.getPostType())) {
            throw new BoardException(BoardErrorCode.INVALID_POST_TYPE_FOR_BOARD);
        }

        // 3. 카테고리 정책 검증
        validateCategoryPolicy(category, request.getIsAnonymous());

        // 3-1. DEPARTMENT 게시판인 경우 사용자 학과 ID 설정
        Long departmentId = null;
        if (boardType == BoardType.DEPARTMENT) {
            departmentId = getUserDepartmentId(authorId);
            if (departmentId == null) {
                log.warn("학과 정보 없는 사용자가 학과 게시판에 글 작성 시도: userId={}", authorId);
                throw new BoardException(BoardErrorCode.UNAUTHORIZED_ACTION);
            }
            log.info("학과 게시글 생성: userId={}, departmentId={}", authorId, departmentId);
        }

        // 4. 게시글 생성
        Post post = Post.builder()
                .category(category)
                .title(request.getTitle())
                .content(request.getContent())
                .postType(request.getPostType())
                .isAnonymous(request.getIsAnonymous())
                .authorId(authorId)
                .departmentId(departmentId)
                .build();

        // 5. 저장
        Post savedPost = postRepository.save(post);
        
        // 6. 해시태그 처리
        if (request.getHashtags() != null && !request.getHashtags().isEmpty()) {
            hashtagService.attachHashtagsToPost(savedPost, request.getHashtags(), authorId);
            log.info("게시글에 해시태그 연결: postId={}, hashtagCount={}", 
                    savedPost.getId(), request.getHashtags().size());
        }
        
        // 7. 첨부파일 연결 (이미 업로드된 파일들)
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            List<Attachment> attachments = attachmentRepository.findAllById(request.getAttachmentIds());
            
            for (Attachment attachment : attachments) {
                attachment.attachToPost(savedPost);
            }
            
            attachmentRepository.saveAll(attachments);
            
            log.info("게시글에 첨부파일 연결: postId={}, attachmentCount={}", 
                    savedPost.getId(), attachments.size());
        }
        
        log.info("게시글 생성 완료: postId={}", savedPost.getId());

        // 8. 영속성 컨텍스트를 플러시하고 엔티티 새로고침
        entityManager.flush();   // DB에 반영
        entityManager.refresh(savedPost);  // 엔티티 다시 로드 (첨부파일, 해시태그 포함)

        // 9. 응답 생성 - 작성자 이름 추가
        PostResponseDto response = PostResponseDto.from(savedPost);
        enrichWithUserInfo(response);
        
        return response;
    }

    /**
     * 게시글 목록 조회 (boardType 기반, 해시태그 필터링 지원)
     */
    public Page<PostListResponseDto> getPostListByBoardType(String boardTypeStr, String search, String hashtag, Pageable pageable, Long currentUserId) {
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

        // 2-1. DEPARTMENT 게시판인 경우 사용자 학과로 자동 필터링
        Long userDepartmentId = null;
        if (boardType == BoardType.DEPARTMENT && currentUserId != null) {
            userDepartmentId = getUserDepartmentId(currentUserId);
            if (userDepartmentId != null) {
                log.info("학과 게시판 자동 필터링: userId={}, departmentId={}", currentUserId, userDepartmentId);
            }
        }

        // 3. 학과 ID 또는 해시태그 필터링 여부에 따라 분기
        Page<Post> posts;
        
        if (userDepartmentId != null) {
            // 학과 ID로 필터링 (DEPARTMENT 게시판)
            if (search != null && !search.isBlank()) {
                posts = postRepository.findByCategoryAndDepartmentIdAndTitleContaining(category, userDepartmentId, search, pageable);
                log.info("게시글 목록 조회 (학과+검색): boardType={}, departmentId={}, search={}, resultCount={}", 
                        boardType, userDepartmentId, search, posts.getTotalElements());
            } else {
                posts = postRepository.findByCategoryAndDepartmentId(category, userDepartmentId, pageable);
                log.info("게시글 목록 조회 (학과): boardType={}, departmentId={}, resultCount={}", 
                        boardType, userDepartmentId, posts.getTotalElements());
            }
        } else if (hashtag != null && !hashtag.isBlank()) {
            // 해시태그로 필터링 (다른 게시판)
            if (search != null && !search.isBlank()) {
                posts = postRepository.findByCategoryAndTitleContainingAndHashtagName(category, search, hashtag, pageable);
                log.info("게시글 목록 조회 (해시태그+검색): boardType={}, hashtag={}, search={}, resultCount={}", 
                        boardType, hashtag, search, posts.getTotalElements());
            } else {
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

        // 4. DTO 변환 및 사용자 이름 추가
        Page<PostListResponseDto> result = posts.map(PostListResponseDto::from);
        
        // 5. 모든 게시글의 작성자 ID 수집
        Set<Long> creatorIds = result.getContent().stream()
                .map(PostListResponseDto::getCreatedBy)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        
        // 6. 사용자 정보 일괄 조회
        if (!creatorIds.isEmpty()) {
            Map<Long, UserBasicInfoDto> userInfoMap = userInfoCacheService.getUserInfoMap(creatorIds);
            
            // 7. 각 게시글에 작성자 이름 설정
            result.getContent().forEach(dto -> {
                if (dto.getCreatedBy() != null) {
                    UserBasicInfoDto userInfo = userInfoMap.get(dto.getCreatedBy());
                    if (userInfo != null && userInfo.getName() != null) {
                        dto.setCreatedByName(userInfo.getName());
                    }
                }
            });
        }
        
        return result;
    }

    /**
     * 게시글 상세 조회 (조회수 증가)
     * 
     * 접근 제어는 SecurityConfig에서 URL 패턴 기반으로 처리됩니다:
     * - /api/v1/boards/PROFESSOR/** → 교수만 접근 가능 (hasAuthority("PROFESSOR"))
     * - /api/v1/boards/STUDENT/** → 학생만 접근 가능 (hasAuthority("STUDENT"))
     * 
     * @param boardTypeStr 게시판 타입 문자열 (예: "PROFESSOR", "STUDENT")
     * @param postId 게시글 ID
     * @param currentUserId 현재 로그인한 사용자 ID (학과 검증용)
     * @return 게시글 상세 정보
     */
    @Transactional
    public PostResponseDto getPost(String boardTypeStr, Long postId, Long currentUserId) {
        Post post = postRepository.findByIdWithHashtags(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        // soft delete 체크
        if (post.isDeleted()) {
            throw new BoardException(BoardErrorCode.POST_ALREADY_DELETED);
        }

        // 게시판 타입 검증
        BoardType boardType;
        try {
            boardType = BoardType.valueOf(boardTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BoardException(BoardErrorCode.BOARD_NOT_FOUND);
        }

        // 게시글이 해당 게시판에 속하는지 확인
        if (post.getCategory().getBoardType() != boardType) {
            throw new BoardException(BoardErrorCode.POST_NOT_FOUND);
        }

        // DEPARTMENT 게시판인 경우 학과 권한 체크
        if (boardType == BoardType.DEPARTMENT && currentUserId != null) {
            Long userDepartmentId = getUserDepartmentId(currentUserId);
            if (userDepartmentId != null && post.getDepartmentId() != null) {
                // 게시글의 department_id와 사용자 학과 비교
                boolean hasAccess = post.getDepartmentId().equals(userDepartmentId);
                
                if (!hasAccess) {
                    log.warn("학과 게시글 접근 권한 없음: userId={}, postId={}, userDeptId={}, postDeptId={}", 
                            currentUserId, postId, userDepartmentId, post.getDepartmentId());
                    throw new BoardException(BoardErrorCode.UNAUTHORIZED_ACTION);
                }
            }
        }

        // 조회수 증가
        post.increaseViewCount();

        PostResponseDto response = PostResponseDto.from(post);
        enrichWithUserInfo(response);
        return response;
    }

    /**
     * 게시글 상세 조회 (조회수 증가) - 레거시 메서드
     * @deprecated boardType을 받는 getPost(String, Long) 사용 권장
     */
    @Deprecated
    @Transactional
    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.findByIdWithHashtags(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        // soft delete 체크
        if (post.isDeleted()) {
            throw new BoardException(BoardErrorCode.POST_ALREADY_DELETED);
        }

        // 조회수 증가
        post.increaseViewCount();

        PostResponseDto response = PostResponseDto.from(post);
        enrichWithUserInfo(response);
        return response;
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
    public PostResponseDto updatePost(Long postId, PostUpdateRequestDto request, List<MultipartFile> files, Long updatedBy) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        if (post.isDeleted()) {
            throw new BoardException(BoardErrorCode.POST_ALREADY_DELETED);
        }

        // 0. 게시글 유형 검증 (수정 시에도 BoardType과 PostType 조합 확인)
        BoardType boardType = post.getCategory().getBoardType();
        if (request.getPostType() != null) {
            try {
                PostType newPostType = PostType.valueOf(request.getPostType());
                if (!boardType.isPostTypeAllowed(newPostType)) {
                    throw new BoardException(BoardErrorCode.INVALID_POST_TYPE_FOR_BOARD);
                }
            } catch (IllegalArgumentException e) {
                throw new BoardException(BoardErrorCode.INVALID_POST_TYPE_FOR_BOARD);
            }
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

        // 4. 해시태그 업데이트 (항상 실행 - 빈 배열이면 모든 해시태그 삭제)
        hashtagService.updatePostHashtags(post, request.getHashtags(), post.getAuthorId());
        log.info("게시글 해시태그 업데이트: postId={}, hashtagCount={}", 
                post.getId(), request.getHashtags() != null ? request.getHashtags().size() : 0);

        PostResponseDto response = PostResponseDto.from(post);
        enrichWithUserInfo(response);
        return response;
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
     * 향후 개선 고려사항:
     * - TODO: ADMIN 역할에 대한 모든 게시판 접근 권한 추가
     *   (UserRole enum에 ADMIN은 정의되어 있으나 이 메서드에서는 아직 활용되지 않음)
     * - TODO: JWT 토큰에서 userType 직접 추출하여 DB 조회 최소화
     *   (JWT claims에 userType이 포함되어 있으나 현재는 캐시/DB 조회 방식 사용 중)
     * 
     * 참고: SecurityConfig에서 이미 URL 기반 접근 제어가 작동하므로,
     *       이 메서드는 게시글 작성 시에만 사용됩니다.
     */
    private void validateBoardAccess(BoardType boardType, Long userId) {
        // 역할 제한이 없는 게시판은 모두 접근 가능
        if (!boardType.isRoleRestrictedBoard()) {
            return;
        }
        
        String userType = determineUserType(userId);
        log.info("게시판 접근 권한 검증: boardType={}, userId={}, userType={}", boardType, userId, userType);
        
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
     * UserInfoCacheService를 통해 캐시된 사용자 정보에서 타입을 조회합니다.
     * Redis 캐시를 활용하여 성능을 최적화합니다.
     */
    private String determineUserType(Long userId) {
        try {
            Map<Long, UserBasicInfoDto> userInfoMap = userInfoCacheService.getUserInfoMap(Set.of(userId));
            UserBasicInfoDto userInfo = userInfoMap.get(userId);
            
            if (userInfo != null && userInfo.getUserType() != null) {
                log.info("사용자 타입 조회 성공 (캐시): userId={}, userType={}", userId, userInfo.getUserType());
                return userInfo.getUserType();
            }
        } catch (Exception e) {
            log.warn("UserInfoCacheService 조회 실패, UserTypeQueryRepository 사용: userId={}", userId, e);
        }
        
        // Fallback: UserTypeQueryRepository 사용
        Optional<String> userTypeOpt = userTypeQueryRepository.findUserTypeCodeByUserId(userId);
        String userType = userTypeOpt.orElseGet(() -> {
            log.warn("사용자 타입 매핑이 없습니다. 기본값(STUDENT) 반환: userId={}", userId);
            return "STUDENT";
        });
        log.info("사용자 타입 조회 결과 (fallback): userId={}, userType={}", userId, userType);
        return userType;
    }

    /**
     * PostResponseDto에 사용자 정보(이름) 추가
     */
    private void enrichWithUserInfo(PostResponseDto response) {
        if (response.getCreatedBy() != null) {
            try {
                log.info("사용자 정보 조회 시작: userId={}", response.getCreatedBy());
                Map<Long, UserBasicInfoDto> userInfoMap = userInfoCacheService.getUserInfoMap(Set.of(response.getCreatedBy()));
                UserBasicInfoDto userInfo = userInfoMap.get(response.getCreatedBy());
                
                log.info("조회된 사용자 정보: userId={}, userInfo={}", response.getCreatedBy(), userInfo);
                
                if (userInfo != null && userInfo.getName() != null) {
                    response.setCreatedByName(userInfo.getName());
                    log.info("게시글 작성자 이름 설정 완료: postId={}, createdBy={}, name={}", 
                            response.getId(), response.getCreatedBy(), userInfo.getName());
                } else {
                    log.warn("사용자 정보가 없거나 이름이 null: userId={}, userInfo={}", response.getCreatedBy(), userInfo);
                }
            } catch (Exception e) {
                log.error("사용자 이름 조회 실패: userId={}", response.getCreatedBy(), e);
            }
        } else {
            log.warn("createdBy가 null입니다: postId={}", response.getId());
        }
    }

    /**
     * 사용자의 학과 ID 조회
     * @param userId 사용자 ID (학번)
     * @return 학과 ID (없으면 null)
     */
    private Long getUserDepartmentId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            return studentDepartmentRepository.findByStudentId(userId)
                    .map(sd -> sd.getDepartment().getId())
                    .orElse(null);
        } catch (Exception e) {
            log.warn("학과 정보 조회 실패: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 사용자의 학과 이름 조회
     * @param userId 사용자 ID (학번 또는 교번)
     * @return 학과 이름 (없으면 null)
     */
    private String getUserDepartmentName(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            return studentDepartmentRepository.findByStudentId(userId)
                    .map(sd -> sd.getDepartment().getDepartmentName())
                    .orElse(null);
        } catch (Exception e) {
            log.warn("학과 정보 조회 실패: userId={}", userId, e);
            return null;
        }
    }
}
