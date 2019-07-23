package com.lyl.study.trainning.engine.core.rpc.dispatch;

/**
 * 消息分发消费者接口
 *
 * @author liyilin
 */
public abstract class Consumer implements Comparable<Consumer> {
    /**
     * 判断消息能否被处理
     *
     * @param content 消息内容
     * @return 若消息能被处理则返回 {@code true}，否则返回 {@code false}
     */
    public abstract boolean acceptable(Object content);

    /**
     * 消费消息
     *
     * @param content 消息内容
     */
    public abstract void accept(Object content);

    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int compareTo(Consumer o) {
        return getOrder() - o.getOrder();
    }
}
