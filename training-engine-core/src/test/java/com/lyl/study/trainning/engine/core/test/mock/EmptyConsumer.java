package com.lyl.study.trainning.engine.core.test.mock;

import com.lyl.study.trainning.engine.core.rpc.dispatch.Consumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 空执行的Consumer
 *
 * @author liyilin
 */
@Slf4j
public class EmptyConsumer extends Consumer {
    protected final boolean _consumable;
    protected final long sleepMillis;
    @Getter
    protected Object lastReceiveContent = null;

    public EmptyConsumer(boolean _consumable, long sleepMillis) {
        this._consumable = _consumable;
        this.sleepMillis = sleepMillis;
    }

    @Override
    public boolean acceptable(Object content) {
        return _consumable;
    }

    @Override
    public void accept(Object content) {
        lastReceiveContent = content;
        log.info("开始执行任务: {}", content);
        try {
            Thread.sleep(sleepMillis);
            log.info("任务执行完毕: {}", content);
        } catch (InterruptedException e) {
            log.info("任务被中断: {}", content);
        }
    }
}
