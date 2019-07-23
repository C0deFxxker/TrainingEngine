package com.lyl.study.trainning.engine.core.test.net;

import com.lyl.study.trainning.engine.core.net.netty.NettyClientSocketOptions;
import com.lyl.study.trainning.engine.core.net.netty.NettyServerSocketOptions;
import com.lyl.study.trainning.engine.core.net.netty.NettyTcpClient;
import com.lyl.study.trainning.engine.core.net.netty.NettyTcpServer;
import com.lyl.study.trainning.engine.core.rpc.dispatch.Consumer;
import com.lyl.study.trainning.engine.core.rpc.dispatch.Dispatcher;
import com.lyl.study.trainning.engine.core.rpc.dispatch.ThreadPoolExecutorDispatcher;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyRpcCallContext;
import com.lyl.study.trainning.engine.core.rpc.serialize.Codec;
import com.lyl.study.trainning.engine.core.rpc.serialize.EncodeException;
import com.lyl.study.trainning.engine.core.rpc.serialize.TypicalObjectMapperCodec;
import com.lyl.study.trainning.engine.core.test.mock.EmptyConsumer;
import com.lyl.study.trainning.engine.core.test.mock.NettyReplyHelloConsumer;
import com.lyl.study.trainning.engine.core.test.mock.RequestWrapper;
import com.lyl.study.trainning.engine.core.util.Assert;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

/**
 * NettyTcpServer单元测试类
 *
 * @author liyilin
 */
@Slf4j
public class NettyTcpServerTest {
    @Test
    public void testStartAndShutdown() throws ExecutionException, InterruptedException {
        final int poolSize = 4;
        final int backLog = 10;
        final int bindPort = 8080;
        final Consumer consumer = new EmptyConsumer(true, 1000L);

        Dispatcher dispatcher = new ThreadPoolExecutorDispatcher(
                poolSize, backLog, Collections.singletonList(consumer));
        Codec<NettyRpcCallContext, byte[]> codec = new TypicalObjectMapperCodec<>(NettyRpcCallContext.class);
        NettyServerSocketOptions options = new NettyServerSocketOptions();

        NettyTcpServer nettyTcpServer
                = new NettyTcpServer(dispatcher, codec, options, new InetSocketAddress(bindPort));
        nettyTcpServer.start().get();


        Assert.isTrue(nettyTcpServer.isActive(), "active状态不正确，此时服务已开启");
        Assert.isTrue(nettyTcpServer.isStarted(), "started状态不正确，此时服务已开启");

        nettyTcpServer.shutdown().get();

        Assert.isTrue(!nettyTcpServer.isActive(), "active状态不正确，此时服务已关闭");
        Assert.isTrue(!nettyTcpServer.isStarted(), "started状态不正确，此时服务已关闭");
    }

    @Test
    public void testReceive() throws ExecutionException, InterruptedException, EncodeException {
        final int poolSize = 4;
        final int backLog = 10;
        final int bindPort = 8080;
        final EmptyConsumer consumer = new EmptyConsumer(true, 1000L);

        Dispatcher dispatcher = new ThreadPoolExecutorDispatcher(
                poolSize, backLog, Collections.singletonList(consumer));
        Codec<NettyRpcCallContext, byte[]> codec = new TypicalObjectMapperCodec<>(NettyRpcCallContext.class);
        NettyServerSocketOptions options = new NettyServerSocketOptions()
                .setPipelineConfigurer(channelPipeline -> channelPipeline.addLast(new SpyChannelDuplexHandler("server")));

        NettyTcpServer nettyTcpServer
                = new NettyTcpServer(dispatcher, codec, options, new InetSocketAddress(bindPort));
        nettyTcpServer.start().get();

        Assert.isTrue(nettyTcpServer.isActive(), "active状态不正确，此时服务已开启");
        Assert.isTrue(nettyTcpServer.isStarted(), "started状态不正确，此时服务已开启");

        // 发送一条信息
        log.info("向NettyTcpServer发送一条信息");
        NettyClientSocketOptions clientOptions = new NettyClientSocketOptions()
                .setPipelineConfigurer(channelPipeline -> channelPipeline.addLast(new SpyChannelDuplexHandler("client")));
        NettyTcpClient nettyTcpClient
                = new NettyTcpClient(null, codec, clientOptions, new InetSocketAddress("127.0.0.1", bindPort));
        nettyTcpClient.start().get();
        while (!nettyTcpClient.isActive()) {
            Thread.sleep(200L);
        }

        RequestWrapper request = new RequestWrapper("1", 0, "success");
        NettyRpcCallContext nettyRpcCallContext = new NettyRpcCallContext(request);
        nettyTcpClient.writeWith(nettyRpcCallContext).get();
        nettyTcpClient.shutdown().get();

        Object lastReceiveContent = consumer.getLastReceiveContent();
        Assert.isTrue(lastReceiveContent instanceof NettyRpcCallContext, "接收到的数据类型不正确: " + lastReceiveContent.getClass());
        lastReceiveContent = ((NettyRpcCallContext) lastReceiveContent).getRequestContent();
        Assert.notNull(lastReceiveContent, "服务器没有收到消息");
        Assert.isTrue(lastReceiveContent.equals(request), "服务器收到的信息与客户端发出的信息不一致");
        log.info("服务器收到的信息与客户端发出的信息一致，测试通过！");

        nettyTcpServer.shutdown().get();


        Assert.isTrue(!nettyTcpServer.isActive(), "active状态不正确，此时服务已关闭");
        Assert.isTrue(!nettyTcpServer.isStarted(), "started状态不正确，此时服务已关闭");
    }

    @Test(expected = IllegalStateException.class)
    public void testRepeatStart() throws ExecutionException, InterruptedException {
        final int bindPort = 80;
        NettyTcpServer nettyTcpServer
                = new NettyTcpServer(null, null, null, new InetSocketAddress(bindPort));
        nettyTcpServer.start().get();
        nettyTcpServer.start();
    }

    @Test(expected = BindException.class)
    public void testBindRepeatPort() throws Throwable {
        final int bindPort = 80;
        NettyTcpServer nettyTcpServer1
                = new NettyTcpServer(null, null, null, new InetSocketAddress(bindPort));
        nettyTcpServer1.start().get();
        NettyTcpServer nettyTcpServer2
                = new NettyTcpServer(null, null, null, new InetSocketAddress(bindPort));
        try {
            nettyTcpServer2.start().get();
        } catch (ExecutionException e) {
            nettyTcpServer1.shutdown().get();
            throw e.getCause();
        }
    }

    @Test(timeout = 30000L)
    public void testReply() throws Throwable {
        final int poolSize = 2;
        final int backLog = 2;
        final int bindPort = 8080;
        final NettyReplyHelloConsumer helloConsumer = new NettyReplyHelloConsumer(true, 1000L);

        Dispatcher serverDispatcher = new ThreadPoolExecutorDispatcher(
                poolSize, backLog, Collections.singletonList(helloConsumer));
        Codec<NettyRpcCallContext, byte[]> codec = new TypicalObjectMapperCodec<>(NettyRpcCallContext.class);
        NettyServerSocketOptions options = new NettyServerSocketOptions()
                .setPipelineConfigurer(channelPipeline -> channelPipeline.addFirst(new SpyChannelDuplexHandler("server")));

        NettyTcpServer nettyTcpServer
                = new NettyTcpServer(serverDispatcher, codec, options, new InetSocketAddress(bindPort));
        nettyTcpServer.start().get();

        Assert.isTrue(nettyTcpServer.isActive(), "active状态不正确，此时服务已开启");
        Assert.isTrue(nettyTcpServer.isStarted(), "started状态不正确，此时服务已开启");

        // 发送一条信息
        log.info("向NettyTcpServer发送一条信息");
        EmptyConsumer clientConsumer = new EmptyConsumer(true, 1000L);
        Dispatcher clientDispatcher = new ThreadPoolExecutorDispatcher(poolSize, backLog, Collections.singletonList(clientConsumer));
        NettyClientSocketOptions clientOptions = new NettyClientSocketOptions()
                .setPipelineConfigurer(channelPipeline -> channelPipeline.addFirst(new SpyChannelDuplexHandler("client")));
        NettyTcpClient nettyTcpClient
                = new NettyTcpClient(clientDispatcher, codec, clientOptions, new InetSocketAddress("127.0.0.1", bindPort));
        nettyTcpClient.start().get();
        while (!nettyTcpClient.isActive()) {
            Thread.sleep(200L);
        }
        RequestWrapper request = new RequestWrapper("1", 0, "success");
        NettyRpcCallContext nettyRpcCallContext = new NettyRpcCallContext(request);
        nettyTcpClient.writeWith(nettyRpcCallContext).get();
        // 等待接收到Server的回应
        while (clientConsumer.getLastReceiveContent() == null) {
            Thread.sleep(200L);
        }
        log.info("接收到Server端响应的消息: {}", clientConsumer.getLastReceiveContent());
        nettyTcpClient.shutdown().get();

        Object lastReceiveContent = clientConsumer.getLastReceiveContent();
        Assert.isTrue(lastReceiveContent instanceof NettyRpcCallContext, "接收到的数据类型不正确: " + lastReceiveContent.getClass());
        lastReceiveContent = ((NettyRpcCallContext) lastReceiveContent).getRequestContent();
        Assert.notNull(lastReceiveContent, "服务器没有收到消息");
        Assert.isTrue(lastReceiveContent.equals(helloConsumer.getReplyObject()), "服务器收到的信息与客户端发出的信息不一致");
        log.info("服务器收到的信息与客户端发出的信息一致，测试通过！");

        nettyTcpServer.shutdown().get();

        Assert.isTrue(!nettyTcpServer.isActive(), "active状态不正确，此时服务已关闭");
        Assert.isTrue(!nettyTcpServer.isStarted(), "started状态不正确，此时服务已关闭");
    }

    @ChannelHandler.Sharable
    private static class SpyChannelDuplexHandler extends ChannelDuplexHandler {
        private final String name;

        public SpyChannelDuplexHandler(String name) {
            this.name = name;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("{} - CONNECT: {}", name, ctx.channel().remoteAddress());
            super.channelActive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.info("{} - READ[{}]: {}", name, ctx.channel().remoteAddress(), msg);
            super.channelRead(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            log.info("{} - READ COMPLETE: {}", name, ctx.channel().remoteAddress());
            super.channelReadComplete(ctx);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            log.info("{} - WRITE[{}]: {}", name, ctx.channel().remoteAddress(), msg);
            super.write(ctx, msg, promise);
        }

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception {
            log.info("{} - FLUSH: {}", name, ctx.channel().remoteAddress());
            super.flush(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info("{} - DISCONNECT: {}", name, ctx.channel().remoteAddress());
            super.channelInactive(ctx);
        }
    }
}
