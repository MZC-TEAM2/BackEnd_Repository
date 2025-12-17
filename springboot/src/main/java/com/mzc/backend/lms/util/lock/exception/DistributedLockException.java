package com.mzc.backend.lms.util.lock.exception;

/**
 * 분산 락 관련 예외
 */
public class DistributedLockException extends RuntimeException {

    public DistributedLockException(String message) {
        super(message);
    }

    public DistributedLockException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 락 획득 실패 예외 생성
     *
     * @param key 락 키
     * @return DistributedLockException
     */
    public static DistributedLockException lockAcquisitionFailed(String key) {
        return new DistributedLockException(
                String.format("Failed to acquire distributed lock for key: %s", key)
        );
    }

    /**
     * 락 인터럽트 예외 생성
     *
     * @param key 락 키
     * @param cause 원인 예외
     * @return DistributedLockException
     */
    public static DistributedLockException lockInterrupted(String key, Throwable cause) {
        return new DistributedLockException(
                String.format("Interrupted while acquiring lock for key: %s", key),
                cause
        );
    }
}
