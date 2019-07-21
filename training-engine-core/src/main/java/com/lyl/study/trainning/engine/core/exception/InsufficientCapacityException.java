package com.lyl.study.trainning.engine.core.exception;

/**
 * 容量不足异常
 *
 * @author liyilin
 */
public class InsufficientCapacityException extends RuntimeException {
    private InsufficientCapacityException() {
        super("The subscriber is overrun by more signals than expected (bounded queue...)");
    }
}
