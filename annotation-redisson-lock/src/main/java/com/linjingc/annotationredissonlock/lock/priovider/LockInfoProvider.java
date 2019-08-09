package com.linjingc.annotationredissonlock.lock.priovider;

import com.linjingc.annotationredissonlock.lock.LockType;
import com.linjingc.annotationredissonlock.lock.annotation.CatLock;
import com.linjingc.annotationredissonlock.lock.config.RedisLockConfig;
import com.linjingc.annotationredissonlock.lock.model.LockInfo;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 锁提供者  创建锁的相关信息都在这里生成
 *
 * @author cxc
 * @date 2019年8月9日18:02:25
 */
@Component
@Log4j2
public class LockInfoProvider {

    /**
     * 锁的key前缀
     */
    public static final String LOCK_NAME_PREFIX = "lock";
    public static final String LOCK_NAME_SEPARATOR = ".";


    @Autowired
    private RedisLockConfig redisLockConfig;

    @Autowired
    private BusinessKeyProvider businessKeyProvider;


    /***
     * 获取锁信息
     * 锁的名称 = 前缀+(方法名 或 锁名) +自定义key
     * @param joinPoint
     * @param catLock
     * @return
     */
    public LockInfo get(ProceedingJoinPoint joinPoint, CatLock catLock) {
        //获取到切面的信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取到锁类型
        LockType type = catLock.lockType();
        //根据自定义配置 获取keyName
        String businessKeyName = businessKeyProvider.getKeyName(joinPoint, catLock);
        //拼接lockName
        String lockName = LOCK_NAME_PREFIX + LOCK_NAME_SEPARATOR + getName(catLock.name(), signature) + businessKeyName;
        //获取等待时间 不设置则根据keyConfig的生成
        long waitTime = getWaitTime(catLock);
        //获取持有时间 不设置则根据keyConfig的生成
        long leaseTime = getLeaseTime(catLock);
        //如果持有时间设置为-1 表示不会过期
        if (leaseTime == -1 && log.isWarnEnabled()) {
            log.warn("Trying to acquire Lock({}) with no expiration, " +
                    "Klock will keep prolong the lock expiration while the lock is still holding by current thread. " +
                    "This may cause dead lock in some circumstances.", lockName);
        }

        //实例化锁
        return new LockInfo(type, lockName, waitTime, leaseTime);
    }


    /**
     * 获取锁名称
     * @param annotationName
     * @param signature
     * @return
     */
    private String getName(String annotationName, MethodSignature signature) {
        //如果keyname没有设置 则返回方法名称
        if (annotationName.isEmpty()) {
            return String.format("%s.%s", signature.getDeclaringTypeName(), signature.getMethod().getName());
        } else {
            return annotationName;
        }
    }


    /**
     * 如果默认是最大等待时间 则使用配置项内的时间 否则 使用自定义的时间
     *
     * @param lock
     * @return
     */
    private long getWaitTime(CatLock lock) {
        return lock.waitTime() == Long.MIN_VALUE ?
                redisLockConfig.getWaitTime() : lock.waitTime();
    }

    /**
     * 如果默认是最大续约时间 则使用配置项内的时间 否则 使用自定义的时间
     *
     * @param lock
     * @return
     */
    private long getLeaseTime(CatLock lock) {
        return lock.leaseTime() == Long.MIN_VALUE ?
                redisLockConfig.getLeaseTime() : lock.leaseTime();
    }
}
