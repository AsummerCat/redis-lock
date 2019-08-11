package com.linjingc.annotationredissonlock.lock.exception;


/**
 * 自定义加锁超时错误
 * @author cxc
 * @date 2019年8月8日18:16:08
 */
public class CatLockTimeoutException extends RuntimeException {

    public CatLockTimeoutException() {
    }

    public CatLockTimeoutException(String message) { super(message); }

    public CatLockTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
