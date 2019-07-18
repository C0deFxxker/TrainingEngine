package com.lyl.study.trainning.engine.core.rpc;

/**
 * Rpc接收方
 *
 * @author liyilin
 */
public abstract class RpcEndpoint {
    /**
     * @return 自己的引用对象
     */
    public abstract RpcEndpointRef getSelfRef();

    /**
     * 处理接收到的Rpc调用
     *
     * @param context 调用上下文
     */
    public abstract void receive(RpcCallContext context);

    /**
     * Rpc调用异常事件处理
     *
     * @param cause 异常对象
     */
    public abstract void onError(Throwable cause);

    /**
     * Rpc调用建立好连接事件处理
     *
     * @param remoteAddress 远端地址
     */
    public abstract void onConnected(RpcAddress remoteAddress);

    /**
     * Rpc调用连接失效事件处理
     *
     * @param remoteAddress 远端地址
     */
    public abstract void onDisconnected(RpcAddress remoteAddress);

    /**
     * 终端点开启事件
     */
    public abstract void onStart();

    /**
     * 终端点关闭事件
     */
    public abstract void onStop();

    /**
     * 异步开启终端点
     */
    public abstract void start();

    /**
     * 异步关闭终端点
     */
    public abstract void stop();

    /**
     * 等待终端初始化完毕
     */
    public abstract void awaitStart() throws InterruptedException;

    /**
     * 终端点是否处于生效状态
     */
    public abstract boolean isActive();

    /**
     * 终端点是否处于开启状态，初始化完毕后将变为生效状态
     */
    public abstract boolean isOpen();
}
