package com.lyl.study.trainning.engine.core.exception;

/**
 * 未知服务异常
 *
 * @author liyilin
 */
public class UnknownServiceException extends RpcDispatchException {
    public UnknownServiceException() {
    }

    public UnknownServiceException(String message) {
        super(message);
    }

    public UnknownServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownServiceException(Throwable cause) {
        super(cause);
    }

    public UnknownServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
