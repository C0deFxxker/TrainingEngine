package com.lyl.study.trainning.engine.core.rpc.dispatch;

/**
 * 消息分发消费者接口
 *
 * @author liyilin
 */
public abstract class DispatchConsumer implements Comparable<DispatchConsumer> {
    /**
     * 判断消息能否被处理
     *
     * @param content 消息内容
     * @return 若消息能被处理则返回 {@code true}，否则返回 {@code false}
     */
    public abstract boolean consumable(Object content);

    /**
     * 消费消息
     *
     * @param content 消息内容
     */
    public abstract void consume(Object content);

    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int compareTo(DispatchConsumer o) {
        return getOrder() - o.getOrder();
    }
}
