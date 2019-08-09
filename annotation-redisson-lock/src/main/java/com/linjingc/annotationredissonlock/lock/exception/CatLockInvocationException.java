package com.linjingc.annotationredissonlock.lock.exception;


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
