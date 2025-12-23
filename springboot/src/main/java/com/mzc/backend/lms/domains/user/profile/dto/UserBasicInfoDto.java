package com.mzc.backend.lms.domains.user.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 유저 기본 정보 DTO
 * 캐시 조회용 복호화된 유저 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicInfoDto {

	private Long id;            // 학번 또는 교번 (userId와 동일)
	private String name;        // 복호화된 이름
	private String userType;    // STUDENT, PROFESSOR
	private String thumbnailUrl; // 프로필 썸네일 URL

	public static UserBasicInfoDto ofStudent(Long id, String name) {
		return UserBasicInfoDto.builder()
				.id(id)
				.name(name)
				.userType("STUDENT")
				.build();
	}

	public static UserBasicInfoDto ofStudent(Long id, String name, String thumbnailUrl) {
		return UserBasicInfoDto.builder()
				.id(id)
				.name(name)
				.userType("STUDENT")
				.thumbnailUrl(thumbnailUrl)
				.build();
	}

	public static UserBasicInfoDto ofProfessor(Long id, String name) {
		return UserBasicInfoDto.builder()
				.id(id)
				.name(name)
				.userType("PROFESSOR")
				.build();
	}

	public static UserBasicInfoDto ofProfessor(Long id, String name, String thumbnailUrl) {
		return UserBasicInfoDto.builder()
				.id(id)
				.name(name)
				.userType("PROFESSOR")
				.thumbnailUrl(thumbnailUrl)
				.build();
	}
}
