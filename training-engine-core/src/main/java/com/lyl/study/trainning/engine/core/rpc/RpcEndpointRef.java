package com.lyl.study.trainning.engine.core.rpc;

import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.Future;

/**
 * 远程接口调用的引用基类，主要负责消息发送功能，此派生类的方法都必须是"线程安全的"。
 *
 * @author liyilin
 */
@Data
public abstract class RpcEndpointRef implements Serializable {
    /**
     * 远程接口调用地址
     */
    protected RpcAddress address;
    /**
     * 远程终端点的名称
     */
    protected String name;
    /**
     * 消息失败重发次数上限
     */
    protected int maxRetries;
    /**
     * 消息失败重发的间隔(ms)
     */
    protected long retryWaitMs;
    /**
     * 远程接口响应超时(ms)
     */
    protected long defaultAskTimeout;

    /**
     * 异步发送消息，不需要理会响应信息
     *
     * @param message 消息内容
     */
    public abstract Future<Void> send(RequestMessage message);

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
    public <T> Future<T> ask(RequestMessage message, Class<T> responseClass) {
        return ask(message, responseClass, defaultAskTimeout);
    }

    /**
     * 异步发送消息，并把响应信息反序列化成指定类型的对象返回
     *
     * @param message       消息内容
     * @param responseClass 期望将响应信息反序列化成的对象类型
     * @param timeout       调用超时(ms)
     * @param <T>           响应对象类型
     * @return 远程接口调用的结果对象
     */
    public abstract <T> Future<T> ask(RequestMessage message, Class<T> responseClass, long timeout);
}
