package com.mzc.backend.lms.util.lock.service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 분산 락 서비스 인터페이스
 * 프로그래밍 방식으로 분산 락을 사용하기 위한 서비스
 *
 * 사용 예시:
 *
 * // 기본 사용
 * distributedLockService.executeWithLock("order:123", () -> {
 *     processOrder(123L);
 * });
 *
 * // 반환값이 있는 경우
 * Order result = distributedLockService.executeWithLock("order:123", () -> {
 *     return orderRepository.findById(123L);
 * });
 *
 * // 커스텀 타임아웃
 * distributedLockService.executeWithLock("order:123", 10, 30, TimeUnit.SECONDS, () -> {
 *     processComplexOrder(123L);
 * });
 *
 */
public interface DistributedLockService {

    /**
     * 락을 획득하고 작업 실행 (반환값 없음)
     * 기본 설정: waitTime=5초, leaseTime=10초
     *
     * @param lockKey 락 키
     * @param task 실행할 작업
     */
    void executeWithLock(String lockKey, Runnable task);

    /**
     * 락을 획득하고 작업 실행 (반환값 있음)
     * 기본 설정: waitTime=5초, leaseTime=10초
     *
     * @param lockKey 락 키
     * @param supplier 실행할 작업
     * @param <T> 반환 타입
     * @return 작업 결과
     */
    <T> T executeWithLock(String lockKey, Supplier<T> supplier);

    /**
     * 락을 획득하고 작업 실행 (반환값 없음, 커스텀 타임아웃)
     *
     * @param lockKey 락 키
     * @param waitTime 락 대기 시간
     * @param leaseTime 락 유지 시간
     * @param timeUnit 시간 단위
     * @param task 실행할 작업
     */
    void executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Runnable task);

    /**
     * 락을 획득하고 작업 실행 (반환값 있음, 커스텀 타임아웃)
     *
     * @param lockKey 락 키
     * @param waitTime 락 대기 시간
     * @param leaseTime 락 유지 시간
     * @param timeUnit 시간 단위
     * @param supplier 실행할 작업
     * @param <T> 반환 타입
     * @return 작업 결과
     */
    <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> supplier);

    /**
     * 락 획득 시도 (즉시 반환)
     * 락을 획득하지 못하면 false 반환
     *
     * @param lockKey 락 키
     * @param task 실행할 작업
     * @return 락 획득 및 작업 실행 성공 여부
     */
    boolean tryExecuteWithLock(String lockKey, Runnable task);

    /**
     * 락 획득 시도 (즉시 반환, 반환값 있음)
     * 락을 획득하지 못하면 null 반환
     *
     * @param lockKey 락 키
     * @param supplier 실행할 작업
     * @param <T> 반환 타입
     * @return 작업 결과 또는 null
     */
    <T> T tryExecuteWithLock(String lockKey, Supplier<T> supplier);
}
