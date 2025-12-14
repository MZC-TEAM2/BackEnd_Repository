package com.mzc.backend.lms.domains.board.repository;

import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Comment;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.enums.PostType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import com.mzc.backend.lms.common.config.JpaConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(JpaConfig.class)
class BoardRepositoryTest {

    @Autowired
    private BoardCategoryRepository boardCategoryRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @Rollback(false)
    @DisplayName("게시판-게시글-댓글 생명주기 테스트")
    void testBoardLifecycle() {
        // 1. BoardCategory (자유게시판) 생성 및 저장
        BoardCategory freeBoard = new BoardCategory(
                BoardType.FREE,
                true,  // 댓글 허용
                true,  // 첨부파일 허용
                true   // 익명 허용
        );
        BoardCategory savedCategory = boardCategoryRepository.save(freeBoard);

        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getBoardType()).isEqualTo(BoardType.FREE);
        log.info("✅ BoardCategory 저장 성공: ID={}, Type={}", savedCategory.getId(), savedCategory.getBoardType());

        // 2. Post (게시글) 작성 및 저장
        Post post = Post.builder()
                .category(savedCategory)
                .title("첫 번째 게시글입니다")
                .content("안녕하세요, 반갑습니다.")
                .postType(PostType.NORMAL)
                .isAnonymous(false)
                .build();
        
        Post savedPost = postRepository.save(post);

        assertThat(savedPost.getId()).isNotNull();
        assertThat(savedPost.getTitle()).isEqualTo("첫 번째 게시글입니다");
        assertThat(savedPost.getCategory().getBoardType()).isEqualTo(BoardType.FREE);
        log.info("✅ Post 저장 성공: ID={}, Title={}", savedPost.getId(), savedPost.getTitle());

        // 3. Comment (댓글) 작성 및 저장
        Comment comment = Comment.builder()
                .post(savedPost)
                .parentComment(null) // 원댓글
                .content("좋은 글이네요!")
                .build();
        
        Comment savedComment = commentRepository.save(comment);

        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getContent()).isEqualTo("좋은 글이네요!");
        assertThat(savedComment.getPost().getId()).isEqualTo(savedPost.getId());
        log.info("✅ Comment 저장 성공: ID={}, Content={}", savedComment.getId(), savedComment.getContent());

        // 4. 조회 검증
        // 카테고리로 게시글 조회
        List<Post> postsInCategory = postRepository.findByCategory(savedCategory);
        assertThat(postsInCategory).hasSize(1);
        assertThat(postsInCategory.get(0).getTitle()).isEqualTo("첫 번째 게시글입니다");
        log.info("✅ Category로 Post 조회 성공: {} 건", postsInCategory.size());

        // 게시글로 댓글 조회
        List<Comment> commentsInPost = commentRepository.findByPost(savedPost);
        assertThat(commentsInPost).hasSize(1);
        assertThat(commentsInPost.get(0).getContent()).isEqualTo("좋은 글이네요!");
        log.info("✅ Post로 Comment 조회 성공: {} 건", commentsInPost.size());
        
        log.info("🎉 모든 테스트 통과!");
    }
}
