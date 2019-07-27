package com.lyl.study.training.engine.core.task;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 任务分发器接口
 *
 * @author liyilin
 */
public interface TaskExecutor {
    /**
     * 软关闭，如果队列中还有任务，则会等任务全部执行完毕才真正关闭
     */
    void shutdown();

    /**
     * 强制关闭分发线程，不管队列中是否还有任务
     *
     * @return 返回还没跑的任务
     */
    List<TaskExecution> forceShutdown();

    /**
     * 判断分发器是否接收了关闭指令
     */
    boolean isShutdown();

    /**
     * 判断分发器是否关闭完毕
     */
    boolean isTerminated();

    /**
     * 同步等待分发器关闭
     *
     * @param timeout 等待超时
     * @param unit    时间单位
     * @return 在等待超时的时间内分发器关闭完毕则会返回 {@code true}，否则返回 {@code false}
     * @throws InterruptedException 等待期间线程被中断则会抛出此异常
     */
    boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException;

    /**
     * 向分发器提交任务
     *
     * @param taskExecution 任务信息
     */
    Future<?> submit(TaskExecution taskExecution);

    /**
     * 从队列中剔除任务
     *
     * @param taskId 任务ID
     * @return 是否剔除成功
     */
    boolean cancelTask(String taskId);
}
