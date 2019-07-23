package com.lyl.study.trainning.engine.core.rpc.dispatch;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 同步消息分发器
 *
 * @author liyilin
 */
public class SynchronousDispatcher implements Dispatcher {
    private final List<Consumer> consumers;

    public SynchronousDispatcher(List<Consumer> consumers) {
        this.consumers = consumers;
    }

    @Override
    public boolean awaitAndShutdown() {
        return true;
    }

    @Override
    public boolean awaitAndShutdown(long timeout, TimeUnit timeUnit) {
        return true;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void forceShutdown() {
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void dispatch(Object data) throws IllegalStateException {
        for (Consumer consumer : consumers) {
            if (consumer.acceptable(data)) {
                consumer.accept(data);
                return;
            }
        }
    }

    @Override
    public String toString() {
        return "immediate";
    }

    @Override
    public int getBacklog() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean inContext() {
        return true;
    }
}
