package com.lyl.study.trainning.engine.core.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 统一命名前缀的线程工厂
 *
 * @author liyilin
 */
public class NamedDaemonThreadFactory implements ThreadFactory {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private final String prefix;
    private final boolean daemon;
    private final ClassLoader contextClassLoader;
    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    /**
     * Creates a new thread factory that will name its threads &lt;prefix&gt;-&lt;n&gt;, where
     * &lt;prefix&gt; is the given {@code prefix} and &lt;n&gt; is the count of threads
     * created thus far by this class.
     *
     * @param prefix The thread name prefix
     */
    public NamedDaemonThreadFactory(String prefix) {
        this(prefix, new ClassLoader(Thread.currentThread().getContextClassLoader()) {
        });
    }

    /**
     * Creates a new thread factory that will name its threads &lt;prefix&gt;-&lt;n&gt;, where
     * &lt;prefix&gt; is the given {@code prefix} and &lt;n&gt; is the count of threads
     * created thus far by this class. If the contextClassLoader parameter is not null it will assign it to the forged
     * Thread
     *
     * @param prefix             The thread name prefix
     * @param contextClassLoader An optional classLoader to assign to thread
     */
    public NamedDaemonThreadFactory(String prefix, ClassLoader contextClassLoader) {
        this(prefix, contextClassLoader, null, true);
    }

    public NamedDaemonThreadFactory(String prefix,
                                    ClassLoader contextClassLoader,
                                    Thread.UncaughtExceptionHandler uncaughtExceptionHandler,
                                    boolean daemon
    ) {
        this.prefix = prefix;
        this.daemon = daemon;
        this.contextClassLoader = contextClassLoader;
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setName(prefix + "-" + COUNTER.incrementAndGet());
        t.setDaemon(daemon);
        if (contextClassLoader != null) {
            t.setContextClassLoader(contextClassLoader);
        }
        if (null != uncaughtExceptionHandler) {
            t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        }
        return t;
    }
}
