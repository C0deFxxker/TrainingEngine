package com.lyl.study.trainning.engine.core.net.netty.handler;

import com.lyl.study.trainning.engine.core.rpc.dispatch.Dispatcher;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyRpcCallContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * NettyTcpClient与NettyTcpServer公共的消息消费Handler
 *
 * @author liyilin
 */
public class DispatchChannelInboundHandler extends ChannelInboundHandlerAdapter {
    private final Dispatcher dispatcher;

    public DispatchChannelInboundHandler(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof NettyRpcCallContext) {
            msg = ((NettyRpcCallContext) msg).getRequestContent();
        }
        dispatcher.dispatch(new NettyRpcCallContext(msg, ctx));
        super.channelRead(ctx, msg);
    }
}
