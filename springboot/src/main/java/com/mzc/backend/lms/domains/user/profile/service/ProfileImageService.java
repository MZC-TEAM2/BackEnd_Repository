package com.mzc.backend.lms.domains.user.profile.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 프로필 이미지 서비스 인터페이스
 */
public interface ProfileImageService {

    /**
     * 프로필 이미지 업로드
     * @param userId 사용자 ID
     * @param file 이미지 파일
     */
    void uploadProfileImage(Long userId, MultipartFile file);

    /**
     * 프로필 이미지 삭제
     * @param userId 사용자 ID
     */
    void deleteProfileImage(Long userId);
}