package com.lyl.study.trainning.engine.core.test.mock;

import com.lyl.study.trainning.engine.core.rpc.RpcCallContext;
import com.lyl.study.trainning.engine.core.util.Assert;
import lombok.extern.slf4j.Slf4j;

/**
 * 这个Consumer依赖于Netty服务器实现，收到任何消息后都会回复"hello"字符串
 *
 * @author liyilin
 */
@Slf4j
public class NettyReplyHelloConsumer extends EmptyConsumer {
    private Object replyObject = new Result<>(0, "success", "hello");

    public NettyReplyHelloConsumer(boolean _consumable, long sleepMillis) {
        super(_consumable, sleepMillis);
    }

    public Object getReplyObject() {
        return replyObject;
    }

    public NettyReplyHelloConsumer setReplyObject(Object replyObject) {
        this.replyObject = replyObject;
        return this;
    }

    @Override
    public void accept(Object content) {
        log.info("接收到消息: {}", content);
        lastReceiveContent = content;

        Assert.isTrue(content instanceof RpcCallContext, "content类型不对，没法响应: " + content.getClass());
        ((RpcCallContext) content).reply(replyObject);
    }
}
