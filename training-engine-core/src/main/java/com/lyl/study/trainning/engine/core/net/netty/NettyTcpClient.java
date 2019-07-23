package com.lyl.study.trainning.engine.core.net.netty;

import com.lyl.study.trainning.engine.core.net.NetworkEndpoint;
import com.lyl.study.trainning.engine.core.net.TcpClient;
import com.lyl.study.trainning.engine.core.net.netty.handler.DispatchChannelInboundHandler;
import com.lyl.study.trainning.engine.core.net.netty.handler.NettyRequestCodec;
import com.lyl.study.trainning.engine.core.rpc.dispatch.Dispatcher;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyRpcCallContext;
import com.lyl.study.trainning.engine.core.rpc.serialize.Codec;
import com.lyl.study.trainning.engine.core.rpc.serialize.EncodeException;
import com.lyl.study.trainning.engine.core.util.InvokeUtils;
import com.lyl.study.trainning.engine.core.util.PlatformUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty实现的Tcp客户端
 *
 * @author liyilin
 */
@Slf4j
public class NettyTcpClient<T> extends TcpClient<T> {
    /**
     * 处理handler的工作线程，线程数据默认为 CPU 核心数乘以2
     */
    protected EventLoopGroup workerGroup = PlatformUtils.isWindows() || PlatformUtils.isMacOS()
            ? new NioEventLoopGroup() : new EpollEventLoopGroup();
    /**
     * Netty客户端启动对象
     */
    protected Bootstrap bootstrap;
    /**
     * 客户端是否已经生效了
     */
    protected AtomicBoolean active = new AtomicBoolean(false);
    /**
     * 用于发送消息的ChannelHandler
     */
    private ClientChannelHandler clientChannelHandler = new ClientChannelHandler(this);

    public NettyTcpClient(Dispatcher dispatcher,
                          Codec<T, byte[]> codec,
                          final NettyClientSocketOptions options,
                          final SocketAddress connectAddress) {
        super(dispatcher, codec, options, connectAddress);

        Bootstrap _bootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(EpollEventLoopGroup.class.isAssignableFrom(workerGroup.getClass())
                        ? EpollSocketChannel.class : NioSocketChannel.class);

        if (options != null) {
            _bootstrap = _bootstrap.option(ChannelOption.SO_RCVBUF, options.getRcvbuf())
                    .option(ChannelOption.SO_SNDBUF, options.getSndbuf())
                    .option(ChannelOption.SO_KEEPALIVE, options.isKeepAlive())
                    .option(ChannelOption.SO_LINGER, options.getLingerSeconds())
                    .option(ChannelOption.TCP_NODELAY, options.isTcpNoDeplay());
        }

        _bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(final SocketChannel ch) {
                if (options != null) {
                    ch.config().setConnectTimeoutMillis((int) options.getTimeoutMillis());

                    if (codec != null) {
                        ch.pipeline().addLast(new NettyRequestCodec<>(codec));
                    }
                    ch.pipeline().addLast(clientChannelHandler);

                    if (options.getPipelineConfigurer() != null) {
                        options.getPipelineConfigurer().accept(ch.pipeline());
                    }
                }
            }
        });

        // TODO 添加SSL配置

        this.bootstrap = _bootstrap;
    }

    @Override
    protected Future<Void> doWriteWith(T object) throws EncodeException {
        byte[] encode = getCodec().encode(object);
        return clientChannelHandler.send(encode);
    }

    @Override
    protected Future<Void> doStart() {
        NettyClientSocketOptions options = (NettyClientSocketOptions) getClientSocketOptions();
        long delayMillis = 5000L;
        int maxAttempts = Integer.MAX_VALUE;
        if (options != null) {
            delayMillis = options.getReconnectDelayMillis();
            maxAttempts = options.getReconnectMaxAttempts();
        }

        // TODO 迟点编写一个全局线程池，专门执行这种小任务
        return InvokeUtils.failRetry(() -> {
            if (isStarted()) {
                ChannelFuture future = bootstrap.connect(connectAddress).sync();
                // connected
                if (log.isInfoEnabled()) {
                    log.info("CONNECTED: " + future.channel());
                }
                active.set(true);

                // 添加故障重连Handler
                final Channel ioCh = future.channel();
                ioCh.pipeline().addLast(new ChannelDuplexHandler() {
                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        if (isStarted()) {
                            if (log.isInfoEnabled()) {
                                log.info("CLOSED: " + ioCh);
                            }
                            active.set(false);

                            doStart();
                        }
                        super.channelInactive(ctx);
                    }
                });
            }
            // 其它线程把客户端关闭后，就没必要继续重试了
            return null;
        }, delayMillis, maxAttempts);
    }

    @Override
    protected Future<Void> doShutdown() {
        FutureTask<Void> futureTask = new FutureTask<>(() -> {
            workerGroup.shutdownGracefully().sync();
            active.set(false);
            if (log.isDebugEnabled()) {
                log.debug("Client is shutdown now");
            }
            return null;
        });
        // TODO 迟点编写一个全局线程池，专门执行这种小任务
        new Thread(futureTask).start();
        return futureTask;
    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    /**
     * NettyTcpClient内置Handler
     */
    @ChannelHandler.Sharable
    private class ClientChannelHandler extends DispatchChannelInboundHandler {
        private ChannelHandlerContext context;

        ClientChannelHandler(NetworkEndpoint<T> socketChannel) {
            super(socketChannel.getDispatcher());
        }

        Future<Void> send(byte[] msg) {
            if (context == null) {
                throw new IllegalStateException("Connection is inactive");
            }

            ByteBuf byteBuf = Unpooled.copiedBuffer(msg);
            return context.writeAndFlush(byteBuf);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            context = ctx;
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            context = null;
            super.channelInactive(ctx);
        }
    }
}
