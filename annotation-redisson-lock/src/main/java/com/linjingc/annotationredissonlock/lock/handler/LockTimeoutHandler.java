package com.linjingc.annotationredissonlock.lock.handler;

import com.linjingc.annotationredissonlock.lock.basiclock.Lock;
import com.linjingc.annotationredissonlock.lock.model.LockInfo;
import org.aspectj.lang.JoinPoint;

/**
 * 获取锁超时的策略接口
 *
 * @author cxc
 * @date 2019年8月8日18:13:34
 **/
public interface LockTimeoutHandler {

    /**
     * 处理
     *
     * @param lockInfo  锁信息
     * @param lock      锁类型
     * @param joinPoint 切面内容
     */
    void handle(LockInfo lockInfo, Lock lock, JoinPoint joinPoint);
}
