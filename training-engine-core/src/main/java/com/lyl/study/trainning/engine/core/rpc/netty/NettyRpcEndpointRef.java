package com.lyl.study.trainning.engine.core.rpc.netty;

import com.lyl.study.trainning.engine.core.rpc.RequestMessage;
import com.lyl.study.trainning.engine.core.rpc.RpcAddress;
import com.lyl.study.trainning.engine.core.rpc.RpcEndpointRef;
import io.netty.channel.ChannelHandlerContext;

import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Netty实现的Rpc终端点引用
 *
 * @author liyilin
 */
public class NettyRpcEndpointRef extends RpcEndpointRef {
    private ChannelHandlerContext context;

    public NettyRpcEndpointRef(NettyRpcClientEndpoint rpcEndpoint) {
        this(rpcEndpoint.getContext());
        this.name = rpcEndpoint.getName();
    }

    public NettyRpcEndpointRef(ChannelHandlerContext context) {
        this.context = context;
        this.address = new RpcAddress(context.channel().remoteAddress());
        this.defaultAskTimeout = 60000L;
    }

    @Override
    public Future<Void> send(RequestMessage message) {
        setRequestId(message);
        message.setSenderAddress(new RpcAddress(context.channel().remoteAddress()));
        return context.writeAndFlush(message);
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> Future<T> ask(RequestMessage message, Class<T> responseClass, long timeout) {
        setRequestId(message);
        Future<Object> future = NettyAskContext.askForResponse(message.getRequestId(), timeout);
        context.writeAndFlush(message);
        return (Future<T>) future;
    }

    private void setRequestId(RequestMessage message) {
        if (message.getRequestId() == null) {
            message.setRequestId(UUID.randomUUID().toString().replace("-", ""));
        }
    }
}
