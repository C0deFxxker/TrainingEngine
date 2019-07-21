package com.lyl.study.trainning.engine.core.net;

import com.lyl.study.trainning.engine.core.net.config.ClientSocketOptions;
import com.lyl.study.trainning.engine.core.rpc.dispatch.Dispatcher;
import com.lyl.study.trainning.engine.core.rpc.serialize.Codec;
import com.lyl.study.trainning.engine.core.rpc.serialize.DecodeException;
import com.lyl.study.trainning.engine.core.rpc.serialize.EncodeException;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.Future;

/**
 * Tcp客户端抽象类
 *
 * @author liyilin
 */
@Slf4j
public abstract class TcpClient<T> extends SocketChannel<T> {
    protected final ClientSocketOptions clientSocketOptions;
    protected final SocketAddress connectAddress;

    public TcpClient(Dispatcher dispatcher,
                     Codec<T, byte[]> codec,
                     ClientSocketOptions clientSocketOptions,
                     SocketAddress connectAddress) {
        super(dispatcher, codec);
        this.clientSocketOptions = clientSocketOptions;
        this.connectAddress = connectAddress;
    }

    /**
     * 向服务器发送消息
     *
     * @param object 要发送的消息对象
     * @throws EncodeException 要发送的消息对象序列化异常
     */
    public final Future<Void> send(T object) throws EncodeException {
        if (isActive()) {
            return doSend(object);
        } else {
            throw new IllegalStateException("Client is inactive.");
        }
    }

    /**
     * 向服务器发送消息的具体实现
     *
     * @param object 要发送的消息对象
     * @throws EncodeException 要发送的消息对象序列化异常
     */
    protected abstract Future<Void> doSend(T object) throws EncodeException;

//    /**
//     * 向服务器发送请求消息（一请求一响应模式）
//     *
//     * @param object 要发送的消息对象
//     * @return 服务器响应的消息对象
//     * @throws EncodeException 要发送的消息对象序列化异常
//     * @throws DecodeException 反序列化服务器响应的消息时遇到的异常
//     */
//    public final <V> Future<V> ask(T object) throws EncodeException, DecodeException {
//        if (isActive()) {
//            return doAsk(object);
//        } else {
//            throw new IllegalStateException("Client is not active.");
//        }
//    }
//
//    /**
//     * 向服务器发送请求消息的具体实现
//     *
//     * @param object 要发送的消息对象
//     * @return 服务器响应的消息对象
//     * @throws EncodeException 要发送的消息对象序列化异常
//     * @throws DecodeException 反序列化服务器响应的消息时遇到的异常
//     */
//    protected abstract <V> Future<V> doAsk(T object) throws EncodeException, DecodeException;

    public ClientSocketOptions getClientSocketOptions() {
        return clientSocketOptions;
    }

    public SocketAddress getConnectAddress() {
        return connectAddress;
    }
}
