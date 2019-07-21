package com.lyl.study.trainning.engine.core.net.netty;

import com.lyl.study.trainning.engine.core.net.TcpClient;
import com.lyl.study.trainning.engine.core.rpc.dispatch.Dispatcher;
import com.lyl.study.trainning.engine.core.rpc.serialize.Codec;
import com.lyl.study.trainning.engine.core.rpc.serialize.EncodeException;
import com.lyl.study.trainning.engine.core.util.PlatformUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    protected EventLoopGroup workerGroup = PlatformUtils.isWindows() ? new NioEventLoopGroup() : new EpollEventLoopGroup();
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
    private ClientChannelHandler clientChannelHandler = new ClientChannelHandler();

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

                    if (options.getPipelineConfigurer() != null) {
                        options.getPipelineConfigurer().accept(ch.pipeline());
                    }

                    ch.pipeline().addLast(clientChannelHandler);
                }
            }
        });

        // TODO 添加SSL配置

        this.bootstrap = _bootstrap;
    }

    @Override
    protected Future<Void> doSend(T object) throws EncodeException {
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
        return bootstrap.connect(connectAddress)
                .addListener(new ReconnectingChannelListener(delayMillis, maxAttempts));
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
     * 用于主动向服务器发送消息的ChannelHandler
     */
    private static class ClientChannelHandler extends ChannelInboundHandlerAdapter {
        private ChannelHandlerContext context;

        Future<Void> send(byte[] msg) {
            if (context == null) {
                throw new IllegalStateException("Connection is inactive");
            }

            return context.writeAndFlush(msg);
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

    /**
     * 自动重连用到Channel监听器
     */
    private class ReconnectingChannelListener implements ChannelFutureListener {
        private final AtomicInteger attempts = new AtomicInteger(0);

        private Timer timer = new Timer();

        private final int maxAttempts;
        private final long delayMillis;

        private ReconnectingChannelListener(long delayMillis,
                                            int maxAttempts) {
            this.delayMillis = delayMillis;
            this.maxAttempts = maxAttempts;
        }

        @Override
        public void operationComplete(final ChannelFuture future) {
            if (!future.isSuccess()) {
                int attempt = attempts.incrementAndGet();
                if (attempt >= maxAttempts) {
                    if (log.isDebugEnabled()) {
                        log.debug("重连次数超过上限，停止自动重连");
                    }
                    return;
                }
                attemptReconnect();
            } else {
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
                        if (log.isInfoEnabled()) {
                            log.info("CLOSED: " + ioCh);
                        }

                        attemptReconnect();
                        super.channelInactive(ctx);
                    }
                });
            }
        }

        private void attemptReconnect() {
            if (log.isInfoEnabled()) {
                log.info("Failed to connect to {}. Attempting reconnect in {}ms.", connectAddress, delayMillis);
            }

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    bootstrap.connect(connectAddress)
                            .addListener(ReconnectingChannelListener.this);
                }
            }, delayMillis);
        }
    }
}
