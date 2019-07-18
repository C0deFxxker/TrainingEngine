package com.lyl.study.trainning.engine.core.rpc.netty;


import com.lyl.study.trainning.engine.core.rpc.RequestMessage;
import com.lyl.study.trainning.engine.core.rpc.RpcAddress;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 通过这个Handler把ChannelHandlerContext保留到NettyRpcEndpoint中
 */
public class NettyRpcRequestResolveInboundHandler extends SimpleChannelInboundHandler<RequestMessage> {
    final NettyRpcEndpoint nettyRpcEndpoint;

    public NettyRpcRequestResolveInboundHandler(NettyRpcEndpoint nettyRpcEndpoint) {
        this.nettyRpcEndpoint = nettyRpcEndpoint;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        nettyRpcEndpoint.context = ctx;
        nettyRpcEndpoint.onConnected(getRemoteAddress(ctx));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        nettyRpcEndpoint.onDisconnected(getRemoteAddress(ctx));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestMessage msg) throws Exception {
        // 响应之前发出的请求
        if (msg.getRequestId() != null
                && msg.getContent().getClass().isAssignableFrom(NettyResponse.class)) {
            NettyResponse nettyResponse = (NettyResponse) msg.getContent();
            nettyResponse.setRequestId(msg.getRequestId());
            NettyAskContext.responseForAsk(nettyResponse);
        } else {
            // TODO 外面主动发起请求的暂时没法得知终端点引用的名称
            NettyRpcEndpointRef rpcEndpointRef = new NettyRpcEndpointRef(ctx);
            rpcEndpointRef.setName("<unknown>");
            NettyRpcCallContext rpcCallContext = new NettyRpcCallContext(rpcEndpointRef, msg);
            nettyRpcEndpoint.receive(rpcCallContext);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        nettyRpcEndpoint.onError(cause);
    }

    private RpcAddress getRemoteAddress(ChannelHandlerContext ctx) {
        String remoteAddress = ctx.channel().remoteAddress().toString();
        int idx = remoteAddress.lastIndexOf(":");
        String host, port = "80";
        if (idx != -1) {
            host = remoteAddress.substring(0, idx);
            port = remoteAddress.substring(idx + 1);
        } else {
            host = remoteAddress;
        }
        return new RpcAddress(host, Integer.parseInt(port));
    }
}
