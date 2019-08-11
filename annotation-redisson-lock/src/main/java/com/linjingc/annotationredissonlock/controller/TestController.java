package com.linjingc.annotationredissonlock.controller;

import com.linjingc.annotationredissonlock.entity.User;
import com.linjingc.annotationredissonlock.lock.Strategy.LockTimeoutStrategy;
import com.linjingc.annotationredissonlock.lock.Strategy.ReleaseTimeoutStrategy;
import com.linjingc.annotationredissonlock.lock.annotation.CatLock;
import com.linjingc.annotationredissonlock.lock.annotation.LockKey;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

/**
 * 测试注解
 *
 * @author cxc
 * @date 2019/8/11 21:13
 */

@RestController
@Log4j2
public class TestController {
    @RequestMapping("/")
    public String index() {
        return "测试redis锁注解";
    }


    /**
     * 获取参数 keys param
     *
     * @param param
     * @return
     * @throws Exception
     */
    @RequestMapping("test1")
    @CatLock(waitTime = 10, leaseTime = 60, keys = {"#param"})
    public String getValue(@NotNull String param) throws Exception {
        //  if ("sleep".equals(param)) {//线程休眠或者断点阻塞，达到一直占用锁的测试效果
        Thread.sleep(1000 * 3);
        //}
        return "success";
    }

    /**
     * 默认参数 使用参数keys userId
     *
     * @param userId
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping("test2")
    @CatLock(keys = {"#userId"})
    public String getValue(String userId, @LockKey int id) throws Exception {
        Thread.sleep(60 * 1000);
        return "success";
    }

    /**
     * 获取多个参数 keys
     *
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping("test3")
    @CatLock(keys = {"#user.name", "#user.id"})
    public String getValue(User user) throws Exception {
        Thread.sleep(60 * 1000);
        return "success";
    }

    /**
     * 测试释放超时
     * leaseTime=-1 表示不超时
     */
    @CatLock(name = "test4", leaseTime = -1, releaseTimeoutStrategy = ReleaseTimeoutStrategy.FAIL_FAST)
    @RequestMapping("test4")
    public String test4() {
        try {
            log.info("foo1 acquire lock");
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "success";

    }

    /**
     * 测试加锁超时 快速失败
     */
    @CatLock(name = "test5", waitTime = 2, lockTimeoutStrategy = LockTimeoutStrategy.FAIL_FAST)
    @RequestMapping("test5")
    public String test5() {
        try {
            log.info("acquire lock");
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "success";

    }

    /**
     * 测试等待超时重试
     */
    @CatLock(name = "test6", waitTime = 2,lockTimeoutStrategy = LockTimeoutStrategy.KEEP_ACQUIRE)
    @RequestMapping("test6")
    public String test6() {
        try {
            TimeUnit.SECONDS.sleep(2);
            log.info("acquire lock");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "success";

    }

    @CatLock(name = "test7", waitTime = 2, customLockTimeoutStrategy = "customLockTimeout")
    @RequestMapping("test7")
    public String foo4(String foo, String bar) {
        try {
            TimeUnit.SECONDS.sleep(2);
            log.info("acquire lock");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 自定义超时处理
     *
     * @param foo
     * @param bar
     * @return
     */
    private String customLockTimeout(String foo, String bar) {
        log.info("customLockTimeout foo: " + foo + " bar: " + bar);
        return "custom foo: " + foo + " bar: " + bar;
    }

}
