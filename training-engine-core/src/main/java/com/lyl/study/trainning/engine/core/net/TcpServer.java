package com.lyl.study.trainning.engine.core.net;

import com.lyl.study.trainning.engine.core.net.config.ServerSocketOptions;
import com.lyl.study.trainning.engine.core.rpc.dispatch.Dispatcher;
import com.lyl.study.trainning.engine.core.rpc.serialize.Codec;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

/**
 * Tcp服务器抽象类
 *
 * @param <T> 受理的消息类型
 * @author liyilin
 */
@Slf4j
public abstract class TcpServer<T> extends NetworkEndpoint<T> {
    @Getter
    protected final ServerSocketOptions socketOptions;
    @Getter
    protected final SocketAddress listenAddress;

    public TcpServer(Dispatcher dispatcher,
                     Codec<T, byte[]> codec,
                     ServerSocketOptions socketOptions,
                     SocketAddress listenAddress) {
        super(dispatcher, codec);
        this.socketOptions = socketOptions;
        this.listenAddress = listenAddress;
    }
}
