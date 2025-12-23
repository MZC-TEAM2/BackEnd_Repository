package com.mzc.backend.lms.domains.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 좋아요 토글 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeToggleResponseDto {
	
	private Boolean liked;
	
	private String message;
	
	/**
	 * 좋아요 토글 응답 생성
	 */
	public static LikeToggleResponseDto of(boolean liked) {
		return LikeToggleResponseDto.builder()
				.liked(liked)
				.message(liked ? "좋아요를 추가했습니다." : "좋아요를 취소했습니다.")
				.build();
	}
}
