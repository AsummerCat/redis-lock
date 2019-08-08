package com.linjingc.annotationredissonlock.lock.annotation;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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

    //环绕处理
    @Around(value = "@annotation(catLock)")
    public Object around(ProceedingJoinPoint joinPoint, CatLock catLock) throws Throwable {


        return joinPoint.proceed();
    }

}
