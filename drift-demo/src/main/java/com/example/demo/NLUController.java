package com.example.demo;

import com.naah69.rpc.drift.client.annotation.ThriftRefer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/nlu")
public class NLUController {

    @Autowired
    DiscoveryClient client;

    @ThriftRefer(version = "1.2.1")
    NLU nlu;

    private static final Logger LOGGER = LoggerFactory.getLogger(NLUController.class);
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
    PriorityBlockingQueue<Long> pbq=new PriorityBlockingQueue<>((int) (SECOND*THREAD_COUNT*2));



    @GetMapping("/status")
    public String status() {
        return nlu.status();
    }

    @PostMapping("/parse")
    public String parse(@RequestBody NluParam param) {
        return nlu.parse(param.getScene(), param.getBot_name(), param.getQ());
    }

    private String parse() {
        return nlu.parse("1001_2002", "1.0.0", "哈哈哈");
    }

    @GetMapping("/test")
    public String test() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    testClient();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
        return "test";
    }

    private void testClient() throws InterruptedException {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                COUNT.incrementAndGet();
                long startTime = System.currentTimeMillis();
                try {
                    //你的测试内容
                    String status = parse();

                    long endTime = System.currentTimeMillis();
                    long costTime = endTime - startTime;


                    SUM_TIME.addAndGet(costTime);
                    synchronized (NLUController.class) {
                        long min = MIN_TIME.get();
                        long max = MAX_TIME.get();
                        if (costTime < min) {
                            MIN_TIME.compareAndSet(min, costTime);
                        }
                        if (costTime > max) {
                            MAX_TIME.compareAndSet(max, costTime);
                        }
                    }
                    pbq.add(costTime);
                    if (StringUtils.isNotBlank(status)) {
//                        LOGGER.info("{}th success , cost time:{}ms , average time:{}ms , min time:{}ms , max time:{}ms , qps:{} , result:{}", SUCCESS_COUNT.incrementAndGet(), costTime, SUM_TIME.get() / COUNT.get(), MIN_TIME.get(), MAX_TIME.get(), SUCCESS_COUNT.get() / ((System.currentTimeMillis()-BEGIN_NAME)/1000),status);
                        LOGGER.info("{}th success , cost time:{}ms , average time:{}ms , min time:{}ms , max time:{}ms , qps:{}", SUCCESS_COUNT.incrementAndGet(), costTime, SUM_TIME.get() / COUNT.get(), MIN_TIME.get(), MAX_TIME.get(), SUCCESS_COUNT.get() / ((System.currentTimeMillis()-BEGIN_NAME)/1000));
                    } else {
                        LOGGER.error("{}th failed , cost time:{}ms , average time:{}ms , min time:{}ms , max time:{}ms , qps:{}", FAILED_COUNT.incrementAndGet(), costTime, SUM_TIME.get() / COUNT.get(), MIN_TIME.get(), MAX_TIME.get(), SUCCESS_COUNT.get() / ((System.currentTimeMillis()-BEGIN_NAME)/1000));
                    }

                } catch (Throwable e) {
                    long endTime = System.currentTimeMillis();
                    long costTime = endTime - startTime;
                    LOGGER.error("{}th  exception , cost time:{}ms , average time:{}ms , min time:{}ms , max time:{}ms , qps:{}", FAILED_COUNT.incrementAndGet(), costTime, SUM_TIME.get() / COUNT.get(), MIN_TIME.get(), MAX_TIME.get(), SUCCESS_COUNT.get() / ((System.currentTimeMillis()-BEGIN_NAME)/1000), e);
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

        Long[] costArray = pbq.toArray(new Long[pbq.size()]);
        Arrays.sort(costArray);
        int length=costArray.length;
        int ninety=(int)(length * 0.9);
        int ninetyFive=(int)(length * 0.95);
        int ninetyNine=(int)(length * 0.99);
        LOGGER.warn("minute:{} , thread:{} ", MINUTE, THREAD_COUNT);
        LOGGER.warn("count:{} , success:{} , failed:{} ", COUNT.get(), SUCCESS_COUNT.get(), FAILED_COUNT.get());
        LOGGER.warn("average time:{}ms , min time:{}ms , max time:{}ms", SUM_TIME.get() / COUNT.get(), MIN_TIME.get(), MAX_TIME.get());
        LOGGER.warn("90% below time:{}ms , 95% below time:{}ms , 99% below time:{}ms", costArray[ninety] ,costArray[ninetyFive], costArray[ninetyNine]);
        LOGGER.warn("qps:{} ", SUCCESS_COUNT.get() / (MINUTE * 60));
    }


}

class NluParam{
    private String scene;
    private String bot_name;
    private String q;

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getBot_name() {
        return bot_name;
    }

    public void setBot_name(String bot_name) {
        this.bot_name = bot_name;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }
}
