package com.lyl.study.trainning.engine.core.test;

import com.lyl.study.training.engine.core.task.ConsumeResult;
import com.lyl.study.training.engine.core.task.TaskExecution;
import com.lyl.study.training.engine.core.task.TaskExecutor;
import com.lyl.study.training.engine.core.task.ThreadPoolTaskExecutor;
import com.lyl.study.training.engine.core.util.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * ThreadPoolTaskQueue单元测试类
 *
 * @author liyilin
 */
public class ThreadPoolTaskExecutorTest {
    @Test(timeout = 30000L)
    public void testRunTask() throws InterruptedException {
        final AtomicInteger runCount = new AtomicInteger(0);
        final int taskNum = 20;
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(8, taskNum);
        TaskExecutionBuilder builder = new TaskExecutionBuilder()
                .consumer(t -> {
                    try {
                        System.out.println("开始执行ID为" + t.getId() + "的任务");
                        runCount.incrementAndGet();
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return ConsumeResult.FINISHED;
                });

        for (int i = 0; i < taskNum; i++) {
            executor.submit(builder.build());
        }

        executor.shutdown();
        executor.awaitTermination(1L, TimeUnit.DAYS);
        System.out.printf("测试结束: %d个任务", taskNum);

        Assert.isTrue(runCount.get() == taskNum, "任务执行次数有误: " + runCount.get());
    }

    @Test(timeout = 30000L)
    public void testRequeueTask() throws InterruptedException {
        final AtomicInteger runCount = new AtomicInteger(0);
        final AtomicInteger requeueCount = new AtomicInteger(0);
        final int taskNum = 20;
        final Map<String, AtomicInteger> attemptsMap = new HashMap<>(taskNum);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(8, taskNum);
        TaskExecutionBuilder builder = new TaskExecutionBuilder()
                .consumer(t -> {
                    int attempts = attemptsMap.get(t.getId()).incrementAndGet();
                    try {
                        System.out.println("开始执行ID为" + t.getId() + "的任务");
                        runCount.incrementAndGet();
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (attempts < executor.getMaxAttempt()) {
                        requeueCount.incrementAndGet();
                    }
                    return ConsumeResult.REQUEUE;
                });

        for (int i = 0; i < taskNum; i++) {
            TaskExecution taskExecution = builder.build();
            attemptsMap.put(taskExecution.getId(), new AtomicInteger(0));
            executor.submit(taskExecution);
        }

        executor.shutdown();
        executor.awaitTermination(1L, TimeUnit.DAYS);
        System.out.printf("测试结束: %d个任务，重跑了%d次", taskNum, requeueCount.get());

        Assert.isTrue(runCount.get() == taskNum + requeueCount.get(), "任务执行次数有误: " + runCount.get());
    }

    @Test(timeout = 30000L)
    public void testRemoveRunningTaskAndRequeue() throws InterruptedException {
        final AtomicInteger count = new AtomicInteger(0);
        final int taskNum = 50;
        TaskExecutor executor = new ThreadPoolTaskExecutor(8, taskNum);
        TaskExecutionBuilder builder = new TaskExecutionBuilder()
                .consumer(t -> {
                    try {
                        System.out.println("开始执行ID为" + t.getId() + "的任务");
                        count.incrementAndGet();
                        executor.cancelTask(t.getId());
                        Thread.sleep(200L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return ConsumeResult.REQUEUE;
                });

        for (int i = 0; i < taskNum; i++) {
            executor.submit(builder.build());
        }

        executor.shutdown();
        executor.awaitTermination(1L, TimeUnit.DAYS);
        System.out.println("测试结束");

        Assert.isTrue(count.get() == taskNum, "任务执行次数有误: " + count.get());
    }

    @SuppressWarnings("unchecked")
    @Test(timeout = 30000L)
    public void testCheckStop() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        final AtomicInteger count = new AtomicInteger(0);
        final int taskNum = 50;
        TaskExecutor executor = new ThreadPoolTaskExecutor(8, taskNum);

        Field stopTaskIdSetField = executor.getClass().getDeclaredField("stopTaskIdSet");
        stopTaskIdSetField.setAccessible(true);
        Set<String> stopTaskIdSet = (Set<String>) stopTaskIdSetField.get(executor);

        TaskExecutionBuilder builder = new TaskExecutionBuilder()
                .consumer(t -> {
                    try {
                        System.out.println("开始执行ID为" + t.getId() + "的任务");
                        count.incrementAndGet();
                        executor.cancelTask(t.getId());
                        Thread.sleep(200L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return ConsumeResult.REQUEUE;
                });

        for (int i = 0; i < taskNum; i++) {
            TaskExecution taskExecution = builder.build();
            // 预先加入停止任务指令
            stopTaskIdSet.add(taskExecution.getId());
            executor.submit(taskExecution);
        }

        executor.shutdown();
        executor.awaitTermination(1L, TimeUnit.DAYS);
        System.out.println("测试结束");

        Assert.isTrue(count.get() == 0, "任务执行次数有误: " + count.get());
    }

    @Test
    public void testTaskError() throws InterruptedException {
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicInteger requeueCount = new AtomicInteger(0);
        final int taskNum = 50;
        final String errorMessage = "故意抛个异常";
        final Map<String, AtomicInteger> attemptsMap = new HashMap<>(taskNum);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(8, taskNum);

        TaskExecutionBuilder builder = new TaskExecutionBuilder()
                .consumer(t -> {
                    try {
                        attemptsMap.get(t.getId()).incrementAndGet();
                        System.out.println("开始执行ID为" + t.getId() + "的任务");
                        count.incrementAndGet();
                        Thread.sleep(200L);
                        throw new RuntimeException(errorMessage);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return ConsumeResult.FINISHED;
                })
                .errorConsumer((t, e) -> {
                    Assert.isTrue(e instanceof RuntimeException, "异常类型不对");
                    Assert.isTrue(e.getMessage().equals(errorMessage));
                    System.out.println("ID为" + t.getId() + "的任务抛出了预料之内的异常，重新放回队列");
                    if (attemptsMap.get(t.getId()).get() < executor.getMaxAttempt()) {
                        requeueCount.incrementAndGet();
                    }
                    return ConsumeResult.REQUEUE;
                })
                .finallyConsumer(t -> {
                    System.out.println("这里是ID为" + t.getId() + "的任务Finally处理器");
                });

        for (int i = 0; i < taskNum; i++) {
            TaskExecution taskExecution = builder.build();
            attemptsMap.put(taskExecution.getId(), new AtomicInteger(0));
            executor.submit(taskExecution);
        }

        executor.shutdown();
        executor.awaitTermination(1L, TimeUnit.DAYS);
        System.out.printf("测试结束: 任务数=%d, 重跑数=%d\n", taskNum, requeueCount.get());

        Assert.isTrue(count.get() == taskNum + requeueCount.get(), "任务执行次数有误: " + count.get());
    }

    @Test
    public void testDequeueNullCache() throws InterruptedException {
        final AtomicInteger runCount = new AtomicInteger(0);
        final int taskNum = 20;
        final AtomicInteger allocateCount = new AtomicInteger(0);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(8, taskNum);
        TaskExecutionBuilder builder = new TaskExecutionBuilder()
                .resourceAllocation(t -> {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (allocateCount.getAndIncrement() < taskNum) {
                        System.out.printf("ID为%s的任务申请资源失败\n", t.getId());
                        return false;
                    }
                    System.out.printf("ID为%s的任务申请资源成功\n", t.getId());
                    return true;
                })
                .consumer(t -> {
                    try {
                        System.out.println("开始执行ID为" + t.getId() + "的任务");
                        runCount.incrementAndGet();
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return ConsumeResult.FINISHED;
                });

        for (int i = 0; i < taskNum; i++) {
            executor.submit(builder.build());
        }

        executor.shutdown();
        executor.awaitTermination(1L, TimeUnit.DAYS);
        System.out.printf("测试结束: %d个任务", taskNum);

        Assert.isTrue(runCount.get() == taskNum, "任务执行次数有误: " + runCount.get());
        Assert.isTrue(allocateCount.get() == taskNum * 2, "资源申请次数有误: " + allocateCount.get());
    }

    @Test(timeout = 5000L)
    public void testRemoveTaskFromQueue() throws InterruptedException {
        final AtomicInteger runCount = new AtomicInteger(0);
        final AtomicInteger stopCount = new AtomicInteger(0);
        final int taskNum = 20;
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(1, taskNum);
        TaskExecutionBuilder builder = new TaskExecutionBuilder()
                .consumer(t -> {
                    try {
                        System.out.println("开始执行ID为" + t.getId() + "的任务");
                        runCount.incrementAndGet();
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return ConsumeResult.FINISHED;
                });

        for (int i = 0; i < taskNum; i++) {
            TaskExecution taskExecution = builder.build();
            executor.submit(taskExecution);
            if (executor.cancelTask(taskExecution.getId())) {
                stopCount.incrementAndGet();
            }
        }

        executor.shutdown();
        executor.awaitTermination(1L, TimeUnit.DAYS);
        System.out.printf("测试结束: %d个任务", taskNum);

        Assert.isTrue(runCount.get() + stopCount.get() == taskNum,
                "任务执行次数有误: " + runCount.get() + ", 成功停止任务数: " + stopCount.get());
    }

    @Test(timeout = 10000L)
    public void testDefaultErrorConsumer() throws InterruptedException {
        final AtomicInteger runCount = new AtomicInteger(0);
        final int taskNum = 5;
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(1, taskNum);
        TaskExecutionBuilder builder = new TaskExecutionBuilder()
                .consumer(t -> {
                    try {
                        System.out.println("开始执行ID为" + t.getId() + "的任务");
                        runCount.incrementAndGet();
                        Thread.sleep(100L);
                        throw new RuntimeException("故意抛个异常");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return ConsumeResult.FINISHED;
                });

        for (int i = 0; i < taskNum; i++) {
            TaskExecution taskExecution = builder.build();
            executor.submit(taskExecution);
        }

        executor.shutdown();
        executor.awaitTermination(1L, TimeUnit.DAYS);
        System.out.printf("测试结束: %d个任务", taskNum);

        Assert.isTrue(runCount.get() == executor.getMaxAttempt() * taskNum, "任务执行次数有误: " + runCount.get());
    }

    @Test(timeout = 3000L)
    public void testForceShutdown() throws InterruptedException {
        final AtomicInteger runCount = new AtomicInteger(0);
        final int taskNum = 20;
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(8, taskNum);
        TaskExecutionBuilder builder = new TaskExecutionBuilder()
                .consumer(t -> {
                    runCount.incrementAndGet();
                    try {
                        System.out.println("开始执行ID为" + t.getId() + "的任务");
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return ConsumeResult.FINISHED;
                });

        for (int i = 0; i < taskNum; i++) {
            executor.submit(builder.build());
        }

        List<TaskExecution> taskExecutions = executor.forceShutdown();
        System.out.printf("测试结束: %d个任务", taskNum);

        // 已完成的任务数+被剔除的任务数 = 总任务数
        Assert.isTrue(runCount.get() + taskExecutions.size() == taskNum,
                "任务执行次数有误: " + runCount.get() + ", 被抛弃的任务数: " + taskExecutions.size());
    }

    @Test
    public void testParallelSubmitTask() throws InterruptedException {
        final AtomicInteger runCount = new AtomicInteger(0);
        final int taskNum = 20;
        final int submitThreadNum = 4;
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(8, taskNum);
        TaskExecutionBuilder builder = new TaskExecutionBuilder()
                .consumer(t -> {
                    try {
                        System.out.println("开始执行ID为" + t.getId() + "的任务");
                        runCount.incrementAndGet();
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return ConsumeResult.FINISHED;
                });

        Thread[] submitThreads = new Thread[submitThreadNum];
        for (int i = 0; i < submitThreadNum; i++) {
            submitThreads[i] = new Thread(() -> {
                for (int j = 0; j < taskNum / submitThreadNum; j++) {
                    executor.submit(builder.build());
                }
            });
            submitThreads[i].start();
        }

        for (int i = 0; i < submitThreads.length; i++) {
            submitThreads[i].join();
        }

        executor.shutdown();
        executor.awaitTermination(1L, TimeUnit.DAYS);
        System.out.printf("测试结束: %d个任务", taskNum);

        Assert.isTrue(runCount.get() == taskNum, "任务执行次数有误: " + runCount.get());
    }

    static class TaskExecutionBuilder {
        private final AtomicInteger idIncrement = new AtomicInteger(0);
        private final long priorityLimit = 100000000000000L;
        private Function<TaskExecution, Boolean> _resourceAllocation = t -> {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        };

        private Function<TaskExecution, ConsumeResult> _consumer = t -> {
            try {
                System.out.println("开始执行ID为" + t.getId() + "的任务");
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return ConsumeResult.FINISHED;
        };

        private BiFunction<TaskExecution, Throwable, ConsumeResult> _errorConsumer = null;

        private Consumer<TaskExecution> _finallyConsumer = null;

        public TaskExecutionBuilder resourceAllocation(Function<TaskExecution, Boolean> resourceAllocation) {
            this._resourceAllocation = resourceAllocation;
            return this;
        }

        public TaskExecutionBuilder consumer(Function<TaskExecution, ConsumeResult> consumer) {
            this._consumer = consumer;
            return this;
        }

        public TaskExecutionBuilder errorConsumer(BiFunction<TaskExecution, Throwable, ConsumeResult> errorConsumer) {
            this._errorConsumer = errorConsumer;
            return this;
        }

        public TaskExecutionBuilder finallyConsumer(Consumer<TaskExecution> finallyConsumer) {
            this._finallyConsumer = finallyConsumer;
            return this;
        }

        public TaskExecution build() {
            TaskExecution taskExecution = new TaskExecution();
            taskExecution.setId(String.valueOf(idIncrement.incrementAndGet()));
            taskExecution.setPriority(priorityLimit - System.currentTimeMillis());
            taskExecution.setResourceAllocator(_resourceAllocation);
            taskExecution.setConsumer(_consumer);
            taskExecution.setErrorConsumer(_errorConsumer);
            taskExecution.setFinallyConsumer(_finallyConsumer);
            return taskExecution;
        }
    }
}
