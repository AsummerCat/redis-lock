package com.linjingc.annotationredissonlock.lock.basiclock;

import com.linjingc.annotationredissonlock.lock.model.LockInfo;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 读锁
 *
 * @author cxc
 * @date 2019年8月8日17:59:38
 */
public class ReadLock implements Lock {

    private RReadWriteLock rLock;

    private final LockInfo lockInfo;

    private RedissonClient redissonClient;

    public ReadLock(RedissonClient redissonClient, LockInfo info) {
        this.redissonClient = redissonClient;
        this.lockInfo = info;
    }

    @Override
    public boolean acquire() {
        try {
            rLock = redissonClient.getReadWriteLock(lockInfo.getName());
            return rLock.readLock().tryLock(lockInfo.getWaitTime(), lockInfo.getLeaseTime(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean release() {
        if (rLock.readLock().isHeldByCurrentThread()) {
            try {
                return rLock.readLock().forceUnlockAsync().get();
            } catch (InterruptedException e) {
                return false;
            } catch (ExecutionException e) {
                return false;
            }
        }

        return false;
    }
}
