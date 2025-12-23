package com.mzc.backend.lms.domains.user.profile.swagger;

import com.mzc.backend.lms.domains.user.profile.dto.ProfileResponseDto;
import com.mzc.backend.lms.domains.user.profile.dto.ProfileUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Profile", description = "프로필 API")
public interface ProfileControllerSwagger {
	
	@Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필을 조회합니다")
	ResponseEntity<ProfileResponseDto> getMyProfile(
			@Parameter(hidden = true) Long userId);
	
	@Operation(summary = "프로필 수정", description = "현재 로그인한 사용자의 프로필을 수정합니다")
	ResponseEntity<ProfileResponseDto> updateProfile(
			@Parameter(hidden = true) Long userId,
			ProfileUpdateRequestDto request);
	
	@Operation(summary = "프로필 이미지 업로드", description = "프로필 이미지를 업로드합니다")
	ResponseEntity<Void> uploadProfileImage(
			@Parameter(hidden = true) Long userId,
			@Parameter(description = "업로드할 이미지 파일") MultipartFile file);
	
	@Operation(summary = "프로필 이미지 삭제", description = "프로필 이미지를 삭제합니다")
	ResponseEntity<Void> deleteProfileImage(
			@Parameter(hidden = true) Long userId);
}
