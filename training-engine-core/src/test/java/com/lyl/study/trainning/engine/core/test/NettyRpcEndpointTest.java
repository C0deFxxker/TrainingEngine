package com.lyl.study.trainning.engine.core.test;

import com.lyl.study.trainning.engine.core.rpc.RequestMessage;
import com.lyl.study.trainning.engine.core.rpc.RpcAddress;
import com.lyl.study.trainning.engine.core.rpc.RpcCallContext;
import com.lyl.study.trainning.engine.core.rpc.RpcEndpointRef;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyRequestCodecHandler;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyResponse;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyRpcServerEndpoint;
import com.lyl.study.trainning.engine.core.rpc.serialize.ObjectMapperCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liyilin
 */
public class NettyRpcEndpointTest {

    @Test
    public void test1() throws InterruptedException {
        TestNettyRpcServerEndpoint rpcServerEndpoint = new TestNettyRpcServerEndpoint("test-server", 8080);
        rpcServerEndpoint.start();

        while (rpcServerEndpoint.isStarted()) {
            Thread.sleep(200L);
        }
    }

    private static class TestNettyRpcServerEndpoint extends NettyRpcServerEndpoint {
        public boolean isStarted() {
            return started.get();
        }

        @Override
        public RpcEndpointRef getSelfRef() {
            return null;
        }

        public TestNettyRpcServerEndpoint(String name, int port) {
            super(name, port);
        }

        @Override
        protected void setupChannelHandler(ChannelPipeline channelPipeline) {
            channelPipeline.addLast(new NettyRequestCodecHandler(new ObjectMapperCodec<>(RequestMessage.class)));
        }

        @Override
        public void receive(RpcCallContext context) {
            Object requestMessage = context.getRequestContent();
            System.out.println("接收到[" + context.getSenderAddress() + "]的信息内容: " + requestMessage);
            Map<String, Object> map = new HashMap<>(2);
            map.put("code", 0);
            map.put("msg", "success");
            context.reply(map);
        }

        @Override
        public void onError(Throwable cause) {
            System.err.println("发生了异常: " + cause.toString());
            try {
                getContext().close().sync();
                System.out.println("断开连接: " + getContext().channel().remoteAddress().toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConnected(RpcAddress remoteAddress) {
            System.out.println("建立连接: " + remoteAddress);
        }

        @Override
        public void onDisconnected(RpcAddress remoteAddress) {
            System.out.println("连接断开: " + remoteAddress);
        }

        @Override
        public void onStart() {
            System.out.println(name + "初始化完毕, 端口号为" + port);
        }

        @Override
        public void onStop() {
            System.out.println(name + "销毁完毕, 端口号为" + port);
        }
    }

    private static class NettyResponseDecoder extends SimpleChannelInboundHandler {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof ByteBuf) {
                ByteBuf byteBuf = ((ByteBuf) msg);
                byte[] bytes = new byte[((ByteBuf) msg).readableBytes()];
                byteBuf.readBytes(bytes);
                String message = new String(bytes, Charset.defaultCharset());
                NettyResponse nettyResponse = new NettyResponse();
                nettyResponse.setSuccess(true);
                nettyResponse.setContent(message);
                ctx.fireChannelRead(nettyResponse);
            } else {
                throw new IllegalArgumentException("不能处理该类信息: " + msg.getClass());
            }
        }
    }
}
