package com.lyl.study.trainning.engine.core.rpc.netty;

import com.lyl.study.trainning.engine.core.exception.RpcException;
import com.lyl.study.trainning.engine.core.rpc.RpcAddress;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty实现的Rpc客户端
 *
 * @author liyilin
 */
public abstract class NettyRpcClientEndpoint extends NettyRpcEndpoint {
    protected final String name;
    protected final RpcAddress rpcAddress;
    protected AtomicBoolean started = new AtomicBoolean(false);
    protected AtomicBoolean terminated = new AtomicBoolean(true);

    protected ChannelFuture channelFuture;
    protected NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    public NettyRpcClientEndpoint(String name, RpcAddress rpcAddress) {
        this.name = name;
        this.rpcAddress = rpcAddress;
    }

    public ChannelHandlerContext getContext() {
        return context;
    }

    public NettyRpcClientEndpoint setContext(ChannelHandlerContext context) {
        this.context = context;
        return this;
    }

    public String getName() {
        return name;
    }

    public RpcAddress getRpcAddress() {
        return rpcAddress;
    }

    @Override
    public void start() {
        // 启动连接
        if (started.compareAndSet(false, true)) {
            try {
                final NettyRpcClientEndpoint that = this;
                doStart().addListener(future -> {
                    terminated.set(false);
                    that.onStart();
                });
            } catch (Throwable e) {
                try {
                    eventLoopGroup.shutdownGracefully().sync();
                    if (channelFuture != null) {
                        channelFuture.channel().closeFuture().sync();
                    }
                } catch (InterruptedException ex) {
                    started.set(false);
                    throw new RpcException(ex);
                }
                started.set(false);
            }
        } else {
            throw new RpcException("Endpoint has started");
        }
    }

    protected ChannelFuture doStart() {
        final NettyRpcClientEndpoint that = this;
        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        that.setupChannelHandler(pipeline);
                        pipeline.addLast(new NettyRpcRequestResolveInboundHandler(that));
                    }
                });
        channelFuture = bootstrap.connect(rpcAddress.getHost(), rpcAddress.getPort());
        return channelFuture;
    }

    @Override
    public void stop() {
        // 关闭连接
        if (terminated.compareAndSet(false, true)) {
            eventLoopGroup.shutdownGracefully();
            if (channelFuture != null) {
                final NettyRpcClientEndpoint that = this;
                channelFuture.channel().closeFuture().addListener(future -> {
                    that.onStop();
                    started.set(false);
                });
            }
        } else {
            throw new RpcException("Endpoint is not running");
        }
    }

    @Override
    public void awaitStart() throws InterruptedException {
        while (started.get() && terminated.get()) {
            Thread.sleep(100L);
        }

        if (!isActive()) {
            throw new IllegalStateException("Endpoint is not opening");
        }
    }

    @Override
    public boolean isActive() {
        return started.get() && !terminated.get();
    }

    @Override
    public boolean isOpen() {
        return started.get();
    }

//    @Override
//    public RpcEndpointRef getSelfRef() {
//        return new NettyRpcEndpointRef(this);
//    }
//
//    @Override
//    public void receive(RpcCallContext context) {
//        // 留给子类实现
//    }
//
//    @Override
//    public void onError(Throwable cause) {
//        // 留给子类实现
//    }
//
//    @Override
//    public void onConnected(RpcAddress remoteAddress) {
//        // 留给子类实现
//    }
//
//    @Override
//    public void onDisconnected(RpcAddress remoteAddress) {
//        // 留给子类实现
//    }
//
//    @Override
//    public void onStart() {
//        // 留给子类实现
//    }
//
//    @Override
//    public void onStop() {
//        // 留给子类实现
//    }
}
