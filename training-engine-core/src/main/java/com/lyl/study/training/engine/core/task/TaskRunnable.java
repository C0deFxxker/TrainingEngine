package com.lyl.study.training.engine.core.task;

/**
 * TaskExecution的Runnable封装类
 *
 * @author liyilin
 */
public abstract class TaskRunnable implements Runnable, Comparable<TaskRunnable> {
    private final TaskExecution taskExecution;

    public TaskRunnable(TaskExecution taskExecution) {
        this.taskExecution = taskExecution;
    }

    public long getPriority() {
        return taskExecution.getPriority();
    }

    public TaskExecution getTaskExecution() {
        return taskExecution;
    }

    @Override
    public int compareTo(TaskRunnable o) {
        return (int) (getPriority() - o.getPriority());
    }
}
