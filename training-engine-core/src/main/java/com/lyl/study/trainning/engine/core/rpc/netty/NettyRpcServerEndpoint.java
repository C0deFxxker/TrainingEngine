package com.lyl.study.trainning.engine.core.rpc.netty;

import com.lyl.study.trainning.engine.core.exception.RpcException;
import com.lyl.study.trainning.engine.core.rpc.RpcEndpointRef;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty实现的Rpc终端点
 *
 * @author liyilin
 */
@Slf4j
public abstract class NettyRpcServerEndpoint extends NettyRpcEndpoint {
    protected final String name;
    protected final int port;
    protected AtomicBoolean started = new AtomicBoolean(false);
    protected AtomicBoolean terminated = new AtomicBoolean(true);

    protected ChannelFuture channelFuture;
    //处理Accept连接事件的线程，这里线程数设置为1即可，netty处理链接事件默认为单线程，过度设置反而浪费cpu资源
    protected NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
    //处理hadnler的工作线程，线程数据默认为 CPU 核心数乘以2
    protected NioEventLoopGroup workerGroup = new NioEventLoopGroup();

    public NettyRpcServerEndpoint(String name, int port) {
        this.name = name;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void start() {
        // 启动连接
        if (started.compareAndSet(false, true)) {
            try {
                final NettyRpcServerEndpoint that = this;
                doStart().addListener(future -> {
                    terminated.set(false);
                    that.onStart();
                });
            } catch (Throwable e) {
                try {
                    workerGroup.shutdownGracefully().sync();
                    bossGroup.shutdownGracefully().sync();
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
        final NettyRpcServerEndpoint that = this;
        //创建ServerBootstrap实例
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        //初始化ServerBootstrap的线程组
        serverBootstrap.group(workerGroup, workerGroup);
        //设置将要被实例化的ServerChannel类
        serverBootstrap.channel(NioServerSocketChannel.class);
        //在ServerChannelInitializer中初始化ChannelPipeline责任链，并添加到serverBootstrap中
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                that.setupChannelHandler(pipeline);
                pipeline.addLast(new NettyRpcRequestResolveInboundHandler(that));
            }
        });
        //标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        // 是否启用心跳保活机机制
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        //绑定端口后，开启监听
        channelFuture = serverBootstrap.bind(port);
        return channelFuture;
    }

    @Override
    public void stop() {
        // 关闭连接
        if (terminated.compareAndSet(false, true)) {
            try {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                if (channelFuture != null) {
                    final NettyRpcServerEndpoint that = this;
                    channelFuture.channel().closeFuture().addListener(future -> that.onStop());
                }
            } finally {
                started.set(false);
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

    @Override
    public RpcEndpointRef getSelfRef() {
        throw new UnsupportedOperationException();
//        SocketAddress socketAddress = channelFuture.channel().localAddress();
//        RpcAddress rpcAddress = new RpcAddress(socketAddress);
//        NettyRpcClientEndpoint nettyRpcClientEndpoint = new NettyRpcClientEndpoint(name, rpcAddress);
//        return nettyRpcClientEndpoint.getSelfRef();
    }
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
