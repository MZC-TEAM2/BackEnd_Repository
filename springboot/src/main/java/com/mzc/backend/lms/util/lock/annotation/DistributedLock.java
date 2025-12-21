package com.mzc.backend.lms.util.lock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 분산 락 어노테이션
 * Redisson 기반 분산 락을 선언적으로 사용하기 위한 어노테이션
 *
 * 사용 예시:
 *
 * // 정적 키
 * @DistributedLock(key = "order-process")
 * public void processOrder() { }
 *
 * // SpEL을 사용한 동적 키
 * @DistributedLock(key = "order:#{#orderId}")
 * public void processOrder(Long orderId) { }
 *
 * // 락 대기 시간 및 유지 시간 설정
 * @DistributedLock(key = "payment:#{#paymentId}", waitTime = 5, leaseTime = 30)
 * public void processPayment(Long paymentId) { }
 * }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 락 키
     * SpEL 표현식 지원 (예: "order:#{#orderId}")
     *
     * @return 락 키 문자열
     */
    String key();

    /**
     * 락 획득 대기 시간
     * 이 시간 동안 락을 획득하지 못하면 예외 발생
     *
     * @return 대기 시간 (기본값: 5)
     */
    long waitTime() default 5;

    /**
     * 락 유지 시간
     * 이 시간이 지나면 자동으로 락 해제 (Watchdog 비활성화 시)
     * -1로 설정하면 Watchdog이 자동으로 락 연장
     *
     * @return 유지 시간 (기본값: 10)
     */
    long leaseTime() default 10;

    /**
     * 시간 단위
     *
     * @return TimeUnit (기본값: SECONDS)
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
