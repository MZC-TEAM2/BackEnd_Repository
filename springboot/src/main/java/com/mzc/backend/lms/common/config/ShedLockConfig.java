package com.mzc.backend.lms.common.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ShedLock 설정
 * 스케줄러 분산 락을 위한 설정 클래스
 * <p>
 * 사용 예시:
 *
 * @Scheduled(cron = "0 0 3 * * *")
 * @SchedulerLock(name = "cleanExpiredTokens", lockAtMostFor = "10m", lockAtLeastFor = "1m")
 * public void cleanExpiredTokens() {
 * // 스케줄러 로직
 * }
 *
 *
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class ShedLockConfig {
	
	private static final String SHEDLOCK_KEY_PREFIX = "shedlock:";
	
	/**
	 * ShedLock용 Redis LockProvider Bean
	 *
	 * @param connectionFactory Spring Data Redis ConnectionFactory
	 * @return LockProvider 인스턴스
	 */
	@Bean
	public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
		return new RedisLockProvider(connectionFactory, "lms", SHEDLOCK_KEY_PREFIX);
	}
}
