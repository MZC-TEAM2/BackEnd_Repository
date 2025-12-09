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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

    // Self-injection for @Async proxy call
    private ProfileImageServiceImpl self;

    @Autowired
    @Lazy
    public void setSelf(ProfileImageServiceImpl self) {
        this.self = self;
    }

    @Override
    @Transactional
    public void uploadProfileImage(Long userId, MultipartFile file) {
        // 1. 사용자 검증
        userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 2. 파일 검증
        imageProcessor.validate(file);

        // 3. 기존 이미지 파일 삭제 (DB 레코드는 비동기에서 업데이트)
        deleteExistingImageFiles(userId);

        // 4. 이미지 데이터 미리 읽기 (비동기 스레드에서 MultipartFile 접근 불가)
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            throw new RuntimeException("파일 읽기 실패", e);
        }

        // 5. 비동기로 이미지 처리 및 저장 (self-proxy를 통해 호출해야 @Async 동작)
        self.processAndSaveImageAsync(userId, fileBytes);
    }

    @Async("imageProcessingExecutor")
    public CompletableFuture<Void> processAndSaveImageAsync(Long userId, byte[] fileBytes) {
        try {
            log.info("프로필 이미지 처리 시작: userId={}", userId);

            // 1. 파일명 생성 (ULID)
            String fileName = imageProcessor.generateFileName();

            // 2. WebP 변환 및 저장
            byte[] webpData = imageProcessor.convertToWebpFromBytes(fileBytes);
            String imageUrl = imageStorageStrategy.store(webpData, fileName, "image/webp");

            // 3. 썸네일 생성 및 저장
            byte[] thumbnailData = imageProcessor.createThumbnailFromBytes(fileBytes);
            String thumbnailUrl = imageStorageStrategy.storeThumbnail(thumbnailData, fileName);

            // 4. DB 저장 (새로운 트랜잭션에서 실행)
            saveProfileImage(userId, imageUrl, thumbnailUrl);

            log.info("프로필 이미지 처리 완료: userId={}, imageUrl={}", userId, imageUrl);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("프로필 이미지 처리 실패: userId={}", userId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProfileImage(Long userId, String imageUrl, String thumbnailUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Optional<UserProfileImage> existingImage = userProfileImageRepository.findByUserId(userId);

        if (existingImage.isPresent()) {
            // 기존 이미지 업데이트 - 더티 체킹으로 자동 저장 (save 호출 불필요)
            existingImage.get().updateImage(imageUrl, thumbnailUrl);
        } else {
            // 새 이미지 생성
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

        deleteExistingImageFiles(userId);

        userProfileImageRepository.findByUserId(userId)
                .ifPresent(userProfileImageRepository::delete);

        log.info("프로필 이미지 삭제 완료: userId={}", userId);
    }

    private void deleteExistingImageFiles(Long userId) {
        userProfileImageRepository.findByUserId(userId)
                .ifPresent(image -> {
                    imageStorageStrategy.delete(image.getImageUrl());
                    imageStorageStrategy.delete(image.getThumbnailUrl());
                });
    }
}