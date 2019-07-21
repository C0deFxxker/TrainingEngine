package com.lyl.study.trainning.engine.core.net.netty;

import com.lyl.study.trainning.engine.core.net.TcpServer;
import com.lyl.study.trainning.engine.core.rpc.dispatch.Dispatcher;
import com.lyl.study.trainning.engine.core.rpc.serialize.Codec;
import com.lyl.study.trainning.engine.core.util.PlatformUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty实现的Tcp服务器
 *
 * @author liyilin
 */
@Slf4j
public class NettyTcpServer<T> extends TcpServer<T> {
    /**
     * 处理Accept连接事件的线程，这里线程数设置为1即可，netty处理链接事件默认为单线程，过度设置反而浪费cpu资源
     */
    protected EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    /**
     * 处理handler的工作线程，线程数据默认为 CPU 核心数乘以2
     */
    protected EventLoopGroup workerGroup = PlatformUtils.isWindows() ? new NioEventLoopGroup() : new EpollEventLoopGroup();
    /**
     * Netty服务器启动对象
     */
    protected ServerBootstrap bootstrap;
    /**
     * 服务器是否已经生效了
     */
    protected AtomicBoolean active = new AtomicBoolean(false);

    public NettyTcpServer(Dispatcher dispatcher,
                          Codec<T, byte[]> codec,
                          NettyServerSocketOptions options,
                          SocketAddress bindAddress) {
        super(dispatcher, codec, options, bindAddress);

        ServerBootstrap _serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(EpollEventLoopGroup.class.isAssignableFrom(workerGroup.getClass())
                        ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .localAddress((null == bindAddress ? new InetSocketAddress(0) : bindAddress))
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        if (options != null) {
            _serverBootstrap = _serverBootstrap
                    .option(ChannelOption.SO_BACKLOG, options.getBacklog())
                    .option(ChannelOption.SO_RCVBUF, options.getRcvbuf())
                    .option(ChannelOption.SO_SNDBUF, options.getSndbuf())
                    .option(ChannelOption.SO_REUSEADDR, options.isReuseAddr());
        }

        _serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(final SocketChannel ch) throws Exception {
                if (options != null) {
                    SocketChannelConfig config = ch.config();
                    config.setReceiveBufferSize(options.getRcvbuf());
                    config.setSendBufferSize(options.getSndbuf());
                    config.setKeepAlive(options.isKeepAlive());
                    config.setReuseAddress(options.isReuseAddr());
                    config.setSoLinger(options.getLingerSeconds());
                    config.setTcpNoDelay(options.isTcpNoDeplay());
                }

                if (log.isDebugEnabled()) {
                    log.debug("CONNECT {}", ch);
                }

                if (null != options && null != options.getPipelineConfigurer()) {
                    options.getPipelineConfigurer().accept(ch.pipeline());
                }
            }
        });

        // TODO 添加SSL配置

        this.bootstrap = _serverBootstrap;
    }

    @Override
    protected Future<Void> doStart() {
        return bootstrap.bind().addListener(future -> {
            active.set(true);
            if (log.isDebugEnabled()) {
                log.debug("Server is active now");
            }
        });
    }

    @Override
    protected Future<Void> doShutdown() {
        FutureTask<Void> futureTask = new FutureTask<>(() -> {
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
            active.set(false);
            if (log.isDebugEnabled()) {
                log.debug("Server is shutdown now");
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
}
