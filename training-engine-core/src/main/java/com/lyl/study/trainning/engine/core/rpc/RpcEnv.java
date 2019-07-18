package com.lyl.study.trainning.engine.core.rpc;

import java.util.concurrent.Future;

/**
 * Rpc环境基类
 * <p>
 * 作为RpcEndpointRef的集中管理类，调用不同终端的Rpc方法时，只需要与一个RpcEnv对象对接即可。
 *
 * @author liyilin
 */
public interface RpcEnv {
    /**
     * 注册Rpc调用终端
     *
     * @param name          终端点名称
     * @param remoteAddress 终端点地址
     * @return 终端点引用
     */
    Future<RpcEndpointRef> setupEndpoint(String name, RpcAddress remoteAddress);

    /**
     * 注销单个Rpc终端点引用
     *
     * @param rpcEndpointRef 要注销的Rpc终端点引用
     */
    void stop(RpcEndpointRef rpcEndpointRef);

    /**
     * 关闭全部服务
     */
    void shutdown();

    /**
     * 异步发送消息，不需要理会响应信息
     *
     * @param message 消息内容
     */
    Future<Void> send(Object message);

    /**
     * 异步发送消息，并把响应信息反序列化成指定类型的对象返回
     * <p>
     * 调用超时采用默认值(defaultAskTimeout)
     *
     * @param message       消息内容
     * @param responseClass 期望将响应信息反序列化成的对象类型
     * @param <T>           响应对象类型
     * @return 远程接口调用的结果对象
     */
    <T> Future<T> ask(Object message, Class<T> responseClass);

    /**
     * 异步发送消息，并把响应信息反序列化成指定类型的对象返回
     *
     * @param message       消息内容
     * @param responseClass 期望将响应信息反序列化成的对象类型
     * @param timeout       调用超时(ms)
     * @param <T>           响应对象类型
     * @return 远程接口调用的结果对象
     */
    <T> Future<T> ask(Object message, Class<T> responseClass, long timeout);
}
