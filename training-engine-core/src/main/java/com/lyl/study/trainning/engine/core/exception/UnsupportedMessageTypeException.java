package com.lyl.study.trainning.engine.core.exception;

import com.lyl.study.trainning.engine.core.rpc.dispatch.Consumer;
import com.lyl.study.trainning.engine.core.rpc.dispatch.Dispatcher;

/**
 * 消息类型不支持异常。
 * <p>
 * 当 {@link Dispatcher} 处理消息时，发现没有合适的 {@link Consumer} 消费此消息时，就会抛出此异常。
 *
 * @author liyilin
 */
public class UnsupportedMessageTypeException extends RuntimeException {
    public UnsupportedMessageTypeException() {
    }

    public UnsupportedMessageTypeException(String message) {
        super(message);
    }

    public UnsupportedMessageTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedMessageTypeException(Throwable cause) {
        super(cause);
    }

    public UnsupportedMessageTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
