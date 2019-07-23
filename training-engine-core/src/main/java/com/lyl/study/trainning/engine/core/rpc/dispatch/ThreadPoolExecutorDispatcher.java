package com.lyl.study.trainning.engine.core.rpc.dispatch;

import com.lyl.study.trainning.engine.core.exception.UnsupportedMessageTypeException;
import com.lyl.study.trainning.engine.core.thread.NamedDaemonThreadFactory;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 基于线程池的异步消息分发器
 *
 * @author liyilin
 */
public class ThreadPoolExecutorDispatcher extends AbstractLifeCycleDispatcher {
    @Getter
    private final int backlog;
    private final ThreadPoolExecutor threadPoolExecutor;

    public ThreadPoolExecutorDispatcher(int poolSize, int backlog,
                                        List<Consumer> consumers) {
        super(consumers);
        this.backlog = backlog;
        threadPoolExecutor = new ThreadPoolExecutor(poolSize, poolSize,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(backlog),
                new NamedDaemonThreadFactory(ThreadPoolExecutorDispatcher.class.getSimpleName(), getContext()));
    }

    public ThreadPoolExecutorDispatcher(int poolSize, int backlog,
                                        List<Consumer> consumers,
                                        RejectedExecutionHandler rejectedExecutionHandler) {
        super(consumers);
        this.backlog = backlog;
        threadPoolExecutor = new ThreadPoolExecutor(poolSize, poolSize,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(backlog),
                new NamedDaemonThreadFactory(ThreadPoolExecutorDispatcher.class.getSimpleName(), getContext()),
                rejectedExecutionHandler);
    }

    @Override
    protected void doDispatch(Object data) {
        for (Consumer consumer : consumers) {
            if (consumer.acceptable(data)) {
                threadPoolExecutor.execute(() -> consumer.accept(data));
                return;
            }
        }
        throw new UnsupportedMessageTypeException("找不到合适的消息消费者处理这条消息");
    }

    @Override
    public boolean awaitAndShutdown(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return threadPoolExecutor.awaitTermination(timeout, timeUnit);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        threadPoolExecutor.shutdown();
    }

    @Override
    public void forceShutdown() {
        super.forceShutdown();
        threadPoolExecutor.shutdownNow();
    }
}
