package com.mzc.backend.lms.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 설정
 * 비즈니스 로직용 분산 락을 위한 설정 클래스
 * <p>
 * ShedLock과의 역할 분담:
 * <p>
 * ShedLock: 스케줄러 중복 실행 방지 (선언적, @SchedulerLock)
 * Redisson: 비즈니스 로직 동시성 제어 (프로그래밍 방식 또는 @DistributedLock)
 *
 */
@Configuration
public class RedissonConfig {
	
	@Value("${spring.data.redis.host}")
	private String redisHost;
	
	@Value("${spring.data.redis.port}")
	private int redisPort;
	
	/**
	 * RedissonClient Bean
	 *
	 * @return RedissonClient 인스턴스
	 */
	@Bean(destroyMethod = "shutdown")
	public RedissonClient redissonClient() {
		Config config = new Config();
		config.useSingleServer()
				.setAddress("redis://" + redisHost + ":" + redisPort)
				.setConnectionMinimumIdleSize(1)
				.setConnectionPoolSize(2);
		
		return Redisson.create(config);
	}
}
