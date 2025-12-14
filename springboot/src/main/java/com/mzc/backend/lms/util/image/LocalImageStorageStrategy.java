package com.mzc.backend.lms.util.image;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 로컬 파일 시스템 이미지 저장 전략
 * 저장 경로: {base-path}/{images|thumbnails}/{yyyy}/{MM}/{dd}/{filename}
 * URL 형식: {server-url}/images/{images|thumbnails}/{yyyy}/{MM}/{dd}/{filename}
 *
 * Nginx 정적 파일 서빙 고려:
 * - 로컬 저장 폴더를 Nginx static 폴더에 바인드마운트
 * - server-url 환경변수로 도메인 설정
 */
@Slf4j
@Component
public class LocalImageStorageStrategy implements ImageStorageStrategy {

    private static final String IMAGES_DIR = "images";
    private static final String THUMBNAILS_DIR = "thumbnails";
    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MM");
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd");

    @Value("${app.image.storage.local.base-path:uploads}")
    private String basePath;

    @Value("${app.server.url:http://localhost:8080}")
    private String serverUrl;

    @Value("${app.image.storage.local.url-prefix:/images}")
    private String urlPrefix;

    private Path storagePath;

    @PostConstruct
    public void init() {
        this.storagePath = Paths.get(basePath).toAbsolutePath().normalize();

        try {
            Files.createDirectories(storagePath);
            log.info("이미지 저장 기본 디렉토리 초기화: {}", storagePath);
            log.info("이미지 URL 형식: {}{}/...", serverUrl, urlPrefix);
        } catch (IOException e) {
            throw new ImageStorageException("이미지 저장 디렉토리 생성 실패", e);
        }
    }

    @Override
    public String store(InputStream inputStream, String fileName, String contentType) {
        String datePath = getDatePath();
        Path targetDir = storagePath.resolve(IMAGES_DIR).resolve(datePath);
        return storeToPath(targetDir, inputStream, fileName, IMAGES_DIR + "/" + datePath);
    }

    @Override
    public String store(byte[] data, String fileName, String contentType) {
        String datePath = getDatePath();
        Path targetDir = storagePath.resolve(IMAGES_DIR).resolve(datePath);
        return storeToPath(targetDir, data, fileName, IMAGES_DIR + "/" + datePath);
    }

    /**
     * 썸네일 저장
     */
    public String storeThumbnail(byte[] data, String fileName) {
        String datePath = getDatePath();
        Path targetDir = storagePath.resolve(THUMBNAILS_DIR).resolve(datePath);
        return storeToPath(targetDir, data, fileName, THUMBNAILS_DIR + "/" + datePath);
    }

    /**
     * 날짜 기반 경로 생성 (yyyy/MM/dd)
     */
    private String getDatePath() {
        LocalDate now = LocalDate.now();
        return now.format(YEAR_FORMAT) + "/" + now.format(MONTH_FORMAT) + "/" + now.format(DAY_FORMAT);
    }

    /**
     * 전체 URL 생성
     * 예: https://example.com/images/images/2025/12/09/filename.webp
     */
    private String buildFullUrl(String urlPath, String fileName) {
        String baseUrl = serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
        String prefix = urlPrefix.startsWith("/") ? urlPrefix : "/" + urlPrefix;
        return baseUrl + prefix + "/" + urlPath + "/" + fileName;
    }

    private String storeToPath(Path targetDir, InputStream inputStream, String fileName, String urlPath) {
        try {
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(fileName).normalize();
            validatePath(targetDir, targetPath, fileName);

            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("이미지 저장 완료: {}", targetPath);

            return buildFullUrl(urlPath, fileName);
        } catch (IOException e) {
            throw new ImageStorageException("이미지 저장 실패: " + fileName, e);
        }
    }

    private String storeToPath(Path targetDir, byte[] data, String fileName, String urlPath) {
        try {
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(fileName).normalize();
            validatePath(targetDir, targetPath, fileName);

            Files.write(targetPath, data);
            log.debug("이미지 저장 완료: {}", targetPath);

            return buildFullUrl(urlPath, fileName);
        } catch (IOException e) {
            throw new ImageStorageException("이미지 저장 실패: " + fileName, e);
        }
    }

    private void validatePath(Path baseDir, Path targetPath, String fileName) {
        if (!targetPath.startsWith(baseDir)) {
            throw new ImageStorageException("잘못된 파일 경로: " + fileName);
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            Path filePath = urlToPath(fileUrl);

            if (filePath != null && Files.exists(filePath)) {
                Files.delete(filePath);
                log.debug("이미지 삭제 완료: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("이미지 삭제 실패: {}", fileUrl, e);
        }
    }

    /**
     * URL을 파일 경로로 변환
     * https://example.com/images/images/2025/12/09/filename.webp -> {basePath}/images/2025/12/09/filename.webp
     */
    private Path urlToPath(String fileUrl) {
        if (fileUrl == null) {
            return null;
        }

        // URL에서 prefix 이후 경로 추출
        String prefix = urlPrefix.startsWith("/") ? urlPrefix : "/" + urlPrefix;
        int prefixIndex = fileUrl.indexOf(prefix);

        if (prefixIndex < 0) {
            return null;
        }

        String relativePath = fileUrl.substring(prefixIndex + prefix.length());
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }

        return storagePath.resolve(relativePath).normalize();
    }

    @Override
    public String extractFileName(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return "";
        }
        int lastSlashIndex = fileUrl.lastIndexOf('/');
        return lastSlashIndex >= 0 ? fileUrl.substring(lastSlashIndex + 1) : fileUrl;
    }
}
