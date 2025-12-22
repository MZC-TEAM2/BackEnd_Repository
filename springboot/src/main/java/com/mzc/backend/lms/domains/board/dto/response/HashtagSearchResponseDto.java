package com.mzc.backend.lms.domains.board.dto.response;

import com.mzc.backend.lms.domains.board.entity.Hashtag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 해시태그 검색 응답 DTO
 * 자동완성 기능에서 사용
 */
@Getter
@Builder
@AllArgsConstructor
public class HashtagSearchResponseDto {

    /**
     * 해시태그 ID
     */
    private Long id;

    /**
     * 해시태그 이름 (소문자, 검색용)
     */
    private String name;

    /**
     * 해시태그 표시명 (화면 표시용)
     */
    private String displayName;

    /**
     * 해시태그 색상
     */
    private String color;

    /**
     * 해시태그 카테고리
     */
    private String category;

    /**
     * Hashtag 엔티티로부터 DTO 생성
     */
    public static HashtagSearchResponseDto from(Hashtag hashtag) {
        return HashtagSearchResponseDto.builder()
                .id(hashtag.getId())
                .name(hashtag.getName())
                .displayName(hashtag.getDisplayName())
                .color(hashtag.getColor())
                .category(hashtag.getTagCategory())
                .build();
    }
}
