package com.lyl.study.trainning.engine.core.rpc.dispatch;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 带有生命周期的消息分发器
 *
 * @author liyilin
 */
public abstract class AbstractLifeCycleDispatcher implements Dispatcher {
    private final AtomicBoolean alive = new AtomicBoolean(true);
    private final ClassLoader context = new ClassLoader(Thread.currentThread()
            .getContextClassLoader()) {
    };

    protected final List<Consumer> consumers;

    public AbstractLifeCycleDispatcher(List<Consumer> consumers) {
        if (consumers == null) {
            consumers = Collections.emptyList();
        }
        this.consumers = consumers;
    }

    @Override
    public boolean isAlive() {
        return alive.get();
    }

    @Override
    public void shutdown() {
        alive.compareAndSet(true, false);
    }

    @Override
    public void forceShutdown() {
        alive.compareAndSet(true, false);
    }

    @Override
    public boolean awaitAndShutdown() throws InterruptedException {
        return awaitAndShutdown(Integer.MAX_VALUE, TimeUnit.SECONDS);
    }

    @Override
    public final void dispatch(Object data) throws IllegalStateException {
        if (!isAlive()) {
            throw new IllegalStateException("This Dispatcher has been shut down.");
        }

        doDispatch(data);
    }

    protected abstract void doDispatch(Object data);

    protected final ClassLoader getContext() {
        return context;
    }

    @Override
    public boolean inContext() {
        return context == Thread.currentThread().getContextClassLoader();
    }
}
