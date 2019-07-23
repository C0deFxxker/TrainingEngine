package com.lyl.study.trainning.engine.core.test.net;

import com.lyl.study.trainning.engine.core.net.TcpServer;
import com.lyl.study.trainning.engine.core.net.netty.NettyClientSocketOptions;
import com.lyl.study.trainning.engine.core.net.netty.NettyServerSocketOptions;
import com.lyl.study.trainning.engine.core.net.netty.NettyTcpClient;
import com.lyl.study.trainning.engine.core.net.netty.NettyTcpServer;
import com.lyl.study.trainning.engine.core.rpc.dispatch.Consumer;
import com.lyl.study.trainning.engine.core.rpc.dispatch.Dispatcher;
import com.lyl.study.trainning.engine.core.rpc.dispatch.ThreadPoolExecutorDispatcher;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyRpcCallContext;
import com.lyl.study.trainning.engine.core.rpc.serialize.Codec;
import com.lyl.study.trainning.engine.core.rpc.serialize.TypicalObjectMapperCodec;
import com.lyl.study.trainning.engine.core.test.mock.EmptyConsumer;
import com.lyl.study.trainning.engine.core.util.Assert;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NettyTcpClient单元测试类
 *
 * @author liyilin
 */
@Slf4j
public class NettyTcpClientTest {
    @Test(timeout = 15000L)
    public void testStartAndShutdown() throws ExecutionException, InterruptedException {
        final int serverPort = 8080;
        TcpServer<NettyRpcCallContext> tcpServer = buildLocalServer(serverPort);
        tcpServer.start().get();

        NettyTcpClient nettyTcpClient = new NettyTcpClient(
                null, null, null, new InetSocketAddress("127.0.0.1", serverPort));

        nettyTcpClient.start().get();

        Assert.isTrue(nettyTcpClient.isActive(), "active状态不正确，此时客户端已开启");
        Assert.isTrue(nettyTcpClient.isStarted(), "started状态不正确，此时客户端已开启");

        nettyTcpClient.shutdown().get();

        Assert.isTrue(!nettyTcpClient.isActive(), "active状态不正确，此时客户端已关闭");
        Assert.isTrue(!nettyTcpClient.isStarted(), "started状态不正确，此时客户端已关闭");

        tcpServer.shutdown().get();
    }

//    NettyTcpServerTest中已做了类似的测试，这里不重写了
//    @Test
//    public void testSendAndReceive() {
//    }

    @Test(expected = ConnectException.class, timeout = 5000L)

    public void testConnectFail() throws Throwable {
        final int serverPort = 30000;

        NettyClientSocketOptions options = new NettyClientSocketOptions();
        options.setReconnectDelayMillis(1000L);
        options.setReconnectMaxAttempts(3);
        NettyTcpClient nettyTcpClient = new NettyTcpClient(
                null, null, options, new InetSocketAddress("127.0.0.1", serverPort));
        try {
            nettyTcpClient.start().get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test(timeout = 30000L)
    public void testReconnect() throws Throwable {
        final int serverPort = 8080;
        final int shutdownTimes = 2;
        TcpServer<NettyRpcCallContext> tcpServer = buildLocalServer(serverPort);
        tcpServer.start().get();
        final AtomicInteger startTime = new AtomicInteger(0);
        NettyTcpClient nettyTcpClient = new NettyTcpClient(
                null, null, null, new InetSocketAddress("127.0.0.1", serverPort)) {
            @Override
            protected Future<Void> doStart() {
                startTime.incrementAndGet();
                return super.doStart();
            }
        };

        nettyTcpClient.start().get();
        log.info("客户端与服务器连接成功");
        Thread.sleep(500L);

        for (int i = 0; i < shutdownTimes; i++) {
            log.info("关闭服务器");
            tcpServer.shutdown().get();
            Thread.sleep(500L);
            log.info("重启服务器");
            tcpServer = buildLocalServer(serverPort);
            tcpServer.start().get();
            log.info("服务器重启完毕");
            while (!nettyTcpClient.isActive()) {
                Thread.sleep(200L);
            }
            log.info("客户端重连成功");
        }

        log.info("关闭客户端以及服务器");
        nettyTcpClient.shutdown().get();
        tcpServer.shutdown().get();

        Assert.isTrue(startTime.get() == shutdownTimes + 1, "startTime不正确: " + startTime.get());
    }

    private TcpServer<NettyRpcCallContext> buildLocalServer(int port) throws ExecutionException, InterruptedException {
        final int poolSize = 4;
        final int backLog = 10;
        final Consumer consumer = new EmptyConsumer(true, 1000L);

        Dispatcher dispatcher = new ThreadPoolExecutorDispatcher(
                poolSize, backLog, Collections.singletonList(consumer));
        Codec<NettyRpcCallContext, byte[]> codec = new TypicalObjectMapperCodec<>(NettyRpcCallContext.class);
        NettyServerSocketOptions options = new NettyServerSocketOptions();


        return new NettyTcpServer(dispatcher, codec, options, new InetSocketAddress(port));
    }
}
