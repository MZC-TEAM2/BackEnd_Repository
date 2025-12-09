package com.mzc.backend.lms.util.image;

/**
 * 이미지 저장 관련 예외
 */
public class ImageStorageException extends RuntimeException {

    public ImageStorageException(String message) {
        super(message);
    }

    public ImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}