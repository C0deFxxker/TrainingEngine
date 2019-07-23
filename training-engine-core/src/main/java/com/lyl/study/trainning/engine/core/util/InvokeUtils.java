package com.lyl.study.trainning.engine.core.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

/**
 * 调用工具类
 *
 * @author liyilin
 */
@Slf4j
public final class InvokeUtils {
    /**
     * 新建一个临时线程进行失败重试调用
     *
     * @param callable    调用闭包
     * @param delayMillis 失败重试间隔
     * @param maxAttempts 最大尝试次数
     * @param <V>         闭包返回值泛型
     * @return 若闭包执行成功，则返回闭包的返回值；否则在Future中抛出最后一次闭包调用抛出的异常
     */
    public static <V> Future<V> failRetry(Callable<V> callable, long delayMillis, int maxAttempts) {
        return failRetry(callable, null, delayMillis, maxAttempts);
    }

    /**
     * 新建一个临时线程进行失败重试调用
     *
     * @param callable      调用闭包
     * @param errorConsumer 异常抛出时通过此消费者做一些善后工作
     * @param delayMillis   失败重试间隔
     * @param maxAttempts   最大尝试次数
     * @param <V>           闭包返回值泛型
     * @return 若闭包执行成功，则返回闭包的返回值；否则在Future中抛出最后一次闭包调用抛出的异常
     */
    public static <V> Future<V> failRetry(Callable<V> callable, Consumer<Exception> errorConsumer,
                                          long delayMillis, int maxAttempts) {
        Assert.isTrue(delayMillis > 0, "delayMillis必须大于0");
        Assert.isTrue(maxAttempts > 0, "maxAttempts必须大于0");
        Assert.notNull(callable, "callable不能为空");

        FutureTask<V> future = createFailRetryFutureTask(callable, errorConsumer, delayMillis, maxAttempts);
        new Thread(future).start();
        return future;
    }

    /**
     * 使用 {@code ExecutorService} 进行失败重试调用
     *
     * @param callable        调用闭包
     * @param delayMillis     失败重试间隔
     * @param maxAttempts     最大尝试次数
     * @param executorService 用来执行失败重试调用的调度服务
     * @param <V>             闭包返回值泛型
     * @return 若闭包执行成功，则返回闭包的返回值；否则在Future中抛出最后一次闭包调用抛出的异常
     */
    public static <V> Future<V> failRetry(Callable<V> callable, long delayMillis, int maxAttempts,
                                          ExecutorService executorService) {
        return failRetry(callable, null, delayMillis, maxAttempts, executorService);
    }

    /**
     * 使用 {@code ExecutorService} 进行失败重试调用
     *
     * @param callable        调用闭包
     * @param errorConsumer   异常抛出时通过此消费者做一些善后工作
     * @param delayMillis     失败重试间隔
     * @param maxAttempts     最大尝试次数
     * @param executorService 用来执行失败重试调用的调度服务
     * @param <V>             闭包返回值泛型
     * @return 若闭包执行成功，则返回闭包的返回值；否则在Future中抛出最后一次闭包调用抛出的异常
     */
    public static <V> Future<V> failRetry(Callable<V> callable, Consumer<Exception> errorConsumer,
                                          long delayMillis, int maxAttempts,
                                          ExecutorService executorService) {
        Assert.isTrue(delayMillis > 0, "delayMillis必须大于0");
        Assert.isTrue(maxAttempts > 0, "maxAttempts必须大于0");
        Assert.notNull(callable, "callable不能为空");

        FutureTask<V> future = createFailRetryFutureTask(callable, errorConsumer, delayMillis, maxAttempts);
        executorService.submit(future);
        return future;
    }

    private static <V> FutureTask<V> createFailRetryFutureTask(Callable<V> callable,
                                                               final Consumer<Exception> errorConsumer,
                                                               long delayMillis, int maxAttempts) {
        return new FutureTask<>(() -> {
            Exception lastException = null;
            for (int i = 0; i < maxAttempts; i++) {
                try {
                    return callable.call();
                } catch (Exception e) {
                    if (log.isDebugEnabled()) {
                        log.debug("第{}次失败，异常信息: {}", i + 1, e.toString());
                    }
                    lastException = e;

                    if (errorConsumer != null) {
                        errorConsumer.accept(e);
                    }

                    // 失败后等待一定间隔
                    if (i != maxAttempts - 1) {
                        Thread.sleep(delayMillis);
                    }
                }
            }
            Assert.notNull(lastException);
            throw lastException;
        });
    }

    private InvokeUtils() {
        // Unused
    }
}
