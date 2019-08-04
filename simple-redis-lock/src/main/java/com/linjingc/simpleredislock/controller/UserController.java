package com.linjingc.simpleredislock.controller;

import com.linjingc.simpleredislock.utils.RedisLockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cxc
 * @date 2019/8/4 21:57
 */
@RestController
public class UserController {
    @Autowired
    private RedisLockUtil redisLockUtil;

    @RequestMapping("/")
    public String index() {
        return "简单版reis锁";
    }

    @RequestMapping("setkey")
    public String setKey() throws InterruptedException {
        //这边while 是会无限访问是否有锁的
        //后期可以处理为try lock 模式
        while (redisLockUtil.getLock("user")) {
            System.out.println("已经存在锁");
            Long expireTime = redisLockUtil.getExpire("user");
            System.out.println("还有" + expireTime + "秒过期!!");
            //设置1秒访问一次 查看是否能加锁
            Thread.sleep(1000L);
        }
        redisLockUtil.setLock("user", "小明", 60L);
        System.out.println("设置key成功");
        return "设置key成功";
    }


}
