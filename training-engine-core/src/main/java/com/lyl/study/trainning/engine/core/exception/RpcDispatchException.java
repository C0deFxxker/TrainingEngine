package com.lyl.study.trainning.engine.core.exception;

/**
 * Rpc服务分发异常
 *
 * @author liyilin
 */
public class RpcDispatchException extends RpcException {
    public RpcDispatchException() {
    }

    public RpcDispatchException(String message) {
        super(message);
    }

    public RpcDispatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcDispatchException(Throwable cause) {
        super(cause);
    }

    public RpcDispatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
