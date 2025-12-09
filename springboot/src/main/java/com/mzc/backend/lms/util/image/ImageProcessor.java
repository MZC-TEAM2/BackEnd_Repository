package com.mzc.backend.lms.util.image;

import com.github.f4b6a3.ulid.UlidCreator;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 이미지 처리 유틸리티
 * - WebP 변환
 * - 썸네일 생성
 * - ULID 파일명 생성
 */
@Slf4j
@Component
public class ImageProcessor {

    private static final String WEBP_EXTENSION = ".webp";
    private static final String THUMBNAIL_SUFFIX = "_thumb";

    @Value("${app.image.max-size:10485760}")
    private long maxFileSize; // 10MB

    @Value("${app.image.quality:70}")
    private int quality;

    @Value("${app.image.max-width:800}")
    private int maxWidth;

    @Value("${app.image.max-height:800}")
    private int maxHeight;

    @Value("${app.image.thumbnail.width:150}")
    private int thumbnailWidth;

    @Value("${app.image.thumbnail.height:150}")
    private int thumbnailHeight;

    /**
     * ULID 기반 파일명 생성
     */
    public String generateFileName() {
        return UlidCreator.getUlid().toString() + WEBP_EXTENSION;
    }

    /**
     * 썸네일용 파일명 생성
     */
    public String generateThumbnailFileName(String originalFileName) {
        String baseName = originalFileName.replace(WEBP_EXTENSION, "");
        return baseName + THUMBNAIL_SUFFIX + WEBP_EXTENSION;
    }

    /**
     * 파일 크기 검증
     */
    public void validateFileSize(MultipartFile file) {
        if (file.getSize() > maxFileSize) {
            throw new ImageStorageException(
                    String.format("파일 크기가 제한을 초과했습니다. 최대 %dMB", maxFileSize / (1024 * 1024)));
        }
    }

    /**
     * 이미지 타입 검증
     */
    public void validateImageType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageStorageException("이미지 파일만 업로드 가능합니다.");
        }
    }

    /**
     * 이미지를 WebP로 변환 (리사이즈 포함)
     * @param file 원본 파일
     * @return WebP 바이트 배열
     */
    public byte[] convertToWebp(MultipartFile file) {
        try {
            ImmutableImage image = ImmutableImage.loader().fromStream(file.getInputStream());

            // 최대 크기 초과 시 리사이즈
            if (image.width > maxWidth || image.height > maxHeight) {
                image = image.bound(maxWidth, maxHeight);
            }

            return image.bytes(WebpWriter.DEFAULT.withQ(quality));
        } catch (IOException e) {
            throw new ImageStorageException("이미지 변환 실패", e);
        }
    }

    /**
     * 썸네일 생성 (WebP)
     * @param file 원본 파일
     * @return 썸네일 WebP 바이트 배열
     */
    public byte[] createThumbnail(MultipartFile file) {
        try {
            ImmutableImage image = ImmutableImage.loader().fromStream(file.getInputStream());

            // 정사각형 썸네일 (중앙 크롭 후 리사이즈)
            ImmutableImage thumbnail = image.cover(thumbnailWidth, thumbnailHeight);

            return thumbnail.bytes(WebpWriter.DEFAULT.withQ(quality));
        } catch (IOException e) {
            throw new ImageStorageException("썸네일 생성 실패", e);
        }
    }

    /**
     * 이미지 검증 (크기 + 타입)
     */
    public void validate(MultipartFile file) {
        validateImageType(file);
        validateFileSize(file);
    }

    /**
     * 바이트 배열에서 WebP 변환 (비동기 처리용)
     */
    public byte[] convertToWebpFromBytes(byte[] imageBytes) {
        try {
            ImmutableImage image = ImmutableImage.loader().fromStream(new ByteArrayInputStream(imageBytes));

            if (image.width > maxWidth || image.height > maxHeight) {
                image = image.bound(maxWidth, maxHeight);
            }

            return image.bytes(WebpWriter.DEFAULT.withQ(quality));
        } catch (IOException e) {
            throw new ImageStorageException("이미지 변환 실패", e);
        }
    }

    /**
     * 바이트 배열에서 썸네일 생성 (비동기 처리용)
     */
    public byte[] createThumbnailFromBytes(byte[] imageBytes) {
        try {
            ImmutableImage image = ImmutableImage.loader().fromStream(new ByteArrayInputStream(imageBytes));
            ImmutableImage thumbnail = image.cover(thumbnailWidth, thumbnailHeight);
            return thumbnail.bytes(WebpWriter.DEFAULT.withQ(quality));
        } catch (IOException e) {
            throw new ImageStorageException("썸네일 생성 실패", e);
        }
    }
}