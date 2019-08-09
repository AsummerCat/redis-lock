package com.linjingc.annotationredissonlock.lock.annotation;

import com.linjingc.annotationredissonlock.lock.basiclock.Lock;
import com.linjingc.annotationredissonlock.lock.basiclock.LockFactory;
import com.linjingc.annotationredissonlock.lock.exception.CatLockInvocationException;
import com.linjingc.annotationredissonlock.lock.model.LockInfo;
import com.linjingc.annotationredissonlock.lock.priovider.LockInfoProvider;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
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


    @Around(value = "@annotation(catLock)")
    public Object around(ProceedingJoinPoint joinPoint, CatLock catLock) throws Throwable {
        //获取切面信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
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

            //如果没有自定义的超时策略
            if (!StringUtils.isEmpty(catLock.customLockTimeoutStrategy())) {

                return handleCustomLockTimeout(catLock.customLockTimeoutStrategy(), joinPoint);

            } else {
                catLock.lockTimeoutStrategy().handle(lockInfo, lock, joinPoint);
            }
        }
        currentThreadLock.set(lock);
        return joinPoint.proceed();
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
}
