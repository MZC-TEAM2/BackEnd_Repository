package com.mzc.backend.lms.domains.user.auth.token.scheduler;

import com.mzc.backend.lms.domains.user.auth.token.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 리프레시 토큰 정리 스케줄러
 * 만료되었거나 폐기된 토큰을 주기적으로 삭제
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupScheduler {
	
	private final RefreshTokenRepository refreshTokenRepository;
	
	/**
	 * 만료/폐기 리프레시 토큰 정리
	 * 매일 새벽 3시에 실행
	 */
	@Scheduled(cron = "0 0 3 * * *")
	@SchedulerLock(
			name = "RefreshTokenCleanupScheduler_cleanExpiredOrRevokedTokens",
			lockAtMostFor = "10m",
			lockAtLeastFor = "1m"
	)
	@Transactional
	public void cleanExpiredOrRevokedTokens() {
		log.info("Starting refresh token cleanup scheduler");
		
		LocalDateTime now = LocalDateTime.now();
		int deletedCount = refreshTokenRepository.deleteExpiredOrRevokedTokens(now);
		
		log.info("Refresh token cleanup completed. Deleted {} tokens", deletedCount);
	}
}
