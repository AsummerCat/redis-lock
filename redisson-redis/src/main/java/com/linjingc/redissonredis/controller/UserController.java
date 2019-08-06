package com.linjingc.redissonredis.controller;

import org.redisson.RedissonMultiLock;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * @author cxc
 * @date 2019/8/4 21:57
 */
@RestController
public class UserController {

    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();


    @Autowired
    private RedissonClient redissonClient;

    @RequestMapping("/")
    public String index() {
        return "Redisson锁";
    }

    @RequestMapping("setkey")
    public String setKey() {
        RLock key = redissonClient.getLock("key");
        try {
            if (key.isLocked()) {
                System.out.println("锁住了");
            } else {
                //设置60秒自动释放锁  （默认是30秒自动过期）
                key.lock(60, TimeUnit.SECONDS);
                Thread.sleep(3000);
                System.out.println("设置key成功");
                key.unlock();
                System.out.println("解锁成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "SUCCESS";
    }

    /**
     * 可重入锁（Reentrant Lock）
     *
     * @return
     */
    @RequestMapping("reentrantLock")
    public String reentrantLock() {
        //创建10个线程进行
        for (int i = 0; i < 10; i++) {
            cachedThreadPool.execute(() -> {
                RLock lock = redissonClient.getLock("key1");
                try {
                    // 1. 最常见的使用方法
                    //lock.lock();
                    // 2. 支持过期解锁功能,10秒钟以后自动解锁, 无需调用unlock方法手动解锁
                    //lock.lock(10, TimeUnit.SECONDS);
                    // 3. 尝试加锁，最多等待3秒，上锁以后10秒自动解锁
                    boolean res = lock.tryLock(3, 10, TimeUnit.SECONDS);
                    if (res) {
                        System.out.println("可重入锁加锁成功");
                        Thread.sleep(4000);
                        lock.unlock();
                        System.out.println("可重入锁解锁成功");
                    } else {
                        System.out.println("可重入锁未获取到锁");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        return "SUCCESS";
    }

    /**
     * 公平锁（Fair Lock）
     * 公平锁会在redis里面 产生一个key的队列 用来标记  一个超时的Zset队列 一个排序的Zset队列
     */
    @RequestMapping("fairLock")
    public String fairLock() {
        //创建10个线程进行
        for (int i = 0; i < 10; i++) {
            cachedThreadPool.execute(() -> {
                RLock lock = redissonClient.getFairLock("key1");
                try {
                    boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
                    if (res) {
                        System.out.println("公平锁加锁成功");
                        Thread.sleep(4000);
                        lock.unlock();
                        System.out.println("公平锁解锁成功");
                    } else {
                        System.out.println("公平锁未获取到锁");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        return "SUCCESS";
    }

    /**
     * 联锁（MultiLock）
     * Redisson的RedissonMultiLock对象可以将多个RLock对象关联为一个联锁，每个RLock对象实例可以来自于不同的Redisson实例。
     * 同时加锁：lock1 lock2 lock3, 所有的锁都上锁成功才算成功
     */
    @RequestMapping("multiLock")
    public String multiLock() {
        //创建10个线程进行
        for (int i = 0; i < 10; i++) {
            cachedThreadPool.execute(() -> {
                RLock lock1 = redissonClient.getLock("key1");
                RLock lock2 = redissonClient.getLock("key2");
                RLock lock3 = redissonClient.getLock("key3");
                //创建联锁  同时加锁：lock1 lock2 lock3, 所有的锁都上锁成功才算成功
                RedissonMultiLock lock = new RedissonMultiLock(lock1, lock2, lock3);
                try {
                    boolean res = lock.tryLock(15, 10, TimeUnit.SECONDS);
                    if (res) {
                        System.out.println("联锁加锁成功");
                        Thread.sleep(4000);
                        lock.unlock();
                        System.out.println("联锁解锁成功");
                    } else {
                        System.out.println("联锁未获取到锁");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        return "SUCCESS";
    }

    /**
     * 红锁（RedLock）
     * 该对象也可以用来将多个RLock对象关联为一个红锁，每个RLock对象实例可以来自于不同的Redisson实例。
     * 需要注意的是 :如果Client获得锁的数量不足一半以上，或获得锁的时间超时，那么认为获得锁失败。
     */
    @RequestMapping("redLock")
    public String redLock() {
        //创建10个线程进行
        for (int i = 0; i < 10; i++) {
            cachedThreadPool.execute(() -> {
                RLock lock1 = redissonClient.getLock("key1");
                RLock lock2 = redissonClient.getLock("key2");
                RLock lock3 = redissonClient.getLock("key3");
                //创建红锁  同时加锁：lock1 lock2 lock3, 红锁在一半以上部分节点上加锁成功就算成功;
                RedissonRedLock lock = new RedissonRedLock(lock1, lock2, lock3);
                try {
                    boolean res = lock.tryLock(15, 10, TimeUnit.SECONDS);
                    if (res) {
                        System.out.println("红锁加锁成功");
                        Thread.sleep(4000);
                        lock.unlock();
                        System.out.println("红锁解锁成功");
                    } else {
                        System.out.println("红锁未获取到锁");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        return "SUCCESS";
    }

    /**
     * 读写锁（ReadWriteLock） 写
     * 一个读写锁同时只能存在一个写锁但是可以存在多个读锁，但不能同时存在写锁和读锁。
     */
    @RequestMapping("readWriteLock")
    public String readWriteLock() {
        //创建10个线程进行
        for (int i = 0; i < 10; i++) {
            cachedThreadPool.execute(() -> {
                RReadWriteLock lock = redissonClient.getReadWriteLock("key1");
                //创建红锁  同时加锁：lock1 lock2 lock3, 红锁在一半以上部分节点上加锁成功就算成功;
                try {
                    boolean res = lock.writeLock().tryLock(15, 10, TimeUnit.SECONDS);
                    if (res) {
                        System.out.println("读写锁写锁加锁成功");
                        Thread.sleep(4000);
                        lock.writeLock().unlock();
                        System.out.println("读写锁写锁解锁成功");
                    } else {
                        System.out.println("读写锁写锁未获取到锁");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        return "SUCCESS";
    }

    /**
     * 读写锁（ReadWriteLock）读
     * 一个读写锁同时只能存在一个写锁但是可以存在多个读锁，但不能同时存在写锁和读锁。
     */
    @RequestMapping("readWriteLockOfRead")
    public String readWriteLockOfRead() {
        //创建10个线程进行

        for (int i = 0; i < 10; i++) {
            cachedThreadPool.execute(() -> {
                RReadWriteLock lock = redissonClient.getReadWriteLock("key1");
                //创建红锁  同时加锁：lock1 lock2 lock3, 红锁在一半以上部分节点上加锁成功就算成功;
                try {
                    boolean res = lock.readLock().tryLock(15, 10, TimeUnit.SECONDS);
                    if (res) {
                        System.out.println("读写锁读锁加锁成功");
                        Thread.sleep(4000);
                        lock.readLock().unlock();
                        System.out.println("读写锁读锁解锁成功");
                    } else {
                        System.out.println("读写锁读锁未获取到锁");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        return "SUCCESS";
    }
}
