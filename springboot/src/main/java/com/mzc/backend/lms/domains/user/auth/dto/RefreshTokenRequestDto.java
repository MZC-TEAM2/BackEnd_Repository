package com.mzc.backend.lms.domains.user.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 토큰 갱신 요청 DTO
 */
@Data
public class RefreshTokenRequestDto {
	
	@NotBlank(message = "Refresh Token은 필수입니다")
	private String refreshToken;
}
