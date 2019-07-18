package com.lyl.study.trainning.engine.core.rpc.netty;

import com.lyl.study.trainning.engine.core.rpc.RpcEndpoint;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

/**
 * Netty实现的Rpc终端点基类
 * <p>
 * 实现Netty客户端/服务器都要用到的公共能力
 *
 * @author liyilin
 */
public abstract class NettyRpcEndpoint extends RpcEndpoint {
    /**
     * Netty的消息发送、终端等操作离不开这个属性
     */
    protected ChannelHandlerContext context;

    public ChannelHandlerContext getContext() {
        return context;
    }

    protected void setupChannelHandler(ChannelPipeline channelPipeline) {
    }
}
