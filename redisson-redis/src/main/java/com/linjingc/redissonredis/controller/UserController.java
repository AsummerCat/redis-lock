package com.linjingc.redissonredis.controller;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author cxc
 * @date 2019/8/4 21:57
 */
@RestController
public class UserController {

    @Autowired
    private RedissonClient redissonClient;

    @RequestMapping("/")
    public String index() {
        return "Redisson锁";
    }

    @RequestMapping("setkey")
    public void setKey() throws InterruptedException {
      try{
          RLock key = redissonClient.getLock("key");
          key.lock(60, TimeUnit.SECONDS); //设置60秒自动释放锁  （默认是30秒自动过期）
                  if(key.isLocked()){
                      System.out.println("锁住了");

                  }else{
                      Thread.sleep(3000);
                      System.out.println("设置key成功");
                      key.unlock();
                  }
      }catch (Exception e){
          System.out.println("设置key失败");
      }
        //return "设置key成功";
    }


}
