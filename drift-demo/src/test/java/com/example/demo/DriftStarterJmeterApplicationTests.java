package com.example.demo;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DriftStarterJmeterApplicationTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(DriftStarterJmeterApplicationTests.class);

    static final long MINUTE = 5L;
    static final long SECOND = MINUTE * 60;
    static final long MILLISECOND = MINUTE * 60 * 1000;
    static final int THREAD_COUNT = 200;
    public static final long PERIOD = 400L;
    public static final long TOTAL_JOB_COUNT = (MILLISECOND) / PERIOD;
    static final Long BEGIN_NAME = System.currentTimeMillis();

    static final AtomicLong SUCCESS_COUNT = new AtomicLong();
    static final AtomicLong FAILED_COUNT = new AtomicLong();
    static final AtomicLong COUNT = new AtomicLong();
    static final AtomicLong JOB_COUNT = new AtomicLong();
    static final AtomicLong SUM_TIME = new AtomicLong();
    static final AtomicLong MIN_TIME = new AtomicLong(Long.MAX_VALUE);
    static final AtomicLong MAX_TIME = new AtomicLong(Long.MIN_VALUE);
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    PriorityBlockingQueue<Long> pbq = new PriorityBlockingQueue<>((int) (SECOND * THREAD_COUNT * 2));

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
                    String status = DemoApplication.parse();

                    long endTime = System.currentTimeMillis();
                    long costTime = endTime - startTime;


                    pbq.add(costTime);
                    boolean isSucced = StringUtils.isNotBlank(status);
                    if (isSucced) {
//                        LOGGER.info("{}th success , cost time:{}ms , average time:{}ms , min time:{}ms , max time:{}ms , qps:{} , result:{}", SUCCESS_COUNT.incrementAndGet(), costTime, SUM_TIME.get() / COUNT.get(), MIN_TIME.get(), MAX_TIME.get(), SUCCESS_COUNT.get() / ((System.currentTimeMillis()-BEGIN_NAME)/1000),status);
                        LOGGER.info("{}th success , cost time:{}ms , average time:{}ms , min time:{}ms , max time:{}ms , qps:{}", SUCCESS_COUNT.incrementAndGet(), costTime, SUM_TIME.get() / COUNT.get(), MIN_TIME.get(), MAX_TIME.get(), SUCCESS_COUNT.get() / ((System.currentTimeMillis() - BEGIN_NAME) / 1000));
                    } else {
                        LOGGER.error("{}th failed , cost time:{}ms , average time:{}ms , min time:{}ms , max time:{}ms , qps:{}", FAILED_COUNT.incrementAndGet(), costTime, SUM_TIME.get() / COUNT.get(), MIN_TIME.get(), MAX_TIME.get(), SUCCESS_COUNT.get() / ((System.currentTimeMillis() - BEGIN_NAME) / 1000));
                    }

                    if (isSucced) {
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
                    }

                } catch (Throwable e) {
                    long endTime = System.currentTimeMillis();
                    long costTime = endTime - startTime;
                    LOGGER.error("{}th  exception , cost time:{}ms , average time:{}ms , min time:{}ms , max time:{}ms , qps:{}", FAILED_COUNT.incrementAndGet(), costTime, SUM_TIME.get() / COUNT.get(), MIN_TIME.get(), MAX_TIME.get(), SUCCESS_COUNT.get() / ((System.currentTimeMillis() - BEGIN_NAME) / 1000), e);
                    e.printStackTrace();
                }
            }
        };


        Runnable job = new Runnable() {
            @Override
            public void run() {
                long jobCount = JOB_COUNT.incrementAndGet();

                if (jobCount <= TOTAL_JOB_COUNT) {

                    for (int i = 0; i < THREAD_COUNT; i++) {
                        executorService.submit(runnable);
                    }
                }
            }
        };

        scheduledExecutorService.scheduleAtFixedRate(job, 0L, PERIOD, TimeUnit.MILLISECONDS);


//        Thread.sleep(Long.MAX_VALUE);
        Thread.sleep(MILLISECOND);
        scheduledExecutorService.shutdown();
        while (SUCCESS_COUNT.get() + FAILED_COUNT.get() != COUNT.get()) {
            Thread.sleep(1000L);
        }
        Long[] costArray = pbq.toArray(new Long[pbq.size()]);
        Long[] array = pbq.toArray(new Long[pbq.size()]);
        Arrays.sort(costArray);
        int length = costArray.length;
        int ninety = (int) (length * 0.9);
        int ninetyFive = (int) (length * 0.95);
        int ninetyNine = (int) (length * 0.99);
        long averageTime = SUM_TIME.get() / COUNT.get();
        int averageTimeIndexInArray = 0;
        for (int i = 0; i < length; i++) {
            averageTimeIndexInArray = i;
            if (costArray[i] > averageTime) {
                break;
            }
        }
        double belowAverageTimePercent=(double)averageTimeIndexInArray/(double)length*100;
        LOGGER.warn("minute:{} , thread:{} ", MINUTE, THREAD_COUNT);
        LOGGER.warn("count:{} , success:{} , failed:{} ", COUNT.get(), SUCCESS_COUNT.get(), FAILED_COUNT.get());
        LOGGER.warn("average time:{}ms , min time:{}ms , max time:{}ms", averageTime, MIN_TIME.get(), MAX_TIME.get());
        LOGGER.warn("{}% below average time , 90% below time:{}ms , 95% below time:{}ms , 99% below time:{}ms", belowAverageTimePercent,costArray[ninety], costArray[ninetyFive], costArray[ninetyNine]);
        LOGGER.warn("qps:{} ", SUCCESS_COUNT.get() / (MINUTE * 60));

        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < length; i++) {

            sb.append(array[i]).append("\n");
        }

        File file=new File("/Users/naah/Documents/projects/spring-cloud-starter-drift/drift-demo/src/main/resources"+ LocalDateTime.now()+".cvs");
        try(BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
            bw.write(sb.toString());
            bw.flush();
        }catch (Exception e){

        }

    }


}

