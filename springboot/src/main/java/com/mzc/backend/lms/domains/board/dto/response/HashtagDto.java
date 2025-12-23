package com.mzc.backend.lms.domains.board.dto.response;

import com.mzc.backend.lms.domains.board.entity.Hashtag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 해시태그 응답 DTO
 * 프론트엔드에 해시태그 정보를 전달
 */
@Getter
@Builder
@AllArgsConstructor
public class HashtagDto {
	
	private Long id;
	
	private String tagName;
	
	private String color;
	
	private String category;
	
	public static HashtagDto from(Hashtag hashtag) {
		return HashtagDto.builder()
				.id(hashtag.getId())
				.tagName(hashtag.getDisplayName())
				.color(hashtag.getColor())
				.category(hashtag.getTagCategory())
				.build();
	}
}
