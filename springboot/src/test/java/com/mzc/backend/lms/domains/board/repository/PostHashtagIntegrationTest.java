package com.mzc.backend.lms.domains.board.repository;

import com.mzc.backend.lms.common.config.JpaConfig;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Hashtag;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.entity.PostHashtag;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.enums.PostType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Post-Hashtag 통합 테스트
 * Post 엔티티의 해시태그 관련 메서드 및 영속성 컨텍스트 동작 검증
 */
@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(JpaConfig.class)
@DisplayName("Post-Hashtag 통합 테스트")
class PostHashtagIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BoardCategoryRepository boardCategoryRepository;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Test
    @DisplayName("Post.addHashtag() - 해시태그 추가 및 양방향 관계 설정")
    void testAddHashtagToPost() {
        // given
        Long userId = 20241001L;
        
        BoardCategory category = new BoardCategory(BoardType.FREE, true, true, true);
        boardCategoryRepository.save(category);
        
        Post post = Post.builder()
                .category(category)
                .title("해시태그 테스트 게시글")
                .content("내용")
                .postType(PostType.NORMAL)
                .authorId(userId)
                .build();
        postRepository.save(post);
        
        Hashtag hashtag1 = Hashtag.builder()
                .name("java")
                .displayName("Java")
                .createdBy(userId)
                .build();
        hashtagRepository.save(hashtag1);
        
        Hashtag hashtag2 = Hashtag.builder()
                .name("spring")
                .displayName("Spring")
                .createdBy(userId)
                .build();
        hashtagRepository.save(hashtag2);

        // when
        post.addHashtag(hashtag1, userId);
        post.addHashtag(hashtag2, userId);
        postRepository.save(post);

        // then
        assertThat(post.getPostHashtags()).hasSize(2);
        assertThat(post.getPostHashtags())
                .extracting(ph -> ph.getHashtag().getName())
                .containsExactlyInAnyOrder("java", "spring");
        
        log.info("✅ Post에 해시태그 2개 추가 성공: {}", 
                post.getPostHashtags().stream()
                        .map(ph -> ph.getHashtag().getDisplayName())
                        .toList());
    }

    @Test
    @DisplayName("Post.clearHashtags() - 모든 해시태그 제거")
    void testClearHashtags() {
        // given
        Long userId = 20241001L;
        
        BoardCategory category = new BoardCategory(BoardType.QUESTION, true, true, true);
        boardCategoryRepository.save(category);
        
        Post post = Post.builder()
                .category(category)
                .title("해시태그 제거 테스트")
                .content("내용")
                .postType(PostType.NORMAL)
                .authorId(userId)
                .build();
        
        Hashtag hashtag1 = Hashtag.builder()
                .name("python")
                .displayName("Python")
                .createdBy(userId)
                .build();
        hashtagRepository.save(hashtag1);
        
        Hashtag hashtag2 = Hashtag.builder()
                .name("django")
                .displayName("Django")
                .createdBy(userId)
                .build();
        hashtagRepository.save(hashtag2);
        
        post.addHashtag(hashtag1, userId);
        post.addHashtag(hashtag2, userId);
        postRepository.save(post);
        
        assertThat(post.getPostHashtags()).hasSize(2); // 사전 확인

        // when
        post.clearHashtags();
        postRepository.save(post);

        // then
        assertThat(post.getPostHashtags()).isEmpty();
        log.info("✅ Post의 모든 해시태그 제거 성공");
    }

    @Test
    @DisplayName("Post.updateHashtags() - 해시태그 전체 교체")
    void testUpdateHashtags() {
        // given
        Long userId = 20241001L;
        
        BoardCategory category = new BoardCategory(BoardType.DISCUSSION, true, true, true);
        boardCategoryRepository.save(category);
        
        Post post = Post.builder()
                .category(category)
                .title("해시태그 업데이트 테스트")
                .content("내용")
                .postType(PostType.NORMAL)
                .authorId(userId)
                .build();
        
        // 기존 해시태그
        Hashtag oldHashtag1 = Hashtag.builder()
                .name("old1")
                .displayName("구태그1")
                .createdBy(userId)
                .build();
        hashtagRepository.save(oldHashtag1);
        
        Hashtag oldHashtag2 = Hashtag.builder()
                .name("old2")
                .displayName("구태그2")
                .createdBy(userId)
                .build();
        hashtagRepository.save(oldHashtag2);
        
        post.addHashtag(oldHashtag1, userId);
        post.addHashtag(oldHashtag2, userId);
        postRepository.save(post);
        
        assertThat(post.getPostHashtags()).hasSize(2);
        
        // 새 해시태그
        Hashtag newHashtag1 = Hashtag.builder()
                .name("new1")
                .displayName("신태그1")
                .createdBy(userId)
                .build();
        hashtagRepository.save(newHashtag1);
        
        Hashtag newHashtag2 = Hashtag.builder()
                .name("new2")
                .displayName("신태그2")
                .createdBy(userId)
                .build();
        hashtagRepository.save(newHashtag2);
        
        Hashtag newHashtag3 = Hashtag.builder()
                .name("new3")
                .displayName("신태그3")
                .createdBy(userId)
                .build();
        hashtagRepository.save(newHashtag3);

        // when
        post.updateHashtags(List.of(newHashtag1, newHashtag2, newHashtag3), userId);
        postRepository.save(post);

        // then
        assertThat(post.getPostHashtags()).hasSize(3);
        assertThat(post.getPostHashtags())
                .extracting(ph -> ph.getHashtag().getName())
                .containsExactlyInAnyOrder("new1", "new2", "new3")
                .doesNotContain("old1", "old2");
        
        log.info("✅ Post 해시태그 업데이트 성공: {} → {}", 
                List.of("old1", "old2"),
                post.getPostHashtags().stream()
                        .map(ph -> ph.getHashtag().getName())
                        .toList());
    }

    @Test
    @DisplayName("Post 삭제 시 PostHashtag cascade 삭제 확인")
    void testCascadeDeleteWhenPostDeleted() {
        // given
        Long userId = 20241001L;
        
        BoardCategory category = new BoardCategory(BoardType.FREE, true, true, true);
        boardCategoryRepository.save(category);
        
        Post post = Post.builder()
                .category(category)
                .title("Cascade 삭제 테스트")
                .content("내용")
                .postType(PostType.NORMAL)
                .authorId(userId)
                .build();
        
        Hashtag hashtag = Hashtag.builder()
                .name("cascadetest")
                .displayName("Cascade테스트")
                .createdBy(userId)
                .build();
        hashtagRepository.save(hashtag);
        
        post.addHashtag(hashtag, userId);
        postRepository.save(post);
        
        Long postId = post.getId();
        assertThat(post.getPostHashtags()).hasSize(1);

        // when
        postRepository.delete(post);
        postRepository.flush();

        // then
        Post deletedPost = postRepository.findById(postId).orElse(null);
        assertThat(deletedPost).isNull();
        
        log.info("✅ Post 삭제 시 PostHashtag도 함께 삭제됨 (Cascade)");
    }

    @Test
    @DisplayName("Post 조회 시 postHashtags Lazy Loading 확인")
    void testLazyLoadingOfPostHashtags() {
        // given
        Long userId = 20241001L;
        
        BoardCategory category = new BoardCategory(BoardType.STUDENT, true, true, true);
        boardCategoryRepository.save(category);
        
        Post post = Post.builder()
                .category(category)
                .title("Lazy Loading 테스트")
                .content("내용")
                .postType(PostType.NORMAL)
                .authorId(userId)
                .build();
        
        Hashtag hashtag = Hashtag.builder()
                .name("lazy")
                .displayName("Lazy")
                .createdBy(userId)
                .build();
        hashtagRepository.save(hashtag);
        
        post.addHashtag(hashtag, userId);
        postRepository.save(post);
        
        Long postId = post.getId();
        postRepository.flush();

        // when
        Post foundPost = postRepository.findById(postId).orElseThrow();

        // then
        assertThat(foundPost.getPostHashtags()).isNotEmpty(); // Lazy Loading 트리거
        assertThat(foundPost.getPostHashtags()).hasSize(1);
        assertThat(foundPost.getPostHashtags().get(0).getHashtag().getName()).isEqualTo("lazy");
        
        log.info("✅ Post 조회 후 postHashtags Lazy Loading 확인");
    }

    @Test
    @DisplayName("해시태그로 게시글 필터링 - 단일 해시태그")
    void testFindPostsByHashtag() {
        // given
        Long userId = 20241001L;
        
        BoardCategory category = new BoardCategory(BoardType.QUESTION, true, true, true);
        boardCategoryRepository.save(category);
        
        // 게시글 3개 생성
        Post post1 = Post.builder()
                .category(category)
                .title("Java 관련 질문 1")
                .content("내용")
                .postType(PostType.NORMAL)
                .authorId(userId)
                .build();
        
        Post post2 = Post.builder()
                .category(category)
                .title("Java 관련 질문 2")
                .content("내용")
                .postType(PostType.NORMAL)
                .authorId(userId)
                .build();
        
        Post post3 = Post.builder()
                .category(category)
                .title("Python 관련 질문")
                .content("내용")
                .postType(PostType.NORMAL)
                .authorId(userId)
                .build();
        
        // 해시태그 생성
        Hashtag javaTag = Hashtag.builder()
                .name("java")
                .displayName("Java")
                .createdBy(userId)
                .build();
        hashtagRepository.save(javaTag);
        
        Hashtag pythonTag = Hashtag.builder()
                .name("python")
                .displayName("Python")
                .createdBy(userId)
                .build();
        hashtagRepository.save(pythonTag);
        
        // 해시태그 연결
        post1.addHashtag(javaTag, userId);
        post2.addHashtag(javaTag, userId);
        post3.addHashtag(pythonTag, userId);
        
        postRepository.saveAll(List.of(post1, post2, post3));
        postRepository.flush();
        
        Pageable pageable = PageRequest.of(0, 20);

        // when
        Page<Post> javaPostsPage = postRepository.findByCategoryAndHashtagName(category, "java", pageable);

        // then
        assertThat(javaPostsPage.getContent()).hasSize(2);
        assertThat(javaPostsPage.getContent())
                .extracting(Post::getTitle)
                .containsExactlyInAnyOrder("Java 관련 질문 1", "Java 관련 질문 2");
        
        log.info("✅ 해시태그 'java'로 필터링: {}개 게시글 조회", javaPostsPage.getTotalElements());
    }

    @Test
    @DisplayName("해시태그로 게시글 필터링 - 대소문자 무시")
    void testFindPostsByHashtagCaseInsensitive() {
        // given
        Long userId = 20241001L;
        
        BoardCategory category = new BoardCategory(BoardType.DISCUSSION, true, true, true);
        boardCategoryRepository.save(category);
        
        Post post = Post.builder()
                .category(category)
                .title("AI 윤리 토론")
                .content("내용")
                .postType(PostType.NORMAL)
                .authorId(userId)
                .build();
        
        Hashtag aiTag = Hashtag.builder()
                .name("ai")  // 소문자 저장
                .displayName("AI")
                .createdBy(userId)
                .build();
        hashtagRepository.save(aiTag);
        
        post.addHashtag(aiTag, userId);
        postRepository.save(post);
        postRepository.flush();
        
        Pageable pageable = PageRequest.of(0, 20);

        // when - 대문자로 검색
        Page<Post> postsUpperCase = postRepository.findByCategoryAndHashtagName(category, "AI", pageable);
        // when - 소문자로 검색
        Page<Post> postsLowerCase = postRepository.findByCategoryAndHashtagName(category, "ai", pageable);
        // when - 혼합으로 검색
        Page<Post> postsMixedCase = postRepository.findByCategoryAndHashtagName(category, "Ai", pageable);

        // then
        assertThat(postsUpperCase.getContent()).hasSize(1);
        assertThat(postsLowerCase.getContent()).hasSize(1);
        assertThat(postsMixedCase.getContent()).hasSize(1);
        
        log.info("✅ 해시태그 대소문자 무시 검색 성공: AI, ai, Ai 모두 동일 결과");
    }

    @Test
    @DisplayName("해시태그 + 제목 검색으로 게시글 필터링")
    void testFindPostsByHashtagAndTitle() {
        // given
        Long userId = 20241001L;
        
        BoardCategory category = new BoardCategory(BoardType.FREE, true, true, true);
        boardCategoryRepository.save(category);
        
        // 게시글 3개 생성
        Post post1 = Post.builder()
                .category(category)
                .title("맛집 추천 - 신촌 맛집")
                .content("내용")
                .postType(PostType.NORMAL)
                .authorId(userId)
                .build();
        
        Post post2 = Post.builder()
                .category(category)
                .title("맛집 추천 - 강남 맛집")
                .content("내용")
                .postType(PostType.NORMAL)
                .authorId(userId)
                .build();
        
        Post post3 = Post.builder()
                .category(category)
                .title("동아리 모집합니다")
                .content("내용")
                .postType(PostType.NORMAL)
                .authorId(userId)
                .build();
        
        // 해시태그 생성
        Hashtag foodTag = Hashtag.builder()
                .name("맛집추천")
                .displayName("맛집추천")
                .createdBy(userId)
                .build();
        hashtagRepository.save(foodTag);
        
        Hashtag clubTag = Hashtag.builder()
                .name("동아리")
                .displayName("동아리")
                .createdBy(userId)
                .build();
        hashtagRepository.save(clubTag);
        
        // 해시태그 연결
        post1.addHashtag(foodTag, userId);
        post2.addHashtag(foodTag, userId);
        post3.addHashtag(clubTag, userId);
        
        postRepository.saveAll(List.of(post1, post2, post3));
        postRepository.flush();
        
        Pageable pageable = PageRequest.of(0, 20);

        // when - 해시태그 '맛집추천' + 제목 '신촌'
        Page<Post> filteredPosts = postRepository.findByCategoryAndTitleContainingAndHashtagName(
                category, "신촌", "맛집추천", pageable);

        // then
        assertThat(filteredPosts.getContent()).hasSize(1);
        assertThat(filteredPosts.getContent().get(0).getTitle()).isEqualTo("맛집 추천 - 신촌 맛집");
        
        log.info("✅ 해시태그 + 제목 검색 필터링 성공: {}개 게시글", filteredPosts.getTotalElements());
    }

    @Test
    @DisplayName("비활성화된 해시태그는 필터링 결과에서 제외")
    void testExcludeInactiveHashtags() {
        // given
        Long userId = 20241001L;
        
        BoardCategory category = new BoardCategory(BoardType.CONTEST, true, true, true);
        boardCategoryRepository.save(category);
        
        Post post = Post.builder()
                .category(category)
                .title("공모전 정보")
                .content("내용")
                .postType(PostType.NORMAL)
                .authorId(userId)
                .build();
        
        Hashtag inactiveTag = Hashtag.builder()
                .name("it/소프트웨어")
                .displayName("IT/소프트웨어")
                .createdBy(userId)
                .build();
        inactiveTag.deactivate();  // 비활성화
        hashtagRepository.save(inactiveTag);
        
        post.addHashtag(inactiveTag, userId);
        postRepository.save(post);
        postRepository.flush();
        
        Pageable pageable = PageRequest.of(0, 20);

        // when
        Page<Post> posts = postRepository.findByCategoryAndHashtagName(category, "it/소프트웨어", pageable);

        // then
        assertThat(posts.getContent()).isEmpty();
        
        log.info("✅ 비활성화된 해시태그는 필터링 결과에서 제외됨");
    }
}
