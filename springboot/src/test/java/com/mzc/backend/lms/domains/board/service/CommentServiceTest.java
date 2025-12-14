package com.mzc.backend.lms.domains.board.service;

import com.mzc.backend.lms.domains.board.dto.request.CommentCreateRequestDto;
import com.mzc.backend.lms.domains.board.dto.request.CommentUpdateRequestDto;
import com.mzc.backend.lms.domains.board.dto.request.PostCreateRequestDto;
import com.mzc.backend.lms.domains.board.dto.response.AttachmentResponseDto;
import com.mzc.backend.lms.domains.board.dto.response.CommentResponseDto;
import com.mzc.backend.lms.domains.board.dto.response.PostResponseDto;
import com.mzc.backend.lms.domains.board.entity.Attachment;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.enums.AttachmentType;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.enums.PostType;
import com.mzc.backend.lms.domains.board.repository.AttachmentRepository;
import com.mzc.backend.lms.domains.board.repository.BoardCategoryRepository;
import com.mzc.backend.lms.domains.board.repository.CommentRepository;
import com.mzc.backend.lms.domains.board.repository.PostRepository;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("CommentService 통합 테스트")
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BoardCategoryRepository boardCategoryRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private UserRepository userRepository;

    private BoardCategory testCategory;
    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        // 기존 카테고리 조회 또는 생성
        testCategory = boardCategoryRepository.findByBoardType(BoardType.NOTICE)
                .orElseGet(() -> {
                    BoardCategory category = new BoardCategory(BoardType.NOTICE, true, true, true);
                    return boardCategoryRepository.save(category);
                });
        
        // 테스트 유저 생성 (이미 존재하면 조회)
        testUser = userRepository.findById(20250101003L)
                .orElseGet(() -> {
                    User user = User.create(20250101003L, "test@test.com", "password");
                    return userRepository.save(user);
                });

        // 테스트용 게시글 생성
        PostCreateRequestDto postRequest = PostCreateRequestDto.builder()
                .categoryId(testCategory.getId())
                .postType(PostType.NOTICE)
                .title("테스트 게시글")
                .content("댓글 테스트를 위한 게시글입니다.")
                .authorId(testUser.getId())
                .isAnonymous(false)
                .build();
        PostResponseDto postResponse = postService.createPost("NOTICE", postRequest);
        testPost = postRepository.findById(postResponse.getId()).orElseThrow();
    }

    @Test
    @DisplayName("댓글 생성 성공")
    @Rollback(false)
    void createComment_Success() {
        // given
        CommentCreateRequestDto request = CommentCreateRequestDto.builder()
                .postId(testPost.getId())
                .authorId(testUser.getId())
                .content("테스트 댓글입니다.")
                .build();

        // when
        CommentResponseDto response = commentService.createComment(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("테스트 댓글입니다.");
        assertThat(response.getPostId()).isEqualTo(testPost.getId());
        assertThat(response.getDepth()).isEqualTo(0);

        log.info("생성된 댓글 ID: {}", response.getId());
    }

    @Test
    @DisplayName("첨부파일이 있는 댓글 생성 성공")
    @Rollback(false)
    void createCommentWithAttachments_Success() {
        // given
        // 테스트용 첨부파일 생성
        Attachment attachment1 = Attachment.builder()
                .originalName("test1.txt")
                .storedName("stored_test1.txt")
                .filePath("/uploads/test1.txt")
                .fileSize(1024L)
                .attachmentType(AttachmentType.OTHER)
                .build();
        Attachment attachment2 = Attachment.builder()
                .originalName("test2.txt")
                .storedName("stored_test2.txt")
                .filePath("/uploads/test2.txt")
                .fileSize(2048L)
                .attachmentType(AttachmentType.OTHER)
                .build();
        
        Attachment savedAttachment1 = attachmentRepository.save(attachment1);
        Attachment savedAttachment2 = attachmentRepository.save(attachment2);

        List<Long> attachmentIds = new ArrayList<>();
        attachmentIds.add(savedAttachment1.getId());
        attachmentIds.add(savedAttachment2.getId());

        CommentCreateRequestDto request = CommentCreateRequestDto.builder()
                .postId(testPost.getId())
                .authorId(testUser.getId())
                .content("첨부파일이 있는 댓글입니다.")
                .attachmentIds(attachmentIds)
                .build();

        // when
        CommentResponseDto response = commentService.createComment(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("첨부파일이 있는 댓글입니다.");
        assertThat(response.getAttachments()).hasSize(2);
        assertThat(response.getAttachments().get(0).getOriginalName()).isEqualTo("test1.txt");
        assertThat(response.getAttachments().get(1).getOriginalName()).isEqualTo("test2.txt");

        log.info("생성된 댓글 ID: {}, 첨부파일 수: {}", response.getId(), response.getAttachments().size());
    }

    @Test
    @DisplayName("댓글 수정 - 내용만 수정")
    @Rollback(false)
    void updateCommentContent_Success() {
        // given
        CommentCreateRequestDto createRequest = CommentCreateRequestDto.builder()
                .postId(testPost.getId())
                .authorId(testUser.getId())
                .content("원본 댓글 내용")
                .build();
        CommentResponseDto createdComment = commentService.createComment(createRequest);

        CommentUpdateRequestDto updateRequest = CommentUpdateRequestDto.builder()
                .content("수정된 댓글 내용")
                .build();

        // when
        CommentResponseDto response = commentService.updateComment(createdComment.getId(), updateRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("수정된 댓글 내용");
        assertThat(response.getId()).isEqualTo(createdComment.getId());

        log.info("댓글 수정 완료: {}", response.getId());
    }

    @Test
    @DisplayName("댓글 수정 - 첨부파일 추가")
    @Rollback(false)
    void updateCommentAddAttachments_Success() {
        // given - 첨부파일 없이 댓글 생성
        CommentCreateRequestDto createRequest = CommentCreateRequestDto.builder()
                .postId(testPost.getId())
                .authorId(testUser.getId())
                .content("첨부파일 없는 댓글")
                .build();
        CommentResponseDto createdComment = commentService.createComment(createRequest);

        // 새 첨부파일 생성
        Attachment newAttachment = Attachment.builder()
                .originalName("new_file.txt")
                .storedName("stored_new_file.txt")
                .filePath("/uploads/new_file.txt")
                .fileSize(3072L)
                .attachmentType(AttachmentType.OTHER)
                .build();
        Attachment savedAttachment = attachmentRepository.save(newAttachment);

        List<Long> newAttachmentIds = new ArrayList<>();
        newAttachmentIds.add(savedAttachment.getId());

        CommentUpdateRequestDto updateRequest = CommentUpdateRequestDto.builder()
                .content("첨부파일이 추가된 댓글")
                .attachmentIds(newAttachmentIds)
                .build();

        // when
        CommentResponseDto response = commentService.updateComment(createdComment.getId(), updateRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("첨부파일이 추가된 댓글");
        assertThat(response.getAttachments()).hasSize(1);
        assertThat(response.getAttachments().get(0).getOriginalName()).isEqualTo("new_file.txt");

        log.info("댓글에 첨부파일 추가 완료: commentId={}, attachmentCount={}", 
                response.getId(), response.getAttachments().size());
    }

    @Test
    @DisplayName("댓글 수정 - 기존 첨부파일 삭제하고 새 파일 추가")
    @Rollback(false)
    void updateCommentReplaceAttachments_Success() {
        // given - 첨부파일과 함께 댓글 생성
        Attachment oldAttachment = Attachment.builder()
                .originalName("old_file.txt")
                .storedName("stored_old_file.txt")
                .filePath("/uploads/old_file.txt")
                .fileSize(1024L)
                .attachmentType(AttachmentType.OTHER)
                .build();
        Attachment savedOldAttachment = attachmentRepository.save(oldAttachment);

        List<Long> oldAttachmentIds = new ArrayList<>();
        oldAttachmentIds.add(savedOldAttachment.getId());

        CommentCreateRequestDto createRequest = CommentCreateRequestDto.builder()
                .postId(testPost.getId())
                .authorId(testUser.getId())
                .content("기존 첨부파일이 있는 댓글")
                .attachmentIds(oldAttachmentIds)
                .build();
        CommentResponseDto createdComment = commentService.createComment(createRequest);

        // 새 첨부파일 생성
        Attachment newAttachment = Attachment.builder()
                .originalName("new_replacement_file.txt")
                .storedName("stored_new_replacement_file.txt")
                .filePath("/uploads/new_replacement_file.txt")
                .fileSize(2048L)
                .attachmentType(AttachmentType.OTHER)
                .build();
        Attachment savedNewAttachment = attachmentRepository.save(newAttachment);

        List<Long> newAttachmentIds = new ArrayList<>();
        newAttachmentIds.add(savedNewAttachment.getId());

        List<Long> removedAttachmentIds = new ArrayList<>();
        removedAttachmentIds.add(savedOldAttachment.getId());

        CommentUpdateRequestDto updateRequest = CommentUpdateRequestDto.builder()
                .content("첨부파일이 교체된 댓글")
                .attachmentIds(newAttachmentIds)
                .removedAttachmentIds(removedAttachmentIds)
                .build();

        // when
        CommentResponseDto response = commentService.updateComment(createdComment.getId(), updateRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("첨부파일이 교체된 댓글");
        assertThat(response.getAttachments()).hasSize(1);
        assertThat(response.getAttachments().get(0).getOriginalName()).isEqualTo("new_replacement_file.txt");

        // 기존 첨부파일이 삭제되었는지 확인
        boolean oldAttachmentExists = attachmentRepository.existsById(savedOldAttachment.getId());
        assertThat(oldAttachmentExists).isFalse();

        log.info("댓글 첨부파일 교체 완료: commentId={}, 새 파일={}", 
                response.getId(), response.getAttachments().get(0).getOriginalName());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    @Rollback(false)
    void deleteComment_Success() {
        // given
        CommentCreateRequestDto createRequest = CommentCreateRequestDto.builder()
                .postId(testPost.getId())
                .authorId(testUser.getId())
                .content("삭제될 댓글")
                .build();
        CommentResponseDto createdComment = commentService.createComment(createRequest);

        // when
        commentService.deleteComment(createdComment.getId());

        // then
        // 삭제된 댓글은 조회할 수 없어야 함
        assertThatThrownBy(() -> {
            commentService.updateComment(createdComment.getId(), 
                    CommentUpdateRequestDto.builder().content("수정 시도").build());
        }).isInstanceOf(Exception.class);

        log.info("댓글 삭제 완료: {}", createdComment.getId());
    }

    @Test
    @DisplayName("대댓글 생성 성공")
    @Rollback(false)
    void createReply_Success() {
        // given - 부모 댓글 생성
        CommentCreateRequestDto parentRequest = CommentCreateRequestDto.builder()
                .postId(testPost.getId())
                .authorId(testUser.getId())
                .content("부모 댓글")
                .build();
        CommentResponseDto parentComment = commentService.createComment(parentRequest);

        // 대댓글 생성
        CommentCreateRequestDto replyRequest = CommentCreateRequestDto.builder()
                .postId(testPost.getId())
                .authorId(testUser.getId())
                .content("대댓글입니다.")
                .parentCommentId(parentComment.getId())
                .build();

        // when
        CommentResponseDto response = commentService.createComment(replyRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("대댓글입니다.");
        assertThat(response.getParentCommentId()).isEqualTo(parentComment.getId());
        assertThat(response.getDepth()).isEqualTo(1);

        log.info("대댓글 생성 완료: parentId={}, replyId={}", parentComment.getId(), response.getId());
    }

    @Test
    @DisplayName("게시글의 모든 댓글 조회")
    @Rollback(false)
    void getCommentsByPost_Success() {
        // given - 여러 댓글 생성
        for (int i = 1; i <= 3; i++) {
            CommentCreateRequestDto request = CommentCreateRequestDto.builder()
                    .postId(testPost.getId())
                    .authorId(testUser.getId())
                    .content("댓글 " + i)
                    .build();
            commentService.createComment(request);
        }

        // when
        List<CommentResponseDto> comments = commentService.getCommentsByPost(testPost.getId());

        // then
        assertThat(comments).hasSizeGreaterThanOrEqualTo(3);
        
        log.info("조회된 댓글 수: {}", comments.size());
        comments.forEach(comment -> log.info("댓글: {}", comment.getContent()));
    }
}
