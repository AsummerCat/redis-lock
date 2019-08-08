package com.linjingc.annotationredissonlock.lock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 锁 自定义配置类
 *
 * @author cxc
 * @date 2019年8月8日18:00:25
 */
@Data
@Configuration
@ConfigurationProperties(prefix = RedisLockConfig.PREFIX)
public class RedisLockConfig {

    public static final String PREFIX = "linjingc.lock";
    //redisson
    private String address;
    private String password;
    //    private int database = 15;
    private ClusterServer clusterServer;
    private String codec = "org.redisson.codec.JsonJacksonCodec";
    //lock
    private long waitTime = 60;
    private long leaseTime = 60;


    public static class ClusterServer {

        private String[] nodeAddresses;

        public String[] getNodeAddresses() {
            return nodeAddresses;
        }

        public void setNodeAddresses(String[] nodeAddresses) {
            this.nodeAddresses = nodeAddresses;
        }
    }
}
