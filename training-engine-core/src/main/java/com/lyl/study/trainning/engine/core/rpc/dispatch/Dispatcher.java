package com.lyl.study.trainning.engine.core.rpc.dispatch;

import java.util.concurrent.TimeUnit;

/**
 * 消息分发器接口
 *
 * @author liyilin
 */
public interface Dispatcher {

    /**
     * 判断分发器是否可用
     *
     * @return 如果分发器可以分发消息则返回{@literal true}；否则，返回{@literal false}
     */
    boolean isAlive();

    /**
     * 异步关闭正在生效的分发器，分发器将不再接受新消息。
     * 如果分发器依然持有消息尚未消费完毕，则会继续消费，直到所有消息消费完毕为止才算是完全关闭。
     */
    void shutdown();

    /**
     * 阻塞当前线程直到分发器完全关闭为止。
     */
    boolean awaitAndShutdown() throws InterruptedException;

    /**
     * 带超时的阻塞等待分发器关闭。
     */
    boolean awaitAndShutdown(long timeout, TimeUnit timeUnit) throws InterruptedException;

    /**
     * 强制关闭分发器，强制关闭正在处理的消息并抛弃还未处理的消息。
     */
    void forceShutdown();

    /**
     * 将消息放到分发器队列中
     *
     * @param data 消息内容
     * @throws IllegalStateException 如果{@code Dispatcher}不是{@link Dispatcher#isAlive() 生效}状态
     */
    void dispatch(Object data) throws IllegalStateException;

    /**
     * 消息队列容量
     */
    int getBacklog();

    /**
     * 分发器会记录运行时上下文绑定的线程，通过这个方法调用者可以知道自己是否处于分发器上下文之中
     */
    boolean inContext();

    /**
     * 注册消费者
     *
     * @param name     消费者名称（重复注册同名消费者时，会覆盖原来的同名消费者）
     * @param consumer 消费者
     */
//    void registryConsumer(String name, Consumer consumer);
}
