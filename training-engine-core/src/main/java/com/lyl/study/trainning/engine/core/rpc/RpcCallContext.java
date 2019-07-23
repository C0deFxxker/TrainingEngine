package com.lyl.study.trainning.engine.core.rpc;

import java.net.SocketAddress;

/**
 * Rpc调用接受方的上下文
 *
 * @author liyilin
 */
public interface RpcCallContext {
    /**
     * @return 发送方地址
     */
    SocketAddress getSenderAddress();

    /**
     * @return Rpc调用信息
     */
    Object getRequestContent();

    /**
     * 响应一个对象
     *
     * @param response 响应对象
     */
    void reply(Object response);
}
