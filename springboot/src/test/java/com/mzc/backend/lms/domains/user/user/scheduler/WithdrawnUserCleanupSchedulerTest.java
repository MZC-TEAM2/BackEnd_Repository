package com.mzc.backend.lms.domains.user.user.scheduler;

import com.mzc.backend.lms.domains.user.auth.token.repository.RefreshTokenRepository;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawnUserCleanupScheduler 테스트")
class WithdrawnUserCleanupSchedulerTest {
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private RefreshTokenRepository refreshTokenRepository;
	
	@InjectMocks
	private WithdrawnUserCleanupScheduler scheduler;
	
	@Test
	@DisplayName("탈퇴 후 30일 경과 회원이 있으면 하드 딜리트한다")
	void givenWithdrawnUsers_whenCleanup_thenUsersDeleted() {
		// Given
		List<Long> userIds = List.of(1L, 2L, 3L);
		given(userRepository.findUserIdsDeletedBefore(any(LocalDateTime.class)))
				.willReturn(userIds);
		given(refreshTokenRepository.deleteByUserIds(userIds))
				.willReturn(5);
		given(userRepository.hardDeleteUsersDeletedBefore(any(LocalDateTime.class)))
				.willReturn(3);
		
		// When
		scheduler.hardDeleteWithdrawnUsers();
		
		// Then
		verify(refreshTokenRepository).deleteByUserIds(userIds);
		verify(userRepository).hardDeleteUsersDeletedBefore(any(LocalDateTime.class));
	}
	
	@Test
	@DisplayName("삭제 대상 회원이 없으면 삭제 쿼리를 실행하지 않는다")
	void givenNoWithdrawnUsers_whenCleanup_thenSkipsDelete() {
		// Given
		given(userRepository.findUserIdsDeletedBefore(any(LocalDateTime.class)))
				.willReturn(Collections.emptyList());
		
		// When
		scheduler.hardDeleteWithdrawnUsers();
		
		// Then
		verify(refreshTokenRepository, never()).deleteByUserIds(anyList());
		verify(userRepository, never()).hardDeleteUsersDeletedBefore(any(LocalDateTime.class));
	}
}
