package com.mzc.backend.lms.util.lock.aop;

import com.mzc.backend.lms.util.lock.annotation.DistributedLock;
import com.mzc.backend.lms.util.lock.exception.DistributedLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 분산 락 AOP Aspect
 *
 * @DistributedLock 어노테이션이 적용된 메서드에 분산 락을 적용
 * <p>
 * 동작 방식:
 * 1. 어노테이션에서 락 키 추출 (SpEL 지원)
 * 2. Redisson을 사용하여 락 획득 시도
 * 3. 락 획득 성공 시 메서드 실행
 * 4. 메서드 완료 후 락 해제
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {
	
	private static final String LOCK_KEY_PREFIX = "lock:";
	private static final String SPEL_PREFIX = "#{";
	
	private final RedissonClient redissonClient;
	private final ExpressionParser expressionParser = new SpelExpressionParser();
	private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
	
	/**
	 * @param joinPoint       조인포인트
	 * @param distributedLock 어노테이션
	 * @return 메서드 실행 결과
	 * @throws Throwable 메서드 실행 중 발생한 예외
	 * @DistributedLock 어노테이션이 적용된 메서드에 락 적용
	 */
	@Around("@annotation(distributedLock)")
	public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
		String lockKey = resolveLockKey(joinPoint, distributedLock.key());
		String fullLockKey = LOCK_KEY_PREFIX + lockKey;
		RLock lock = redissonClient.getLock(fullLockKey);
		
		boolean acquired = false;
		try {
			acquired = lock.tryLock(
					distributedLock.waitTime(),
					distributedLock.leaseTime(),
					distributedLock.timeUnit()
			);
			
			if (!acquired) {
				log.warn("Failed to acquire lock for key: {}", fullLockKey);
				throw DistributedLockException.lockAcquisitionFailed(fullLockKey);
			}
			
			log.debug("Lock acquired for key: {}", fullLockKey);
			return joinPoint.proceed();
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw DistributedLockException.lockInterrupted(fullLockKey, e);
		} finally {
			if (acquired && lock.isHeldByCurrentThread()) {
				lock.unlock();
				log.debug("Lock released for key: {}", fullLockKey);
			}
		}
	}
	
	/**
	 * 락 키 해석
	 * SpEL 표현식이 포함된 경우 파싱하여 실제 값으로 변환
	 *
	 * @param joinPoint     조인포인트
	 * @param keyExpression 키 표현식
	 * @return 해석된 락 키
	 */
	private String resolveLockKey(ProceedingJoinPoint joinPoint, String keyExpression) {
		if (!keyExpression.contains(SPEL_PREFIX)) {
			return keyExpression;
		}
		
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		Object[] args = joinPoint.getArgs();
		String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
		
		EvaluationContext context = new StandardEvaluationContext();
		if (parameterNames != null) {
			for (int i = 0; i < parameterNames.length; i++) {
				context.setVariable(parameterNames[i], args[i]);
			}
		}
		
		// SpEL 표현식 추출 및 파싱
		StringBuilder result = new StringBuilder();
		int lastIndex = 0;
		int startIndex;
		
		while ((startIndex = keyExpression.indexOf(SPEL_PREFIX, lastIndex)) != -1) {
			result.append(keyExpression, lastIndex, startIndex);
			
			int endIndex = keyExpression.indexOf("}", startIndex);
			if (endIndex == -1) {
				break;
			}
			
			String expression = keyExpression.substring(startIndex + 2, endIndex);
			Object value = expressionParser.parseExpression(expression).getValue(context);
			result.append(value != null ? value.toString() : "null");
			
			lastIndex = endIndex + 1;
		}
		
		result.append(keyExpression.substring(lastIndex));
		return result.toString();
	}
}

