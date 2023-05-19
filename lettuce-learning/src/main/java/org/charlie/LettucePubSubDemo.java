package org.charlie;

import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author liangchunli
 * @date 2023/5/15 17:50
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LettucePubSubDemo {

    private static StatefulRedisConnection<String, String> connection;

    private static RedisClient redisClient;

    static  {
        ClassLoader classLoader = LettucePubSubDemo.class.getClassLoader();
        InputStream is = classLoader.getResourceAsStream("config.properties");
        Properties properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        redisClient = RedisClient.create(RedisURI.builder()
                .withHost(properties.getProperty("host"))
                .withPort(6379)
                .withPassword(properties.getProperty("password").toCharArray())
                .build());
        connection = redisClient.connect();
    }

    @Test
    public void testSyncGet() {
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.setex("hello", 10, "Hello Charlie");
        System.out.println(syncCommands.get("hello"));
    }

    @Test
    public void testAsyncGet() throws ExecutionException, InterruptedException {
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        RedisFuture<String> future = asyncCommands.get("hello");
        while (!future.isDone()) {
            future.await(10, TimeUnit.MILLISECONDS);
        }
        System.out.println(future.get());
    }

    @Test
    public void testSet() throws ExecutionException, InterruptedException {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisStringAsyncCommands<String, String> async = connection.async();
        RedisFuture<String> set = async.set("key", "value");
        RedisFuture<String> get = async.get("key");

        boolean b = false;
        while (!b) {
            b = LettuceFutures.awaitAll(Duration.ofSeconds(1), set, get);
        }

        Assertions.assertEquals("OK", set.get());
        Assertions.assertEquals("value", get.get());
    }

    @Test
    public void testPubSub() {

    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        RedisPubSubCommands<String, String> commands = redisClient.connectPubSub().sync();
        commands.getStatefulConnection().addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, String message) {
                System.out.println("received message: " + message);
            }
        });

        commands.subscribe("TEST");

        countDownLatch.await();
    }

    @AfterAll
    public void destory() {
        connection.close();
        redisClient.shutdown();
    }
}