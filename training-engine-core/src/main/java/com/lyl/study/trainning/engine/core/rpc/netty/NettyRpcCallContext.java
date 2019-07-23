package com.lyl.study.trainning.engine.core.rpc.netty;

import com.lyl.study.trainning.engine.core.rpc.RpcCallContext;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.net.SocketAddress;

/**
 * Netty实现的Rpc调用上下文
 *
 * @author liyilin
 */
@Data
@ToString
@NoArgsConstructor
public class NettyRpcCallContext implements RpcCallContext {
    private Object requestContent;
    private transient SocketAddress socketAddress;
    private transient ChannelHandlerContext context;

    public NettyRpcCallContext(Object requestContent) {
        this.requestContent = requestContent;
    }

    public NettyRpcCallContext(Object requestContent, ChannelHandlerContext context) {
        this.requestContent = requestContent;
        this.context = context;
        this.socketAddress = context.channel().remoteAddress();
    }

    @Override
    public SocketAddress getSenderAddress() {
        return socketAddress;
    }

    @Override
    public Object getRequestContent() {
        return requestContent;
    }

    @Override
    public void reply(Object response) {
        NettyRpcCallContext rpcCallContext = new NettyRpcCallContext();
        rpcCallContext.setRequestContent(response);
        context.writeAndFlush(rpcCallContext);
    }
}
