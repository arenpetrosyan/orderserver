package com.aren.orderserver.web.redis;


import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderProcessingLock {

    private final RedissonClient redissonClient;

    /**
     * Acquires a lock for the specified order ID.
     *
     * @param orderId the ID of the order to acquire lock for
     */
    public void acquireLock(Integer orderId) {
        RLock lock = redissonClient.getLock("orderLock:" + orderId);
        lock.tryLock();
    }

    /**
     * Releases the lock for the specified order ID.
     *
     * @param orderId the ID of the order to release lock for
     */
    public void releaseLock(Integer orderId) {
        RLock lock = redissonClient.getLock("orderLock:" + orderId);
        lock.unlock();
    }

    /**
     * Checks if a lock is currently held for the specified order ID.
     *
     * @param orderId the ID of the order to check lock status for
     * @return true if the order is locked, false otherwise
     */
    public boolean isLocked(Integer orderId) {
        RLock lock = redissonClient.getLock("orderLock:" + orderId);
        return lock.isLocked();
    }
}