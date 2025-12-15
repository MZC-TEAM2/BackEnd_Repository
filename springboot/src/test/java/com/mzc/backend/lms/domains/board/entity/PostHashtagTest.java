package com.mzc.backend.lms.domains.board.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PostHashtag 연결 엔티티 단위 테스트
 */
@DisplayName("PostHashtag 연결 엔티티 테스트")
class PostHashtagTest {

    @Test
    @DisplayName("PostHashtag 생성 - 빌더 패턴")
    void testPostHashtagCreation() {
        // given
        Long userId = 20241001L;
        
        Post post = Post.builder()
                .title("테스트 게시글")
                .content("내용")
                .build();
        
        Hashtag hashtag = Hashtag.builder()
                .name("test")
                .displayName("테스트")
                .createdBy(userId)
                .build();

        // when
        PostHashtag postHashtag = PostHashtag.builder()
                .post(post)
                .hashtag(hashtag)
                .createdBy(userId)
                .build();

        // then
        assertThat(postHashtag.getPost()).isEqualTo(post);
        assertThat(postHashtag.getHashtag()).isEqualTo(hashtag);
    }

    @Test
    @DisplayName("PostHashtag - 양방향 관계 설정 (Post → PostHashtag)")
    void testBidirectionalRelationship() {
        // given
        Long userId = 20241001L;
        
        Post post = Post.builder()
                .title("양방향 테스트")
                .content("내용")
                .build();
        
        Hashtag hashtag = Hashtag.builder()
                .name("bidirectional")
                .displayName("양방향")
                .createdBy(userId)
                .build();

        // when
        PostHashtag postHashtag = PostHashtag.builder()
                .hashtag(hashtag)
                .createdBy(userId)
                .build();
        
        postHashtag.setPost(post); // 양방향 관계 설정

        // then
        assertThat(postHashtag.getPost()).isEqualTo(post);
        assertThat(post.getPostHashtags()).contains(postHashtag);
    }

    @Test
    @DisplayName("PostHashtag - 기존 관계 변경 시 자동 정리")
    void testChangingRelationshipCleansUpOldPost() {
        // given
        Long userId = 20241001L;
        
        Post oldPost = Post.builder()
                .title("이전 게시글")
                .content("내용")
                .build();
        
        Post newPost = Post.builder()
                .title("새 게시글")
                .content("내용")
                .build();
        
        Hashtag hashtag = Hashtag.builder()
                .name("test")
                .displayName("테스트")
                .createdBy(userId)
                .build();

        PostHashtag postHashtag = PostHashtag.builder()
                .hashtag(hashtag)
                .createdBy(userId)
                .build();
        
        postHashtag.setPost(oldPost);

        // when
        postHashtag.setPost(newPost); // 새 게시글로 변경

        // then
        assertThat(postHashtag.getPost()).isEqualTo(newPost);
        assertThat(newPost.getPostHashtags()).contains(postHashtag);
        assertThat(oldPost.getPostHashtags()).doesNotContain(postHashtag); // 이전 관계 정리
    }

    @Test
    @DisplayName("PostHashtag - 중복 추가 방지")
    void testDuplicatePreventionInBidirectionalRelationship() {
        // given
        Long userId = 20241001L;
        
        Post post = Post.builder()
                .title("중복 테스트")
                .content("내용")
                .build();
        
        Hashtag hashtag = Hashtag.builder()
                .name("duplicate")
                .displayName("중복")
                .createdBy(userId)
                .build();

        PostHashtag postHashtag = PostHashtag.builder()
                .hashtag(hashtag)
                .createdBy(userId)
                .build();

        // when
        postHashtag.setPost(post);
        postHashtag.setPost(post); // 동일한 Post에 다시 설정

        // then
        assertThat(post.getPostHashtags()).hasSize(1); // 중복 추가 안 됨
        assertThat(post.getPostHashtags()).contains(postHashtag);
    }
}
