package com.mzc.backend.lms.util.lock.service;

import com.mzc.backend.lms.util.lock.exception.DistributedLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 분산 락 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistributedLockServiceImpl implements DistributedLockService {

    private static final String LOCK_KEY_PREFIX = "lock:";
    private static final long DEFAULT_WAIT_TIME = 5L;
    private static final long DEFAULT_LEASE_TIME = 10L;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    private final RedissonClient redissonClient;

    @Override
    public void executeWithLock(String lockKey, Runnable task) {
        executeWithLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, DEFAULT_TIME_UNIT, task);
    }

    @Override
    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        return executeWithLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, DEFAULT_TIME_UNIT, supplier);
    }

    @Override
    public void executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Runnable task) {
        executeWithLock(lockKey, waitTime, leaseTime, timeUnit, () -> {
            task.run();
            return null;
        });
    }

    @Override
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit,
                                  Supplier<T> supplier) {
        String fullLockKey = LOCK_KEY_PREFIX + lockKey;
        RLock lock = redissonClient.getLock(fullLockKey);

        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitTime, leaseTime, timeUnit);

            if (!acquired) {
                log.warn("Failed to acquire lock for key: {}", fullLockKey);
                throw DistributedLockException.lockAcquisitionFailed(fullLockKey);
            }

            log.debug("Lock acquired for key: {}", fullLockKey);
            return supplier.get();

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

    @Override
    public boolean tryExecuteWithLock(String lockKey, Runnable task) {
        Boolean result = tryExecuteWithLock(lockKey, () -> {
            task.run();
            return true;
        });
        return result != null && result;
    }

    @Override
    public <T> T tryExecuteWithLock(String lockKey, Supplier<T> supplier) {
        String fullLockKey = LOCK_KEY_PREFIX + lockKey;
        RLock lock = redissonClient.getLock(fullLockKey);

        boolean acquired = false;
        try {
            acquired = lock.tryLock();

            if (!acquired) {
                log.debug("Could not acquire lock immediately for key: {}", fullLockKey);
                return null;
            }

            log.debug("Lock acquired for key: {}", fullLockKey);
            return supplier.get();

        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Lock released for key: {}", fullLockKey);
            }
        }
    }
}
