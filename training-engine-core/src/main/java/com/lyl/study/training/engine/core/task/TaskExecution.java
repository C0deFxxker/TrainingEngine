package com.lyl.study.training.engine.core.task;

import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 任务队列元素
 *
 * @author liyilin
 */
@Data
@ToString
public class TaskExecution {
    /**
     * 任务ID
     */
    private String id;
    /**
     * 优先级
     */
    private long priority;
    /**
     * 创建时间
     */
    private Date createTime = new Date();
    /**
     * 扩展额外参数
     */
    private Object extra;
    /**
     * 申请资源函数，任务队列中需要用到。资源的释放需要开发者在下面的正常流或异常流处理函数中完成。
     * <p>
     * 函数返回值表示是否申请资源成功。
     */
    private Function<TaskExecution, Boolean> resourceAllocator;
    /**
     * 任务正常流处理函数
     * <p>
     * 函数返回值表示任务执行的结果，如果是REQUEUE则会放回队列中重跑
     */
    private Function<TaskExecution, ConsumeResult> consumer;
    /**
     * 任务异常流处理函数（可选），当正常流处理函数抛出异常时，会被这个函数捕获
     * <p>
     * 函数返回值表示任务执行的结果，如果是REQUEUE则会放回队列中重跑
     */
    private BiFunction<TaskExecution, Throwable, ConsumeResult> errorConsumer;
    /**
     * 善后工作消费者（可选）
     */
    private Consumer<TaskExecution> finallyConsumer;
}
