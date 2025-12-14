package com.mzc.backend.lms.domains.user.profile.service;

import com.mzc.backend.lms.domains.user.profile.dto.ProfileResponseDto;
import com.mzc.backend.lms.domains.user.profile.dto.ProfileUpdateRequestDto;

/**
 * 프로필 서비스 인터페이스
 */
public interface ProfileService {

    /**
     * 내 프로필 조회
     * @param userId 사용자 ID
     * @return 프로필 정보
     */
    ProfileResponseDto getMyProfile(Long userId);

    /**
     * 프로필 수정
     * @param userId 사용자 ID
     * @param request 수정 요청 DTO
     * @return 수정된 프로필 정보
     */
    ProfileResponseDto updateProfile(Long userId, ProfileUpdateRequestDto request);
}