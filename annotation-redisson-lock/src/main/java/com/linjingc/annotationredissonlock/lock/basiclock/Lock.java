package com.linjingc.annotationredissonlock.lock.basiclock;

/**
 * 锁接口
 * 自定义的锁类型都需要实现该接口的内容
 *
 * @author cxc
 * @date 2019年8月8日17:59:04
 */
public interface Lock {

    /**
     * 加锁
     *
     * @return
     */
    boolean acquire();

    /**
     * 解锁
     *
     * @return
     */
    boolean release();

    /**
     * 当前锁是否被释放过
     */


}

