package com.linjingc.annotationredissonlock.lock.priovider;

import com.linjingc.annotationredissonlock.lock.LockType;
import com.linjingc.annotationredissonlock.lock.annotation.Redislock;
import com.linjingc.annotationredissonlock.lock.model.LockInfo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.klock.annotation.Klock;
import org.springframework.boot.autoconfigure.klock.config.KlockConfig;
import org.springframework.boot.autoconfigure.klock.model.LockInfo;
import org.springframework.boot.autoconfigure.klock.model.LockType;

/**
 * Created by kl on 2017/12/29.
 */
public class LockInfoProvider {

    public static final String LOCK_NAME_PREFIX = "lock";
    public static final String LOCK_NAME_SEPARATOR = ".";


    @Autowired
    private KlockConfig klockConfig;

    @Autowired
    private BusinessKeyProvider businessKeyProvider;

    private static final Logger logger = LoggerFactory.getLogger(LockInfoProvider.class);

    public LockInfo get(ProceedingJoinPoint joinPoint, Redislock redislock) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        LockType type= redislock.lockType();
        String businessKeyName=businessKeyProvider.getKeyName(joinPoint,redislock);
        String lockName = LOCK_NAME_PREFIX+LOCK_NAME_SEPARATOR+getName(redislock.name(), signature)+businessKeyName;
        long waitTime = getWaitTime(redislock);
        long leaseTime = getLeaseTime(redislock);

        if(leaseTime == -1 && logger.isWarnEnabled()) {
            logger.warn("Trying to acquire Lock({}) with no expiration, " +
                        "Klock will keep prolong the lock expiration while the lock is still holding by current thread. " +
                        "This may cause dead lock in some circumstances.", lockName);
        }

        return new LockInfo(type,lockName,waitTime,leaseTime);
    }

    private String getName(String annotationName, MethodSignature signature) {
        if (annotationName.isEmpty()) {
            return String.format("%s.%s", signature.getDeclaringTypeName(), signature.getMethod().getName());
        } else {
            return annotationName;
        }
    }


    /**
     * 如果默认是最大等待时间 则使用配置项内的时间 否则 使用自定义的时间
     * @param lock
     * @return
     */
    private long getWaitTime(Redislock lock) {
        return lock.waitTime() == Long.MIN_VALUE ?
                klockConfig.getWaitTime() : lock.waitTime();
    }

    /**
     * 如果默认是最大续约时间 则使用配置项内的时间 否则 使用自定义的时间
     * @param lock
     * @return
     */
    private long getLeaseTime(Redislock lock) {
        return lock.leaseTime() == Long.MIN_VALUE ?
                klockConfig.getLeaseTime() : lock.leaseTime();
    }
}
