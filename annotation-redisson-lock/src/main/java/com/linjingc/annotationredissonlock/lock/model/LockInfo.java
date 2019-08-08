package com.linjingc.annotationredissonlock.lock.model;

import com.linjingc.annotationredissonlock.lock.LockType;
import lombok.Data;

/**
 * 锁基本 信息
 *
 * @author cxc
 * @date 2019年8月8日17:13:18
 */
@Data
public class LockInfo {

    /**
     * 锁类型
     */
    private LockType type;
    /**
     * 锁名称
     */
    private String name;
    /**
     * 等待时间
     */
    private long waitTime;
    /**
     * 续约时间 ->处理时间 达到该时间会自动解锁
     */
    private long leaseTime;

    public LockInfo() {
    }

    public LockInfo(LockType type, String name, long waitTime, long leaseTime) {
        this.type = type;
        this.name = name;
        this.waitTime = waitTime;
        this.leaseTime = leaseTime;
    }

}
