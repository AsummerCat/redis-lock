package com.linjingc.simpleredislock.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * redis 锁 工具类
 *
 * @author cxc
 * @date 2019/8/4 21:59
 */
@Component
public class RedisLockUtil {

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 加入锁 如果没有过期时间默认为0
     *
     * @param key
     * @param value
     * @param time  秒
     * @return
     */
    public boolean setLock(String key, Object value, Long time) {
        return redisUtil.set(key, value, time == null ? 0 : time);
    }

    /**
     * 获取锁 是否存在
     *
     * @param key
     * @return
     */
    public boolean getLock(String key) {
        return redisUtil.hasKey(key);
    }

    /**
     * 获取过期时间
     *
     * @param key
     * @return
     */
    public Long getExpire(String key) {
        return redisUtil.getExpire(key);
    }

}
