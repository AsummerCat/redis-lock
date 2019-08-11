package com.linjingc.annotationredissonlock.lock.exception;

/**
 * 处理锁超时错误
 * @author cxc
 * @date 2019年8月8日18:16:08
 */
public class CatLockInvocationException extends RuntimeException {

    public CatLockInvocationException() {
    }

    public CatLockInvocationException(String message) {
        super(message);
    }

    public CatLockInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
