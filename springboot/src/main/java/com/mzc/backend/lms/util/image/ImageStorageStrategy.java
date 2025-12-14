package com.mzc.backend.lms.util.image;

import java.io.InputStream;

/**
 * 이미지 저장 전략 인터페이스
 * Local, S3 등 다양한 저장소 구현 가능
 */
public interface ImageStorageStrategy {

    /**
     * 이미지 저장
     * @param inputStream 이미지 입력 스트림
     * @param fileName 저장할 파일명 (ULID 기반)
     * @param contentType 컨텐츠 타입
     * @return 저장된 이미지 URL
     */
    String store(InputStream inputStream, String fileName, String contentType);

    /**
     * 이미지 저장 (바이트 배열)
     * @param data 이미지 바이트 배열
     * @param fileName 저장할 파일명
     * @param contentType 컨텐츠 타입
     * @return 저장된 이미지 URL
     */
    String store(byte[] data, String fileName, String contentType);

    /**
     * 이미지 삭제
     * @param fileUrl 삭제할 이미지 URL
     */
    void delete(String fileUrl);

    /**
     * URL에서 파일명 추출
     * @param fileUrl 파일 URL
     * @return 파일명
     */
    String extractFileName(String fileUrl);
}