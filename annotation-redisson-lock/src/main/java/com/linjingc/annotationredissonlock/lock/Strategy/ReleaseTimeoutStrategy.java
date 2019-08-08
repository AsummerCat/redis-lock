package com.linjingc.annotationredissonlock.lock.Strategy;

import com.linjingc.annotationredissonlock.lock.exception.CatLockTimeoutException;
import com.linjingc.annotationredissonlock.lock.handler.ReleaseTimeoutHandler;
import com.linjingc.annotationredissonlock.lock.model.LockInfo;

/**
 * 释放超时的 策略
 *
 * @author cxc
 * @date 2019年8月8日18:21:28
 **/
public enum ReleaseTimeoutStrategy implements ReleaseTimeoutHandler {

    /**
     * 继续执行业务逻辑，不做任何处理
     */
    NO_OPERATION() {
        @Override
        public void handle(LockInfo lockInfo) {
            // do nothing
        }
    },
    /**
     * 快速失败
     */
    FAIL_FAST() {
        @Override
        public void handle(LockInfo lockInfo) {

            String errorMsg = String.format("Found Lock(%s) already been released while lock lease time is %d s", lockInfo.getName(), lockInfo.getLeaseTime());
            throw new CatLockTimeoutException(errorMsg);
        }
    }
}
