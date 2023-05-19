package org.charlie.redission;

import org.assertj.core.util.Files;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.config.Config;

import java.io.IOException;

/**
 * Quick start 示例
 *
 * @author liangchunli
 * @date 2023/5/19 17:00
 */
public class QuickStartDemo {

    public static void main(String[] args) throws IOException {
        // create config object
        // Config config = new Config();
        // config.useSingleServer().setAddress("redis://localhost:6379");
        // System.out.println(config.toYAML());

        // read config from file
        Config config = Config.fromYAML(ClassLoader.getSystemResourceAsStream("config.yml"));

        // 2. Create Redisson instance
        // Sync and async API
        RedissonClient redissonClient = Redisson.create(config);
        // reactive API
        RedissonReactiveClient reactiveClient = redissonClient.reactive();
        // RxJava3 API
        RedissonRxClient redissonRxClient = redissonClient.rxJava();

        // 3. Get Redis based implementation of java.util.concurrent.ConcurrentMap
        RMap<String, String> map = redissonClient.getMap("myMap");

        RMapReactive<String, String> mapReactive = reactiveClient.getMap("myMap");

        RMapRx<String, String> mapRx = redissonRxClient.getMap("myMap");

        // 4. Get Redis based implementation of java.util.concurrent.locks.Lock
        RLock lock = redissonClient.getLock("myLock");

        RLockReactive lockReactive = reactiveClient.getLock("myLock");

        RLockRx lockRx = redissonRxClient.getLock("myLock");

        // 4. Get Redis based implementation of java.util.concurrent.ExecutorService
        RExecutorService executor = redissonClient.getExecutorService("myExecutorService");
    }
}