package com.lyl.study.trainning.engine.core.rpc.netty;

import com.lyl.study.trainning.engine.core.rpc.RequestMessage;
import com.lyl.study.trainning.engine.core.rpc.serialize.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Netty请求编解码拦截器
 *
 * @author liyilin
 */
@Slf4j
public class NettyRequestCodecHandler extends ByteToMessageCodec<RequestMessage> {
    private Codec<RequestMessage, byte[]> codec;

    public NettyRequestCodecHandler(Codec<RequestMessage, byte[]> codec) {
        this.codec = codec;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, RequestMessage msg, ByteBuf out) throws Exception {
        out.writeBytes(codec.encode(msg));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        out.add(codec.decode(bytes));
    }
}
