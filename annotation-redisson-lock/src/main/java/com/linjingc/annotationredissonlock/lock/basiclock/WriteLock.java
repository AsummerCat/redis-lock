package com.linjingc.annotationredissonlock.lock.basiclock;

import com.linjingc.annotationredissonlock.lock.model.LockInfo;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 写锁
 *
 * @author cxc
 * @date 2019年8月8日18:00:07
 */
public class WriteLock implements Lock {

    private RReadWriteLock rLock;

    private final LockInfo lockInfo;

    private RedissonClient redissonClient;

    public WriteLock(RedissonClient redissonClient, LockInfo info) {
        this.redissonClient = redissonClient;
        this.lockInfo = info;
    }

    @Override
    public boolean acquire() {
        try {
            rLock = redissonClient.getReadWriteLock(lockInfo.getName());
            return rLock.writeLock().tryLock(lockInfo.getWaitTime(), lockInfo.getLeaseTime(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean release() {
        if (rLock.writeLock().isHeldByCurrentThread()) {
            try {
                return rLock.writeLock().forceUnlockAsync().get();
            } catch (InterruptedException | ExecutionException e) {
                return false;
            }
        }
        return false;
    }
}
