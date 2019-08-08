package com.linjingc.annotationredissonlock.lock.handler;

import com.linjingc.annotationredissonlock.lock.model.LockInfo;

/**
 * 处理锁超时的处理逻辑接口
 *
 * @author cxc
 * @since 2019年8月8日18:19:18
 **/
public interface ReleaseTimeoutHandler {

    /**
     * 处理
     *
     * @param lockInfo 锁信息
     */
    void handle(LockInfo lockInfo);
}
