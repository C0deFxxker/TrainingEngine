package com.lyl.study.trainning.engine.core.exception;

/**
 * 消费异常
 *
 * @author liyilin
 */
public class ConsumeException extends RpcException {
    public ConsumeException() {
    }

    public ConsumeException(String message) {
        super(message);
    }

    public ConsumeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsumeException(Throwable cause) {
        super(cause);
    }

    public ConsumeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
