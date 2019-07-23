package com.lyl.study.trainning.engine.core.test.dispatch;

import com.lyl.study.trainning.engine.core.exception.UnsupportedMessageTypeException;
import com.lyl.study.trainning.engine.core.rpc.dispatch.Consumer;
import com.lyl.study.trainning.engine.core.rpc.dispatch.ThreadPoolExecutorDispatcher;
import com.lyl.study.trainning.engine.core.util.Assert;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadPoolExecutorDispatcher单元测试类
 *
 * @author liyilin
 */
@Slf4j
public class ThreadPoolExecutorDispatcherTest {

    @Test
    public void testParallel() throws InterruptedException {
        final int poolSize = 4;
        final int backlog = 10;
        final int taskNum = poolSize + backlog;
        final long duration = 200L;
        final AtomicInteger currentParallelNum = new AtomicInteger(0);
        final AtomicInteger maxParallelNum = new AtomicInteger(0);
        final CountDownLatch countDownLatch = new CountDownLatch(taskNum);

        Consumer testConsumer = new Consumer() {
            @Override
            public boolean acceptable(Object content) {
                return true;
            }

            @Override
            public void accept(Object content) {
                int num = currentParallelNum.incrementAndGet();
                int max = maxParallelNum.get();
                maxParallelNum.compareAndSet(max, Math.max(max, num));
                Assert.isTrue(((ThreadPoolExecutorDispatcher) content).inContext(), "当前线程应该处于调度器上下文中");

                log.info("开始执行任务");
                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("任务执行完毕");
                currentParallelNum.decrementAndGet();
                countDownLatch.countDown();
            }
        };

        ThreadPoolExecutorDispatcher dispatcher = new ThreadPoolExecutorDispatcher(
                poolSize, backlog, Collections.singletonList(testConsumer));

        for (int i = 0; i < taskNum; i++) {
            dispatcher.dispatch(dispatcher);
        }

        Assert.isTrue(!dispatcher.inContext(), "当前线程不应在调度器线程上下文");

        countDownLatch.await();

        log.info("最大并行数: {}", maxParallelNum.get());
        Assert.isTrue(maxParallelNum.get() == 4, "并行执行的任务数异常");
    }

    @Test
    public void testReject() {
        final int poolSize = 2;
        final int backlog = 5;
        final int expectRejectNum = 10;
        final int taskNum = poolSize + backlog + expectRejectNum;
        final long duration = 10000L;
        final AtomicInteger rejectNum = new AtomicInteger(0);

        Consumer testConsumer = new Consumer() {
            @Override
            public boolean acceptable(Object content) {
                return true;
            }

            @Override
            public void accept(Object content) {
                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        ThreadPoolExecutorDispatcher dispatcher = new ThreadPoolExecutorDispatcher(
                poolSize, backlog, Collections.singletonList(testConsumer),
                (r, executor) -> rejectNum.incrementAndGet());

        Assert.isTrue(dispatcher.getBacklog() == backlog, "backlogSize不正确");

        for (int i = 0; i < taskNum; i++) {
            dispatcher.dispatch(i);
        }

        log.info("rejectNum={}", rejectNum.get());
        Assert.isTrue(rejectNum.get() == expectRejectNum, "rejectNum不正确");
    }

    @Test
    public void testShutdown() throws InterruptedException {
        final int poolSize = 4;
        final int backlog = 8;
        final int taskNum = poolSize + backlog;
        final long duration = 200L;
        final AtomicInteger finishTaskNum = new AtomicInteger(0);

        Consumer testConsumer = new Consumer() {
            @Override
            public boolean acceptable(Object content) {
                return true;
            }

            @Override
            public void accept(Object content) {
                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finishTaskNum.incrementAndGet();
            }
        };

        ThreadPoolExecutorDispatcher dispatcher = new ThreadPoolExecutorDispatcher(
                poolSize, backlog, Collections.singletonList(testConsumer));

        for (int i = 0; i < taskNum; i++) {
            dispatcher.dispatch(i);
        }

        dispatcher.shutdown();
        dispatcher.awaitAndShutdown();

        log.info("finishTaskNum={}", finishTaskNum.get());
        Assert.isTrue(finishTaskNum.get() == taskNum, "任务完成数有误");
    }

    @Test
    public void testForceShutdown() throws InterruptedException {
        final int poolSize = 4;
        final int backlog = 8;
        final int taskNum = poolSize + backlog;
        final long duration = 10000L;
        final AtomicInteger finishTaskNum = new AtomicInteger(0);
        final AtomicInteger interruptTaskNum = new AtomicInteger(0);

        Consumer testConsumer = new Consumer() {
            @Override
            public boolean acceptable(Object content) {
                return true;
            }

            @Override
            public void accept(Object content) {
                log.info("开始执行任务: {}", content);
                try {
                    Thread.sleep(duration);
                    log.info("任务执行完毕: {}", content);
                    finishTaskNum.incrementAndGet();
                } catch (InterruptedException e) {
                    log.info("任务被中断: {}", content);
                    interruptTaskNum.incrementAndGet();
                }
            }
        };

        ThreadPoolExecutorDispatcher dispatcher = new ThreadPoolExecutorDispatcher(
                poolSize, backlog, Collections.singletonList(testConsumer));

        for (int i = 0; i < taskNum; i++) {
            dispatcher.dispatch(i);
        }

        dispatcher.forceShutdown();
        dispatcher.awaitAndShutdown();

        log.info("finishTaskNum={}", finishTaskNum.get());
        log.info("interruptTaskNum={}", interruptTaskNum.get());
        Assert.isTrue(finishTaskNum.get() == 0, "任务完成数有误");
        Assert.isTrue(interruptTaskNum.get() == poolSize, "任务中断数有误");
    }

    @Test(expected = UnsupportedMessageTypeException.class)
    public void testUnsupportedConsumer() {
        final int poolSize = 4;
        final int backlog = 8;


        Consumer testConsumer = new Consumer() {
            @Override
            public boolean acceptable(Object content) {
                return false;
            }

            @Override
            public void accept(Object content) {
                Assert.isTrue(false, "Can't go here!");
            }
        };

        ThreadPoolExecutorDispatcher dispatcher = new ThreadPoolExecutorDispatcher(
                poolSize, backlog, Collections.singletonList(testConsumer));

        dispatcher.dispatch("abc");
    }

    @Test(expected = UnsupportedMessageTypeException.class)
    public void testEmptyConsumer() {
        ThreadPoolExecutorDispatcher dispatcher = new ThreadPoolExecutorDispatcher(
                1, 1, null);
        dispatcher.dispatch("abc");
    }

    @Test(expected = IllegalStateException.class)
    public void testActive() throws InterruptedException {
        ThreadPoolExecutorDispatcher dispatcher = new ThreadPoolExecutorDispatcher(
                1, 1, null);
        dispatcher.shutdown();
        dispatcher.awaitAndShutdown();
        dispatcher.dispatch("abc");
    }
}
