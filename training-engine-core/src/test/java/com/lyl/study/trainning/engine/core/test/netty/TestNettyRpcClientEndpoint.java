package com.lyl.study.trainning.engine.core.test.netty;

import com.lyl.study.trainning.engine.core.rpc.RequestMessage;
import com.lyl.study.trainning.engine.core.rpc.RpcAddress;
import com.lyl.study.trainning.engine.core.rpc.RpcCallContext;
import com.lyl.study.trainning.engine.core.rpc.RpcEndpointRef;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyRequestCodecHandler;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyRpcClientEndpoint;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyRpcEndpointRef;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyRpcRequestResolveInboundHandler;
import com.lyl.study.trainning.engine.core.rpc.serialize.ObjectMapperCodec;
import io.netty.channel.ChannelPipeline;
import lombok.extern.slf4j.Slf4j;

/**
 * 测试用的NettyRpcClientEndpoint
 *
 * @author liyilin
 */
@Slf4j
public class TestNettyRpcClientEndpoint extends NettyRpcClientEndpoint {
    public TestNettyRpcClientEndpoint(String name, RpcAddress rpcAddress) {
        super(name, rpcAddress);
    }

    @Override
    public RpcEndpointRef getSelfRef() {
        if (this.context != null) {
            return new NettyRpcEndpointRef(this);
        } else {
            return null;
        }
    }

    @Override
    protected void setupChannelHandler(ChannelPipeline channelPipeline) {
        channelPipeline.addLast(new NettyRequestCodecHandler(new ObjectMapperCodec<>(RequestMessage.class)));
        channelPipeline.addLast(new NettyRpcRequestResolveInboundHandler(this));
    }

    @Override
    public void receive(RpcCallContext context) {
        Object requestMessage = context.getRequestContent();
        log.info("接收到[" + context.getSenderAddress() + "]的信息内容: " + requestMessage);
        this.stop();
    }

    @Override
    public void onError(Throwable cause) {
        log.error("发生了异常", cause);
        try {
            getContext().close().sync();
            log.info("断开连接: " + getContext().channel().remoteAddress().toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(RpcAddress remoteAddress) {
        log.info("建立连接: " + remoteAddress);
    }

    @Override
    public void onDisconnected(RpcAddress remoteAddress) {
        log.info("连接断开: " + remoteAddress);
    }

    @Override
    public void onStart() {
        log.info(name + "初始化完毕: " + rpcAddress);
    }

    @Override
    public void onStop() {
        log.info(name + "销毁完毕: " + rpcAddress);
    }
}
