package com.mzc.backend.lms.util.lock.service;

import com.mzc.backend.lms.util.lock.exception.DistributedLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DistributedLockService 테스트")
class DistributedLockServiceTest {
	
	@Mock
	private RedissonClient redissonClient;
	
	@Mock
	private RLock rLock;
	
	private DistributedLockService distributedLockService;
	
	@BeforeEach
	void setUp() {
		distributedLockService = new DistributedLockServiceImpl(redissonClient);
		given(redissonClient.getLock(anyString())).willReturn(rLock);
	}
	
	@Nested
	@DisplayName("executeWithLock 테스트")
	class ExecuteWithLockTest {
		
		@Test
		@DisplayName("락 획득 성공 시 작업이 실행된다")
		void givenLockAcquired_whenExecuteWithLock_thenTaskExecuted() throws InterruptedException {
			// Given
			given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);
			given(rLock.isHeldByCurrentThread()).willReturn(true);
			AtomicBoolean taskExecuted = new AtomicBoolean(false);
			
			// When
			distributedLockService.executeWithLock("test-key", () -> taskExecuted.set(true));
			
			// Then
			assertThat(taskExecuted.get()).isTrue();
			verify(rLock).unlock();
		}
		
		@Test
		@DisplayName("락 획득 성공 시 반환값이 정상적으로 반환된다")
		void givenLockAcquired_whenExecuteWithLockWithSupplier_thenResultReturned() throws InterruptedException {
			// Given
			given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);
			given(rLock.isHeldByCurrentThread()).willReturn(true);
			String expectedResult = "success";
			
			// When
			String result = distributedLockService.executeWithLock("test-key", () -> expectedResult);
			
			// Then
			assertThat(result).isEqualTo(expectedResult);
			verify(rLock).unlock();
		}
		
		@Test
		@DisplayName("락 획득 실패 시 예외가 발생한다")
		void givenLockNotAcquired_whenExecuteWithLock_thenThrowsException() throws InterruptedException {
			// Given
			given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(false);
			
			// When & Then
			assertThatThrownBy(() ->
					distributedLockService.executeWithLock("test-key", () -> {
					})
			).isInstanceOf(DistributedLockException.class)
					.hasMessageContaining("Failed to acquire distributed lock");
			
			verify(rLock, never()).unlock();
		}
		
		@Test
		@DisplayName("작업 실행 중 예외가 발생해도 락이 해제된다")
		void givenTaskThrowsException_whenExecuteWithLock_thenLockReleased() throws InterruptedException {
			// Given
			given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);
			given(rLock.isHeldByCurrentThread()).willReturn(true);
			
			// When & Then
			assertThatThrownBy(() ->
					distributedLockService.executeWithLock("test-key", () -> {
						throw new RuntimeException("Task failed");
					})
			).isInstanceOf(RuntimeException.class)
					.hasMessage("Task failed");
			
			verify(rLock).unlock();
		}
		
		@Test
		@DisplayName("커스텀 타임아웃으로 락을 획득한다")
		void givenCustomTimeout_whenExecuteWithLock_thenUsesCustomTimeout() throws InterruptedException {
			// Given
			given(rLock.tryLock(10L, 30L, TimeUnit.SECONDS)).willReturn(true);
			given(rLock.isHeldByCurrentThread()).willReturn(true);
			AtomicInteger counter = new AtomicInteger(0);
			
			// When
			distributedLockService.executeWithLock("test-key", 10, 30, TimeUnit.SECONDS,
					() -> counter.incrementAndGet());
			
			// Then
			assertThat(counter.get()).isEqualTo(1);
			verify(rLock).tryLock(10L, 30L, TimeUnit.SECONDS);
		}
	}
	
	@Nested
	@DisplayName("tryExecuteWithLock 테스트")
	class TryExecuteWithLockTest {
		
		@Test
		@DisplayName("락 획득 성공 시 true를 반환한다")
		void givenLockAcquired_whenTryExecuteWithLock_thenReturnsTrue() {
			// Given
			given(rLock.tryLock()).willReturn(true);
			given(rLock.isHeldByCurrentThread()).willReturn(true);
			AtomicBoolean taskExecuted = new AtomicBoolean(false);
			
			// When
			boolean result = distributedLockService.tryExecuteWithLock("test-key",
					() -> taskExecuted.set(true));
			
			// Then
			assertThat(result).isTrue();
			assertThat(taskExecuted.get()).isTrue();
			verify(rLock).unlock();
		}
		
		@Test
		@DisplayName("락 획득 실패 시 false를 반환하고 작업이 실행되지 않는다")
		void givenLockNotAcquired_whenTryExecuteWithLock_thenReturnsFalse() {
			// Given
			given(rLock.tryLock()).willReturn(false);
			AtomicBoolean taskExecuted = new AtomicBoolean(false);
			
			// When
			boolean result = distributedLockService.tryExecuteWithLock("test-key",
					() -> taskExecuted.set(true));
			
			// Then
			assertThat(result).isFalse();
			assertThat(taskExecuted.get()).isFalse();
			verify(rLock, never()).unlock();
		}
		
		@Test
		@DisplayName("락 획득 실패 시 Supplier는 null을 반환한다")
		void givenLockNotAcquired_whenTryExecuteWithLockSupplier_thenReturnsNull() {
			// Given
			given(rLock.tryLock()).willReturn(false);
			
			// When
			String result = distributedLockService.tryExecuteWithLock("test-key", () -> "success");
			
			// Then
			assertThat(result).isNull();
		}
	}
}
