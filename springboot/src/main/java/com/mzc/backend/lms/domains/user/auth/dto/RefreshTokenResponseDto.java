package com.mzc.backend.lms.domains.user.auth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 토큰 갱신 응답 DTO
 */
@Data
@Builder
public class RefreshTokenResponseDto {
	
	private String accessToken;
	private String refreshToken;
	
	/**
	 * 토큰 응답 생성
	 */
	public static RefreshTokenResponseDto of(String accessToken, String refreshToken) {
		return RefreshTokenResponseDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();
	}
}
