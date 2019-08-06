package com.linjingc.redissonredis.controller;

import org.redisson.RedissonMultiLock;
import org.redisson.RedissonRedLock;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    /**
     * 信号量（Semaphore）
     * RSemaphore
     */
    @RequestMapping("SemaphoreLock")
    public String semaphoreLock() {
        //创建10个线程进行
        //创建信号量
        RSemaphore semaphore = redissonClient.getSemaphore("semaphore");
        //设置许可数量
        semaphore.trySetPermits(20);


        for (int i = 0; i < 40; i++) {
            cachedThreadPool.execute(() -> {
                try {
                    //获得一个许可
                    semaphore.acquire();
                    System.out.println("获取到许可" + new Date());
                    Thread.sleep(4000);
                    //释放一个许可
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //移除信号量
//                semaphore.unlink();
            });
        }
        return "SUCCESS";
    }


    /**
     * 可过期性信号量（PermitExpirableSemaphore）
     * Redisson的可过期性信号量（PermitExpirableSemaphore）实在RSemaphore对象的基础上，
     * 为每个信号增加了一个过期时间。每个信号可以通过独立的ID来辨识，释放时只能通过提交这个ID才能释放。
     */
    @RequestMapping("PermitExpirableSemaphoreLock")
    public String permitExpirableSemaphoreLock() {
        //创建10个线程进行
        //创建信号量
        RPermitExpirableSemaphore semaphore = redissonClient.getPermitExpirableSemaphore("semaphore1");
        semaphore.trySetPermits(5);

        for (int i = 0; i < 40; i++) {
            cachedThreadPool.execute(() -> {
                try {
                    //获取一个信号，有效期只有2秒钟。
                    String acquire = semaphore.acquire(2, TimeUnit.SECONDS);
                    //获取一个信号，等待只有3秒钟。
                    //  String acquire = semaphore.tryAcquire(3, TimeUnit.SECONDS);
                    //获取一个信号，等待只有3秒钟,有效期只有2秒钟;
//                      String acquire = semaphore.tryAcquire(3,2, TimeUnit.SECONDS);

                    Thread.sleep(4000);
                    System.out.println("获取到许可" + new Date());
                    //释放一个许可
                    semaphore.release(acquire);
                } catch (InterruptedException e) {
                    System.out.println("线程中断 超出有效时间");
                }
                //移除信号量
//                semaphore.unlink();
            });
        }
        return "SUCCESS";
    }


    /**
     * 闭锁（CountDownLatch） 类似发令枪
     * Redisson的分布式闭锁（CountDownLatch）Java对象RCountDownLatch采用了与java.util.concurrent.CountDownLatch相似的接口和用法。
     */
    @RequestMapping("CountDownLatchLock")
    public String countDownLatchLock() throws InterruptedException {
        //创建10个线程进行
        //创建发令枪
        RCountDownLatch countDownLatchTest = redissonClient.getCountDownLatch("CountDownLatchTest");
        //设置发令枪数量;
        countDownLatchTest.trySetCount(10);

        //await
        for (int i = 0; i < 40; i++) {
            cachedThreadPool.execute(() -> {
                try {
                    //等待发令枪

                    System.out.println("等待发令枪" + new Date());
                    countDownLatchTest.await();
                    System.out.println("起跑" + new Date());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        //计数器
        for (int i = 0; i < 10; i++) {
            cachedThreadPool.execute(() -> {
                try {
                    Thread.sleep(new Random().nextInt(10+1)*1000);
                    System.out.println("准备好了" + new Date());
                    countDownLatchTest.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        //等待发令枪
        countDownLatchTest.await();
        //移除发令枪
        System.out.println("出发成功");
        countDownLatchTest.unlink();
        return "SUCCESS";
    }
}
