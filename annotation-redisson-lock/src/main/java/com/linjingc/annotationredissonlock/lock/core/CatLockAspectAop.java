package com.linjingc.annotationredissonlock.lock.core;

import com.linjingc.annotationredissonlock.lock.annotation.CatLock;
import com.linjingc.annotationredissonlock.lock.basiclock.Lock;
import com.linjingc.annotationredissonlock.lock.basiclock.LockFactory;
import com.linjingc.annotationredissonlock.lock.exception.CatLockInvocationException;
import com.linjingc.annotationredissonlock.lock.model.LockInfo;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * redis锁切面类
 * 用来包裹方法使用
 *
 * @author cxc
 * @date 2019年8月8日17:19:55
 */
@Aspect
@Component
//声明首先加载入spring
@Order(0)
@Log4j2
public class CatLockAspectAop {
    @Autowired
    LockFactory lockFactory;
    @Autowired
    private LockInfoProvider lockInfoProvider;


    /**
     * 获取当前线程获取到的锁
     */
    private ThreadLocal<Lock> currentThreadLock = new ThreadLocal<>();
    /**
     * 该锁是否已经处理释放操作
     */
    private ThreadLocal<LockRes> currentThreadLockRes = new ThreadLocal<>();

    /**
     * 方法 环绕  加锁
     *
     * @param joinPoint 切面
     * @param catLock   锁类型
     * @return
     * @throws Throwable
     */
    @Around(value = "@annotation(catLock)")
    public Object around(ProceedingJoinPoint joinPoint, CatLock catLock) throws Throwable {
        //获取切面信息
        //MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取自定义锁信息
        LockInfo lockInfo = lockInfoProvider.get(joinPoint, catLock);
        //根据工厂模式 获取到Lock
        Lock lock = lockFactory.getLock(lockInfo);
        //加锁
        boolean carryLock = lock.acquire();
        //获取锁时的超时处理
        if (!carryLock) {
            if (log.isWarnEnabled()) {
                log.warn("Timeout while acquiring Lock({})", lockInfo.getName());
            }

            //如果有自定义的超时策略
            if (!StringUtils.isEmpty(catLock.customLockTimeoutStrategy())) {
                return handleCustomLockTimeout(catLock.customLockTimeoutStrategy(), joinPoint);
            } else {
                //否则使用配置的超时处理策略
                catLock.lockTimeoutStrategy().handle(lockInfo, lock, joinPoint);
            }
        }
        currentThreadLock.set(lock);
        return joinPoint.proceed();
    }


    /**
     * 方法执行完毕 释放锁
     *
     * @param joinPoint
     * @param catLock
     * @throws Throwable
     */
    @AfterReturning(value = "@annotation(catLock)")
    public void afterReturning(JoinPoint joinPoint, CatLock catLock) throws Throwable {
        //释放锁
        releaseLock(catLock, joinPoint);
        //清理线程副本
        cleanUpThreadLocal();
    }

    /**
     * 切面 异常处理
     *
     * @param joinPoint
     * @param catLock
     * @param ex
     * @throws Throwable
     */
    @AfterThrowing(value = "@annotation(catLock)", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, CatLock catLock, Throwable ex) throws Throwable {
        //释放锁
        releaseLock(catLock, joinPoint);
        //清理线程副本
        cleanUpThreadLocal();
        throw ex;
    }


    /**
     * 释放锁 避免重复释放锁
     * 如: 执行完毕释放一次 throw时又释放一次
     */
    private void releaseLock(CatLock catLock, JoinPoint joinPoint) throws Throwable {
        LockRes lockRes = currentThreadLockRes.get();
        //未执行过释放锁操作
        if (!lockRes.getUseState()) {
            boolean releaseRes = currentThreadLock.get().release();
            // avoid release lock twice when exception happens below
            lockRes.setUseState(true);
            if (releaseRes) {
                handleReleaseTimeout(catLock, lockRes.getLockInfo(), joinPoint);
            }
        }
    }

    /**
     * 处理自定义加锁超时
     */
    private Object handleCustomLockTimeout(String lockTimeoutHandler, JoinPoint joinPoint) throws Throwable {

        // prepare invocation context
        Method currentMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method handleMethod = null;
        try {
            handleMethod = joinPoint.getTarget().getClass().getDeclaredMethod(lockTimeoutHandler, currentMethod.getParameterTypes());
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal annotation param customLockTimeoutStrategy", e);
        }
        Object[] args = joinPoint.getArgs();

        // invoke
        Object res = null;
        try {
            res = handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new CatLockInvocationException("Fail to invoke custom lock timeout handler: " + lockTimeoutHandler, e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

        return res;
    }


    /**
     * 处理释放锁时已超时
     */
    private void handleReleaseTimeout(CatLock catLock, LockInfo lockInfo, JoinPoint joinPoint) throws Throwable {

        if (log.isWarnEnabled()) {
            log.warn("Timeout while release Lock({})", lockInfo.getName());
        }

        if (!StringUtils.isEmpty(catLock.customReleaseTimeoutStrategy())) {

            handleCustomReleaseTimeout(catLock.customReleaseTimeoutStrategy(), joinPoint);

        } else {
            catLock.releaseTimeoutStrategy().handle(lockInfo);
        }
    }

    /**
     * 处理自定义释放锁时已超时
     */
    private void handleCustomReleaseTimeout(String releaseTimeoutHandler, JoinPoint joinPoint) throws Throwable {

        Method currentMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method handleMethod = null;
        try {
            handleMethod = joinPoint.getTarget().getClass().getDeclaredMethod(releaseTimeoutHandler, currentMethod.getParameterTypes());
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal annotation param customReleaseTimeoutStrategy", e);
        }
        Object[] args = joinPoint.getArgs();

        try {
            handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new CatLockInvocationException("Fail to invoke custom release timeout handler: " + releaseTimeoutHandler, e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * 清除当前线程副本
     */
    private void cleanUpThreadLocal() {
        currentThreadLockRes.remove();
        currentThreadLock.remove();
    }


    /**
     * 当前线程锁状态
     */
    @Data
    private class LockRes {

        private LockInfo lockInfo;
        /**
         * 当前锁是否执行释放操作过  true 执行 false 未执行
         */
        private Boolean useState;

        LockRes(LockInfo lockInfo, Boolean useState) {
            this.lockInfo = lockInfo;
            this.useState = useState;
        }
    }
}