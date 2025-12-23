package com.mzc.backend.lms.domains.user.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프로필 수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequestDto {
	
	private String name;
	private String mobileNumber;
	private String homeNumber;
	private String officeNumber;
}
