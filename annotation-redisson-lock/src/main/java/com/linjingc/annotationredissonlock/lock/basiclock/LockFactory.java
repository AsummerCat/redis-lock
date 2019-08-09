package com.linjingc.annotationredissonlock.lock.basiclock;

import com.linjingc.annotationredissonlock.lock.model.LockInfo;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 工厂模式 根据lock的类型自动加载 对应的锁类型
 *
 * @author cxc
 * @date 2019年8月8日17:50:38
 */
@Log4j2
@Component
public class LockFactory {
    @Autowired
    private RedissonClient redissonClient;

    public Lock getLock(LockInfo lockInfo) {
        switch (lockInfo.getType()) {
            case Fair:
                return new FairLock(redissonClient, lockInfo);
            case Read:
                return new ReadLock(redissonClient, lockInfo);
            case Write:
                return new WriteLock(redissonClient, lockInfo);
            default:
                return new ReentrantLock(redissonClient, lockInfo);
        }
    }

}
