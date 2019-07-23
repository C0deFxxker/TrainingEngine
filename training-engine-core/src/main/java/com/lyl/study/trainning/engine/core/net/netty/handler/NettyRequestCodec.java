package com.lyl.study.trainning.engine.core.net.netty.handler;

import com.lyl.study.trainning.engine.core.rpc.serialize.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * NettyTcpClient与NettyTcpServer公共的消息编码Handler
 *
 * @author liyilin
 */
public class NettyRequestCodec<T> extends ByteToMessageCodec<T> {
    private final Codec<T, byte[]> codec;

    public NettyRequestCodec(Codec<T, byte[]> codec) {
        super(codec.getInClass());
        this.codec = codec;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, T msg, ByteBuf out) throws Exception {
        byte[] encode = codec.encode(msg);
        out.writeBytes(encode);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        T decode = codec.decode(bytes);
        out.add(decode);
    }
}
