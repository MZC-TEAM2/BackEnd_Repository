package com.mzc.backend.lms.domains.board.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Hashtag 엔티티 단위 테스트
 */
@DisplayName("Hashtag 엔티티 테스트")
class HashtagTest {

    @Test
    @DisplayName("Hashtag 생성 - 빌더 패턴")
    void testHashtagCreation() {
        // given
        Long userId = 20241001L;

        // when
        Hashtag hashtag = Hashtag.builder()
                .name("AI윤리토론")
                .displayName("AI윤리토론")
                .description("인공지능 윤리 관련 토론")
                .color("#ff5722")
                .tagCategory("DISCUSSION")
                .createdBy(userId)
                .build();

        // then
        assertThat(hashtag.getName()).isEqualTo("ai윤리토론"); // 소문자 변환 확인
        assertThat(hashtag.getDisplayName()).isEqualTo("AI윤리토론");
        assertThat(hashtag.getDescription()).isEqualTo("인공지능 윤리 관련 토론");
        assertThat(hashtag.getColor()).isEqualTo("#ff5722");
        assertThat(hashtag.getTagCategory()).isEqualTo("DISCUSSION");
        assertThat(hashtag.isActive()).isTrue(); // 기본값 true
    }

    @Test
    @DisplayName("Hashtag 생성 - name 소문자 변환 및 공백 제거")
    void testHashtagNameNormalization() {
        // given
        String mixedCaseName = "  Python프로그래밍  ";

        // when
        Hashtag hashtag = Hashtag.builder()
                .name(mixedCaseName)
                .displayName("Python프로그래밍")
                .createdBy(20241001L)
                .build();

        // then
        assertThat(hashtag.getName()).isEqualTo("python프로그래밍");
    }

    @Test
    @DisplayName("Hashtag 생성 - color 기본값 설정")
    void testHashtagDefaultColor() {
        // given & when
        Hashtag hashtag = Hashtag.builder()
                .name("test")
                .displayName("테스트")
                .createdBy(20241001L)
                .build();

        // then
        assertThat(hashtag.getColor()).isEqualTo("#007bff"); // 기본값
    }

    @Test
    @DisplayName("Hashtag 활성화")
    void testActivateHashtag() {
        // given
        Hashtag hashtag = Hashtag.builder()
                .name("test")
                .displayName("테스트")
                .createdBy(20241001L)
                .build();
        hashtag.deactivate(); // 먼저 비활성화

        // when
        hashtag.activate();

        // then
        assertThat(hashtag.isActive()).isTrue();
    }

    @Test
    @DisplayName("Hashtag 비활성화")
    void testDeactivateHashtag() {
        // given
        Hashtag hashtag = Hashtag.builder()
                .name("test")
                .displayName("테스트")
                .createdBy(20241001L)
                .build();

        // when
        hashtag.deactivate();

        // then
        assertThat(hashtag.isActive()).isFalse();
    }

    @Test
    @DisplayName("Hashtag 정보 업데이트 - 모든 필드")
    void testUpdateHashtagAllFields() {
        // given
        Hashtag hashtag = Hashtag.builder()
                .name("oldname")
                .displayName("구이름")
                .description("구설명")
                .color("#000000")
                .createdBy(20241001L)
                .build();

        // when
        hashtag.update("새이름", "새설명", "#ffffff");

        // then
        assertThat(hashtag.getDisplayName()).isEqualTo("새이름");
        assertThat(hashtag.getDescription()).isEqualTo("새설명");
        assertThat(hashtag.getColor()).isEqualTo("#ffffff");
    }

    @Test
    @DisplayName("Hashtag 정보 업데이트 - 일부 필드만 (null 방어)")
    void testUpdateHashtagPartialFields() {
        // given
        Hashtag hashtag = Hashtag.builder()
                .name("test")
                .displayName("원래이름")
                .description("원래설명")
                .color("#ff0000")
                .createdBy(20241001L)
                .build();

        // when
        hashtag.update("변경된이름", null, null);

        // then
        assertThat(hashtag.getDisplayName()).isEqualTo("변경된이름");
        assertThat(hashtag.getDescription()).isEqualTo("원래설명"); // 변경 없음
        assertThat(hashtag.getColor()).isEqualTo("#ff0000"); // 변경 없음
    }
}
