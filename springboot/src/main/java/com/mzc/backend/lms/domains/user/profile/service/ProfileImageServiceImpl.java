package com.mzc.backend.lms.domains.user.profile.service;

import com.mzc.backend.lms.domains.user.profile.entity.UserProfileImage;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileImageRepository;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.exceptions.UserErrorCode;
import com.mzc.backend.lms.domains.user.user.exceptions.UserException;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import com.mzc.backend.lms.util.image.ImageProcessor;
import com.mzc.backend.lms.util.image.LocalImageStorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 프로필 이미지 서비스 구현체
 * 비동기로 이미지 처리 (WebP 변환, 썸네일 생성)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileImageServiceImpl implements ProfileImageService {

    private final UserRepository userRepository;
    private final UserProfileImageRepository userProfileImageRepository;
    private final ImageProcessor imageProcessor;
    private final LocalImageStorageStrategy imageStorageStrategy;

    @Override
    @Transactional
    public void uploadProfileImage(Long userId, MultipartFile file) {
        // 1. 사용자 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 2. 파일 검증
        imageProcessor.validate(file);

        // 3. 기존 이미지 삭제
        deleteExistingImages(userId);

        // 4. 비동기로 이미지 처리 및 저장
        processAndSaveImageAsync(user, file);
    }

    @Async("imageProcessingExecutor")
    public CompletableFuture<Void> processAndSaveImageAsync(User user, MultipartFile file) {
        try {
            log.info("프로필 이미지 처리 시작: userId={}", user.getId());

            // 1. 파일명 생성 (ULID)
            String fileName = imageProcessor.generateFileName();

            // 2. WebP 변환 및 저장
            byte[] webpData = imageProcessor.convertToWebp(file);
            String imageUrl = imageStorageStrategy.store(webpData, fileName, "image/webp");

            // 3. 썸네일 생성 및 저장
            byte[] thumbnailData = imageProcessor.createThumbnail(file);
            String thumbnailUrl = imageStorageStrategy.storeThumbnail(thumbnailData, fileName);

            // 4. DB 저장
            saveProfileImage(user, imageUrl, thumbnailUrl);

            log.info("프로필 이미지 처리 완료: userId={}, imageUrl={}", user.getId(), imageUrl);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("프로필 이미지 처리 실패: userId={}", user.getId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Transactional
    public void saveProfileImage(User user, String imageUrl, String thumbnailUrl) {
        Optional<UserProfileImage> existingImage = userProfileImageRepository.findByUserId(user.getId());

        if (existingImage.isPresent()) {
            existingImage.get().updateImage(imageUrl, thumbnailUrl);
            userProfileImageRepository.save(existingImage.get());
        } else {
            UserProfileImage profileImage = UserProfileImage.builder()
                    .user(user)
                    .imageUrl(imageUrl)
                    .thumbnailUrl(thumbnailUrl)
                    .build();
            userProfileImageRepository.save(profileImage);
        }
    }

    @Override
    @Transactional
    public void deleteProfileImage(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        deleteExistingImages(userId);

        userProfileImageRepository.findByUserId(userId)
                .ifPresent(userProfileImageRepository::delete);

        log.info("프로필 이미지 삭제 완료: userId={}", userId);
    }

    private void deleteExistingImages(Long userId) {
        userProfileImageRepository.findByUserId(userId)
                .ifPresent(image -> {
                    imageStorageStrategy.delete(image.getImageUrl());
                    imageStorageStrategy.delete(image.getThumbnailUrl());
                });
    }
}