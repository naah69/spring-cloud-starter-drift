package com.example.demo;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DriftStarterJmeterApplicationTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(DriftStarterJmeterApplicationTests.class);

    @Test
    public void contextLoads() {
    }


    static final long MINUTE = 30L;
    static final long SECOND = MINUTE * 60;
    static final long MILLISECOND = MINUTE * 60 * 1000;
    static final int THREAD_COUNT = 250;

    static final AtomicLong SUCCESS_COUNT = new AtomicLong();
    static final AtomicLong FAILED_COUNT = new AtomicLong();
    static final AtomicLong COUNT = new AtomicLong();
    static final AtomicLong JOB_COUNT = new AtomicLong();
    static final AtomicLong SUM_TIME = new AtomicLong();
    static final AtomicLong MIN_TIME = new AtomicLong(Long.MAX_VALUE);
    static final AtomicLong MAX_TIME = new AtomicLong(Long.MIN_VALUE);
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

    @Test
    public void MultiRequestsTest() throws InterruptedException {
        DemoApplication.getStatus();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                COUNT.incrementAndGet();
                long startTime = System.currentTimeMillis();
                try {
                    //你的测试内容
                    String status = DemoApplication.getStatus();

                    long endTime = System.currentTimeMillis();
                    long costTime = endTime - startTime;


                    SUM_TIME.addAndGet(costTime);
                    synchronized (DriftStarterJmeterApplicationTests.class) {
                        long min = MIN_TIME.get();
                        long max = MAX_TIME.get();
                        if (costTime < min) {
                            MIN_TIME.compareAndSet(min, costTime);
                        }
                        if (costTime > max) {
                            MAX_TIME.compareAndSet(max, costTime);
                        }
                    }

                    if (StringUtils.isNotBlank(status)) {
                        LOGGER.info("{}th success , cost time:{}ms , average time:{}ms , min time:{}ms , max time:{}ms , qps:{}", SUCCESS_COUNT.incrementAndGet(), costTime, SUM_TIME.get() / COUNT.get(), MIN_TIME.get(), MAX_TIME.get(), SUCCESS_COUNT.get() / (MINUTE * 60));
                    } else {
                        LOGGER.error("{}th failed , cost time:{}ms , average time:{}ms , min time:{}ms , max time:{}ms , qps:{}", FAILED_COUNT.incrementAndGet(), costTime, SUM_TIME.get() / COUNT.get(), MIN_TIME.get(), MAX_TIME.get(), SUCCESS_COUNT.get() / (MINUTE * 60));
                    }

                } catch (Throwable e) {
                    long endTime = System.currentTimeMillis();
                    long costTime = endTime - startTime;
                    LOGGER.error("{}th  exception , cost time:{}ms , average time:{}ms , min time:{}ms , max time:{}ms , qps:{}", FAILED_COUNT.incrementAndGet(), costTime, SUM_TIME.get() / COUNT.get(), MIN_TIME.get(), MAX_TIME.get(), SUCCESS_COUNT.get() / (MINUTE * 60), e);
                    e.printStackTrace();
                }
            }
        };


        Runnable job = new Runnable() {
            @Override
            public void run() {
                long jobCount = JOB_COUNT.incrementAndGet();

                if (jobCount <= SECOND) {

                    for (int i = 0; i < THREAD_COUNT; i++) {
                        executorService.submit(runnable);
                    }
                }
            }
        };

        scheduledExecutorService.scheduleAtFixedRate(job, 0L, 1L, TimeUnit.SECONDS);


//        Thread.sleep(Long.MAX_VALUE);
        Thread.sleep(MILLISECOND);

        LOGGER.trace("minute:{} , thread:{} ", MINUTE, THREAD_COUNT);
        LOGGER.trace("count:{} , success:{} , failed:{} ", COUNT.get(), SUCCESS_COUNT.get(), FAILED_COUNT.get());
        LOGGER.trace("average time:{}ms , min time:{}ms , max time:{}ms", SUM_TIME.get() / COUNT.get(), MIN_TIME.get(), MAX_TIME.get());
        LOGGER.trace("qps:{} ", SUCCESS_COUNT.get() / (MINUTE * 60));

    }


}

