package com.mzc.backend.lms.domains.board.service;

import com.mzc.backend.lms.common.config.JpaConfig;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Hashtag;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.enums.PostType;
import com.mzc.backend.lms.domains.board.repository.BoardCategoryRepository;
import com.mzc.backend.lms.domains.board.repository.HashtagRepository;
import com.mzc.backend.lms.domains.board.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * HashtagService 단위 테스트
 */
@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(JpaConfig.class)
@DisplayName("HashtagService 테스트")
class HashtagServiceTest {

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BoardCategoryRepository boardCategoryRepository;

    private HashtagService hashtagService;

    private Long userId;
    private Post testPost;

    @BeforeEach
    void setUp() {
        hashtagService = new HashtagService(hashtagRepository);
        userId = 20241001L;

        // 테스트용 게시글 생성
        BoardCategory category = new BoardCategory(BoardType.FREE, true, true, true);
        boardCategoryRepository.save(category);

        testPost = Post.builder()
                .category(category)
                .title("테스트 게시글")
                .content("내용")
                .postType(PostType.NORMAL)
                .authorId(userId)
                .build();
        postRepository.save(testPost);
    }

    @Test
    @DisplayName("getOrCreateHashtag - 새 해시태그 생성")
    void testCreateNewHashtag() {
        // given
        String tagName = "Java";

        // when
        Hashtag hashtag = hashtagService.getOrCreateHashtag(tagName, userId);

        // then
        assertThat(hashtag).isNotNull();
        assertThat(hashtag.getId()).isNotNull();
        assertThat(hashtag.getName()).isEqualTo("java"); // 소문자 변환
        assertThat(hashtag.getDisplayName()).isEqualTo("Java"); // 원본 유지
        assertThat(hashtag.getColor()).isEqualTo("#007bff"); // 기본 색상
        assertThat(hashtag.isActive()).isTrue();
    }

    @Test
    @DisplayName("getOrCreateHashtag - 기존 해시태그 재사용")
    void testReuseExistingHashtag() {
        // given
        String tagName = "Spring";
        Hashtag existing = Hashtag.builder()
                .name("spring")
                .displayName("Spring")
                .createdBy(userId)
                .build();
        hashtagRepository.save(existing);

        // when
        Hashtag retrieved = hashtagService.getOrCreateHashtag(tagName, userId);

        // then
        assertThat(retrieved.getId()).isEqualTo(existing.getId()); // 같은 ID
        assertThat(hashtagRepository.findAll()).hasSize(1); // 중복 생성 안 됨
    }

    @Test
    @DisplayName("getOrCreateHashtag - null 또는 빈 문자열 예외 처리")
    void testGetOrCreateHashtagWithInvalidInput() {
        // when & then
        assertThatThrownBy(() -> hashtagService.getOrCreateHashtag(null, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해시태그 이름은 필수입니다");

        assertThatThrownBy(() -> hashtagService.getOrCreateHashtag("", userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해시태그 이름은 필수입니다");

        assertThatThrownBy(() -> hashtagService.getOrCreateHashtag("   ", userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해시태그 이름은 필수입니다");
    }

    @Test
    @DisplayName("attachHashtagsToPost - 게시글에 해시태그 연결")
    void testAttachHashtagsToPost() {
        // given
        List<String> tagNames = Arrays.asList("Java", "Spring", "JPA");

        // when
        hashtagService.attachHashtagsToPost(testPost, tagNames, userId);

        // then
        assertThat(testPost.getPostHashtags()).hasSize(3);
        assertThat(testPost.getPostHashtags())
                .extracting(ph -> ph.getHashtag().getName())
                .containsExactlyInAnyOrder("java", "spring", "jpa");
    }

    @Test
    @DisplayName("attachHashtagsToPost - 중복 태그 제거")
    void testAttachHashtagsWithDuplicates() {
        // given
        List<String> tagNames = Arrays.asList("Java", "java", "JAVA", "Spring");

        // when
        hashtagService.attachHashtagsToPost(testPost, tagNames, userId);

        // then
        assertThat(testPost.getPostHashtags()).hasSize(2); // 중복 제거됨
        assertThat(testPost.getPostHashtags())
                .extracting(ph -> ph.getHashtag().getName())
                .containsExactlyInAnyOrder("java", "spring");
    }

    @Test
    @DisplayName("attachHashtagsToPost - 빈 리스트 처리")
    void testAttachHashtagsWithEmptyList() {
        // given
        List<String> emptyList = List.of();

        // when
        hashtagService.attachHashtagsToPost(testPost, emptyList, userId);

        // then
        assertThat(testPost.getPostHashtags()).isEmpty(); // 아무것도 추가 안 됨
    }

    @Test
    @DisplayName("attachHashtagsToPost - null 게시글 예외 처리")
    void testAttachHashtagsWithNullPost() {
        // given
        List<String> tagNames = Arrays.asList("Java");

        // when & then
        assertThatThrownBy(() -> hashtagService.attachHashtagsToPost(null, tagNames, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글은 필수입니다");
    }

    @Test
    @DisplayName("updatePostHashtags - 해시태그 전체 교체")
    void testUpdatePostHashtags() {
        // given
        List<String> oldTags = Arrays.asList("Old1", "Old2");
        hashtagService.attachHashtagsToPost(testPost, oldTags, userId);
        assertThat(testPost.getPostHashtags()).hasSize(2);

        // when
        List<String> newTags = Arrays.asList("New1", "New2", "New3");
        hashtagService.updatePostHashtags(testPost, newTags, userId);

        // then
        assertThat(testPost.getPostHashtags()).hasSize(3);
        assertThat(testPost.getPostHashtags())
                .extracting(ph -> ph.getHashtag().getName())
                .containsExactlyInAnyOrder("new1", "new2", "new3")
                .doesNotContain("old1", "old2");
    }

    @Test
    @DisplayName("updatePostHashtags - 빈 리스트로 모두 제거")
    void testUpdatePostHashtagsToEmpty() {
        // given
        List<String> oldTags = Arrays.asList("Tag1", "Tag2");
        hashtagService.attachHashtagsToPost(testPost, oldTags, userId);
        assertThat(testPost.getPostHashtags()).hasSize(2);

        // when
        hashtagService.updatePostHashtags(testPost, List.of(), userId);

        // then
        assertThat(testPost.getPostHashtags()).isEmpty(); // 모두 제거됨
    }

    @Test
    @DisplayName("updatePostHashtags - null 리스트로 모두 제거")
    void testUpdatePostHashtagsToNull() {
        // given
        List<String> oldTags = Arrays.asList("Tag1", "Tag2");
        hashtagService.attachHashtagsToPost(testPost, oldTags, userId);
        assertThat(testPost.getPostHashtags()).hasSize(2);

        // when
        hashtagService.updatePostHashtags(testPost, null, userId);

        // then
        assertThat(testPost.getPostHashtags()).isEmpty(); // 모두 제거됨
    }

    @Test
    @DisplayName("해시태그 재사용 - 여러 게시글이 같은 해시태그 사용")
    void testHashtagReusedAcrossPosts() {
        // given
        Post post1 = testPost;
        Post post2 = Post.builder()
                .category(testPost.getCategory())
                .title("두 번째 게시글")
                .content("내용")
                .postType(PostType.NORMAL)
                .authorId(userId)
                .build();
        postRepository.save(post2);

        // when
        hashtagService.attachHashtagsToPost(post1, Arrays.asList("Java", "Spring"), userId);
        hashtagService.attachHashtagsToPost(post2, Arrays.asList("Java", "Python"), userId);

        // then
        // Java 해시태그는 재사용됨 (1개만 생성)
        List<Hashtag> allHashtags = hashtagRepository.findAll();
        assertThat(allHashtags).hasSize(3); // Java, Spring, Python

        Hashtag javaHashtag = hashtagRepository.findByName("java").orElseThrow();
        assertThat(javaHashtag).isNotNull();
        
        log.info("✅ 해시태그 재사용 확인: Java 해시태그가 2개 게시글에서 공유됨");
    }
}
