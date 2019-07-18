package com.lyl.study.trainning.engine.core.rpc.netty;

import com.lyl.study.trainning.engine.core.rpc.RpcAddress;
import lombok.extern.slf4j.Slf4j;

/**
 * 带有断线重连机制的Netty客户端
 *
 * @author liyilin
 */
@Slf4j
public abstract class AutoReconnectNettyRpcClientEndpoint extends NettyRpcClientEndpoint {
    private final long retryInterval;

    public AutoReconnectNettyRpcClientEndpoint(String name, RpcAddress rpcAddress, long retryInterval) {
        super(name, rpcAddress);
        this.retryInterval = retryInterval;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    @Override
    public void onDisconnected(RpcAddress remoteAddress) {
        if (isActive()) {
            new Thread(() -> {
                log.info("尝试断线重连: host={}, port={}", remoteAddress.getHost(), remoteAddress.getPort());
                boolean finished = false;
                while (!finished) {
                    try {
                        Thread.sleep(retryInterval);
                        doStart().sync();
                        finished = true;
                    } catch (InterruptedException e) {
                        return;
                    } catch (Throwable e) {
                        if (log.isDebugEnabled()) {
                            log.debug("断线重连失败，异常信息: {}", e.getMessage());
                        }
                        try {
                            eventLoopGroup.shutdownGracefully().sync();
                            if (channelFuture != null) {
                                channelFuture.channel().closeFuture().sync();
                            }
                        } catch (InterruptedException ex) {
                            return;
                        }
                    }
                }
            }).start();
        }
    }
}
