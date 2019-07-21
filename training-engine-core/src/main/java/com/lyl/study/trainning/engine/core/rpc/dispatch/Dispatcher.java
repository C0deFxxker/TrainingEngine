package com.lyl.study.trainning.engine.core.rpc.dispatch;

import com.lyl.study.trainning.engine.core.exception.InsufficientCapacityException;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 分发器接口
 *
 * @author liyilin
 */
public interface Dispatcher extends Executor {

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
    boolean awaitAndShutdown();

    /**
     * 带超时的阻塞等待分发器关闭。
     */
    boolean awaitAndShutdown(long timeout, TimeUnit timeUnit);

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
     * 尝试立刻分发消息，若这条新来的消息需要排队等待，没法立刻处理时，
     * 此方法会抛出{@code InsufficientCapacityException}异常；否则，将正常处理本次投放的消息。
     *
     * @param data 消息内容
     * @throws IllegalStateException         如果{@code Dispatcher}不是{@link Dispatcher#isAlive() 生效}状态
     * @throws InsufficientCapacityException 当前消息需要排队等待，没法立刻处理时抛出此异常
     */
    void tryDispatch(Object data) throws IllegalStateException, InsufficientCapacityException;

    /**
     * 还未处理的消息数目
     */
    long backlogSize();

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
    void registryConsumer(String name, DispatchConsumer consumer);
}
