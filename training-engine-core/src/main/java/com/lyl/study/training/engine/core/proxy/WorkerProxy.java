package com.lyl.study.training.engine.core.proxy;

import com.lyl.study.training.engine.core.bean.Result;
import reactor.core.publisher.Mono;

/**
 * @author liyilin
 */
public interface WorkerProxy {
    Mono<Result<?>> submitTask(long id);
}
