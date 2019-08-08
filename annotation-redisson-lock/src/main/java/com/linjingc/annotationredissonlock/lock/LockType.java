package com.linjingc.annotationredissonlock.lock;

public enum LockType {
    /**
     * 可重入锁
     */
    Reentrant,
    /**
     * 公平锁
     */
    Fair,
    /**
     * 读锁
     */
    Read,
    /**
     * 写锁
     */
    Write,

    /**
     * 红锁
     */

    /**
     * 联锁
     */
    MultiLock,


    /**
     * 红锁
     */
    RedLock;

    LockType() {
    }

}