package com.mzc.backend.lms.domains.user.profile.controller;

import com.mzc.backend.lms.domains.user.profile.dto.ProfileResponseDto;
import com.mzc.backend.lms.domains.user.profile.dto.ProfileUpdateRequestDto;
import com.mzc.backend.lms.domains.user.profile.service.ProfileImageService;
import com.mzc.backend.lms.domains.user.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 프로필 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final ProfileImageService profileImageService;

    /**
     * 내 프로필 조회
     * GET /api/v1/profile/me
     */
    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDto> getMyProfile(@AuthenticationPrincipal Long userId) {
        log.debug("프로필 조회 요청: userId={}", userId);

        ProfileResponseDto profile = profileService.getMyProfile(userId);

        return ResponseEntity.ok(profile);
    }

    /**
     * 프로필 수정
     * PUT /api/v1/profile/me
     */
    @PutMapping("/me")
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal Long userId,
            @RequestBody ProfileUpdateRequestDto request) {
        log.debug("프로필 수정 요청: userId={}", userId);

        profileService.updateProfile(userId, request);

        return ResponseEntity.ok().build();
    }

    /**
     * 프로필 이미지 업로드
     * POST /api/v1/profile/me/image
     */
    @PostMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadProfileImage(
            @AuthenticationPrincipal Long userId,
            @RequestParam("image") MultipartFile file) {
        log.debug("프로필 이미지 업로드 요청: userId={}, fileName={}", userId, file.getOriginalFilename());

        profileImageService.uploadProfileImage(userId, file);

        return ResponseEntity.accepted().build();
    }

    /**
     * 프로필 이미지 삭제
     * DELETE /api/v1/profile/me/image
     */
    @DeleteMapping("/me/image")
    public ResponseEntity<Void> deleteProfileImage(@AuthenticationPrincipal Long userId) {
        log.debug("프로필 이미지 삭제 요청: userId={}", userId);

        profileImageService.deleteProfileImage(userId);

        return ResponseEntity.ok().build();
    }
}
