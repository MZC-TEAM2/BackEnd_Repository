package com.mzc.backend.lms.domains.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 좋아요 여부 조회 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeCheckResponseDto {
	
	private Boolean liked;
	
	/**
	 * 좋아요 여부 응답 생성
	 */
	public static LikeCheckResponseDto of(boolean liked) {
		return LikeCheckResponseDto.builder()
				.liked(liked)
				.build();
	}
}
