package com.linjingc.annotationredissonlock.lock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 这个注解用来标记 参数 使用key lock
 *
 * @author cxc
 * @date 2019年8月8日17:52:34
 */
@Target(value = {ElementType.PARAMETER, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface LockKey {
    String value() default "";
}
