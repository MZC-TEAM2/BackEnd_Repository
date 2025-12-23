package com.mzc.backend.lms.domains.dashboard.student.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 일일 로그인 확인 서비스
 * Redis를 사용하여 오늘 첫 로그인 여부를 확인
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyLoginService {
	
	private static final String KEY_PREFIX = "daily_login:";
	
	private final StringRedisTemplate redisTemplate;
	
	/**
	 * 오늘 첫 로그인인지 확인하고, 첫 로그인이면 마킹
	 *
	 * @param userId 사용자 ID
	 * @return true: 오늘 첫 로그인, false: 이미 로그인한 적 있음
	 */
	public boolean checkAndMarkFirstLoginToday(Long userId) {
		String key = buildKey(userId);
		
		Boolean isFirstLogin = redisTemplate.opsForValue().setIfAbsent(key, "1", calculateTtl());
		
		if (Boolean.TRUE.equals(isFirstLogin)) {
			log.debug("오늘 첫 로그인: userId={}", userId);
			return true;
		}
		
		log.debug("오늘 이미 로그인함: userId={}", userId);
		return false;
	}
	
	/**
	 * 오늘 로그인 여부만 확인 (마킹 없이)
	 *
	 * @param userId 사용자 ID
	 * @return true: 오늘 로그인한 적 있음, false: 오늘 첫 로그인
	 */
	public boolean hasLoggedInToday(Long userId) {
		String key = buildKey(userId);
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}
	
	private String buildKey(Long userId) {
		return KEY_PREFIX + userId + ":" + LocalDate.now();
	}
	
	/**
	 * 자정까지 남은 시간 계산
	 */
	private Duration calculateTtl() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime midnight = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT);
		return Duration.between(now, midnight);
	}
}
