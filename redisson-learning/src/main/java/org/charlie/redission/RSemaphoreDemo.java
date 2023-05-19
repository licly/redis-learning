package org.charlie.redission;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.redisson.Redisson;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author liangchunli
 * @date 2023/5/12 9:19
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RSemaphoreDemo {

    private RedissonClient redissonClient;

    private CountDownLatch latch = new CountDownLatch(10);

    @BeforeAll
    public void init() throws IOException {
        Config config = Config.fromYAML(ClassLoader.getSystemResourceAsStream("config.yml"));
        redissonClient = Redisson.create(config);
    }

    @Test
    public void test() throws InterruptedException {
        RSemaphore semaphore = redissonClient.getSemaphore("semaphore");
        // 设置资源数量
        semaphore.trySetPermits(3);

        for (int i = 0; i < 10; i++) {
            new Thread(new TestRunnable(semaphore)).start();
        }

        latch.await();
    }

    class TestRunnable implements Runnable {

        RSemaphore semaphore;

        TestRunnable(RSemaphore semaphore) {
            this.semaphore = semaphore;
        }

        @Override
        public void run() {
            while(true) {
                try {
                    semaphore.acquire();
                    System.out.println(Thread.currentThread());
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }
                latch.countDown();
            }
        }
    }
}