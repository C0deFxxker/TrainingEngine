package com.lyl.study.training.engine.core.task;

import com.lyl.study.training.engine.core.util.Assert;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 线程池任务分发器
 *
 * @author liyilin
 */
@Slf4j
public class ThreadPoolTaskExecutor extends ThreadPoolExecutor implements TaskExecutor {
    private final Lock lock = new ReentrantLock();
    private final Set<String> runningTaskIdSet = new HashSet<>();
    private final Set<String> stopTaskIdSet = new HashSet<>();
    /**
     * 最大执行次数
     */
    private final int maxAttempt;
    /**
     * 任务执行次数缓存
     */
    private final Map<String, Integer> attemptsMap = new HashMap<>();
    /**
     * 默认的异常处理方法
     */
    private final BiFunction<TaskExecution, Throwable, ConsumeResult> defaultErrorConsumer = (t, e) -> {
        Integer attempts = attemptsMap.get(t.getId());
        log.error("默认异常处理器: id为" + t.getId() + "的任务运行异常，这是第" + attempts + "次运行", e);
        return ConsumeResult.REQUEUE;
    };

    public ThreadPoolTaskExecutor(int parallelNum, int backlog) {
        this(parallelNum, backlog, 3, 5000L);
    }

    public ThreadPoolTaskExecutor(int parallelNum, int backlog, int maxAttempt) {
        this(parallelNum, backlog, maxAttempt, 5000L);
    }

    public ThreadPoolTaskExecutor(int parallelNum, int backlog, int maxAttempt, long dequeueNullIntervalMillis) {
        super(parallelNum, parallelNum, 0L, TimeUnit.SECONDS,
                new TaskPriorityBlockingQueue(backlog, dequeueNullIntervalMillis));
        this.maxAttempt = maxAttempt;
    }

    public ThreadPoolTaskExecutor(int parallelNum, int backlog, int maxAttempt, long dequeueNullIntervalMillis,
                                  ThreadFactory threadFactory) {
        super(parallelNum, parallelNum, 0L, TimeUnit.SECONDS,
                new TaskPriorityBlockingQueue(backlog, dequeueNullIntervalMillis),
                threadFactory);
        this.maxAttempt = maxAttempt;
    }

    public ThreadPoolTaskExecutor(int parallelNum, int backlog, int maxAttempt, long dequeueNullIntervalMillis,
                                  ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
        super(parallelNum, parallelNum, 0L, TimeUnit.SECONDS,
                new TaskPriorityBlockingQueue(backlog, dequeueNullIntervalMillis),
                threadFactory, rejectedExecutionHandler);
        this.maxAttempt = maxAttempt;
    }

    public int getMaxAttempt() {
        return maxAttempt;
    }

    @Override
    public Future<?> submit(TaskExecution taskExecution) {
        int addNum = prestartAllCoreThreads();
        if (addNum > 0) {
            try {
                // 稍作等待，否则线程还没准备完毕立刻扔任务到队列中第一次dequeue会报InterruptedException
                Thread.sleep(20L);
            } catch (InterruptedException ignored) {
            }

            // 并发提交任务时，这个日志可能会出现多次，但可以放心线程数始终会保持一致
            if (log.isDebugEnabled()) {
                log.debug("初始化线程池线程完毕，线程数={}", getCorePoolSize());
            }
        }

        ThreadPoolTaskExecutorTaskRunnable threadPoolTaskExecutorTaskRunnable = new ThreadPoolTaskExecutorTaskRunnable(taskExecution);
        FutureTask<Void> future = new FutureTask<>(threadPoolTaskExecutorTaskRunnable, null);
        getQueue().offer(threadPoolTaskExecutorTaskRunnable);
        return future;
    }

    @Override
    public List<TaskExecution> forceShutdown() {
        return shutdownNow().stream()
                .filter(e -> e instanceof ThreadPoolTaskExecutorTaskRunnable)
                .map(e -> ((ThreadPoolTaskExecutorTaskRunnable) e).getTaskExecution())
                .collect(Collectors.toList());
    }

    @Override
    public boolean cancelTask(String taskId) {
        lock.lock();
        try {
            if (runningTaskIdSet.contains(taskId)) {
                stopTaskIdSet.add(taskId);
                return true;
            } else {
                return getQueue().removeIf(e -> ((ThreadPoolTaskExecutorTaskRunnable) e).getTaskExecution().getId().equals(taskId));
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 任务Runnable接口封装类
     */
    public class ThreadPoolTaskExecutorTaskRunnable extends TaskRunnable {
        public ThreadPoolTaskExecutorTaskRunnable(TaskExecution taskExecution) {
            super(taskExecution);
        }

        @Override
        public final void run() {
            String taskId = getTaskExecution().getId();
            Integer attempts = attemptsMap.getOrDefault(taskId, 0);
            attemptsMap.put(taskId, attempts + 1);

            if (!checkStop(getTaskExecution())) {
                return;
            }
            ConsumeResult result = ConsumeResult.FINISHED;
            try {
                try {
                    // 跑正常流
                    Assert.notNull(getTaskExecution().getConsumer());
                    result = getTaskExecution().getConsumer().apply(getTaskExecution());
                } catch (Throwable e) {
                    // 遇到异常时执行异常流处理方法
                    if (getTaskExecution().getErrorConsumer() != null) {
                        result = getTaskExecution().getErrorConsumer().apply(getTaskExecution(), e);
                    } else {
                        result = defaultErrorConsumer.apply(getTaskExecution(), e);
                    }
                } finally {
                    // 执行Finally处理方法
                    if (getTaskExecution().getFinallyConsumer() != null) {
                        getTaskExecution().getFinallyConsumer().accept(getTaskExecution());
                    }
                }
            } finally {
                // 尝试次数少于阈值，可以回去重跑
                if (result == ConsumeResult.REQUEUE && attemptsMap.get(taskId) < maxAttempt) {
                    requeueTask(getTaskExecution());
                } else if (result == ConsumeResult.REQUEUE) {
                    log.error("ID为{}的任务因重试次数过多被剔除出队列", taskId);
                    attemptsMap.remove(taskId);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("ID为{}的任务执行完毕", taskId);
                    }
                    attemptsMap.remove(taskId);
                }
            }
        }

        boolean checkStop(TaskExecution taskExecution) {
            String taskId = taskExecution.getId();
            lock.lock();
            try {
                if (stopTaskIdSet.contains(taskId)) {
                    if (log.isDebugEnabled()) {
                        log.debug("ID为{}的任务已被停止，放弃运行该任务", taskId);
                    }
                    stopTaskIdSet.remove(taskId);
                    return false;
                } else {
                    runningTaskIdSet.add(taskId);
                    return true;
                }
            } finally {
                lock.unlock();
            }
        }

        void requeueTask(TaskExecution taskExecution) {
            String taskId = taskExecution.getId();
            lock.lock();
            runningTaskIdSet.remove(taskId);
            try {
                if (!stopTaskIdSet.contains(taskId)) {
                    if (log.isDebugEnabled()) {
                        log.debug("ID={}的任务重新放入队列中", taskId);
                    }
                    getQueue().offer(this);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("ID={}的任务已被剔除，不会重新放入队列中", taskId);
                    }
                    stopTaskIdSet.remove(taskId);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 任务优先队列
     * <p>
     * 修改了出队逻辑，为任务需要申请到资源才能出队
     */
    static class TaskPriorityBlockingQueue extends PriorityBlockingQueue<Runnable> {
        private final long dequeueNullIntervalMillis;

        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notEmpty = lock.newCondition();
        private final AtomicLong lastDequeueNullTimestamp = new AtomicLong(0);

        public TaskPriorityBlockingQueue(int initialCapacity, long dequeueNullIntervalMillis) {
            super(initialCapacity);
            this.dequeueNullIntervalMillis = dequeueNullIntervalMillis;
        }

        /**
         * 重写出队逻辑，只有在任务资源申请成功才允许出队
         */
        protected Runnable dequeue() {
            ThreadPoolTaskExecutorTaskRunnable goal = null;
            long now = System.currentTimeMillis();
            if (now >= lastDequeueNullTimestamp.get() + dequeueNullIntervalMillis) {
                for (Runnable runnable : this) {
                    ThreadPoolTaskExecutorTaskRunnable threadPoolTaskExecutorTaskRunnable = (ThreadPoolTaskExecutorTaskRunnable) runnable;
                    TaskExecution taskExecution = threadPoolTaskExecutorTaskRunnable.getTaskExecution();
                    Function<TaskExecution, Boolean> resourceAllocator = taskExecution.getResourceAllocator();
                    // 只有在资源申请到的时候才允许出队
                    if (resourceAllocator.apply(taskExecution)) {
                        goal = threadPoolTaskExecutorTaskRunnable;
                        remove(goal);
                        break;
                    }
                }
                // 只有在本队列不为空，但又没有任务可以申请到资源时稍作缓存，避免资源申请操作太频繁
                if (!this.isEmpty() && goal == null) {
                    lastDequeueNullTimestamp.set(System.currentTimeMillis());
                }
            }
            return goal;
        }

        @Override
        public Runnable poll() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return dequeue();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Runnable take() throws InterruptedException {
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            Runnable result;
            try {
                while ((result = dequeue()) == null && isEmpty())
                    notEmpty.await();
            } finally {
                lock.unlock();
            }
            return result;
        }

        @Override
        public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
            long nanos = unit.toNanos(timeout);
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            Runnable result;
            try {
                while ((result = dequeue()) == null && nanos > 0 && isEmpty())
                    nanos = notEmpty.awaitNanos(nanos);
            } finally {
                lock.unlock();
            }
            return result;
        }

        @Override
        public Runnable peek() {
            throw new UnsupportedOperationException();
        }
    }
}
