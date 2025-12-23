package com.mzc.backend.lms.domains.user.user.scheduler;

import com.mzc.backend.lms.domains.user.auth.token.repository.RefreshTokenRepository;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 탈퇴 회원 하드 딜리트 스케줄러
 * 탈퇴 후 30일이 경과한 회원 데이터를 영구 삭제
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WithdrawnUserCleanupScheduler {
	
	private static final int RETENTION_DAYS = 30;
	
	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	
	/**
	 * 탈퇴 후 30일 경과 회원 하드 딜리트
	 * 매일 새벽 3시 30분에 실행 (토큰 정리 스케줄러 이후)
	 */
	@Scheduled(cron = "0 30 3 * * *")
	@SchedulerLock(
			name = "WithdrawnUserCleanupScheduler_hardDeleteWithdrawnUsers",
			lockAtMostFor = "30m",
			lockAtLeastFor = "5m"
	)
	@Transactional
	public void hardDeleteWithdrawnUsers() {
		log.info("Starting withdrawn user cleanup scheduler");
		
		LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);
		
		// 1. 삭제 대상 사용자 ID 조회
		List<Long> userIdsToDelete = userRepository.findUserIdsDeletedBefore(threshold);
		
		if (userIdsToDelete.isEmpty()) {
			log.info("No withdrawn users to delete");
			return;
		}
		
		log.info("Found {} withdrawn users to delete (deleted before {})",
				userIdsToDelete.size(), threshold);
		
		// 2. RefreshToken 먼저 삭제 (FK 참조)
		int deletedTokens = refreshTokenRepository.deleteByUserIds(userIdsToDelete);
		log.info("Deleted {} refresh tokens for withdrawn users", deletedTokens);
		
		// 3. 사용자 하드 딜리트 (연관 데이터는 CASCADE로 자동 삭제)
		int deletedUsers = userRepository.hardDeleteUsersDeletedBefore(threshold);
		
		log.info("Withdrawn user cleanup completed. Deleted {} users", deletedUsers);
	}
}
