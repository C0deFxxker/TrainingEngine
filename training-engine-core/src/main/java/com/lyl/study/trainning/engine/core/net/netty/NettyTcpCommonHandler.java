//package com.lyl.study.trainning.engine.core.net.netty;
//
//import com.lyl.study.trainning.engine.core.net.NetworkEndpoint;
//import com.lyl.study.trainning.engine.core.rpc.dispatch.Dispatcher;
//import com.lyl.study.trainning.engine.core.rpc.netty.NettyRpcCallContext;
//import com.lyl.study.trainning.engine.core.rpc.serialize.Codec;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.ChannelDuplexHandler;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelPromise;
//import io.netty.handler.codec.EncoderException;
//import io.netty.util.ReferenceCountUtil;
//
///**
// * NettyTcpClient与NettyTcpServer的公共内置Handler
// *
// * @author liyilin
// */
//public class NettyTcpCommonHandler<T> extends ChannelDuplexHandler {
//    private final Codec<T, byte[]> codec;
//    private final Dispatcher dispatcher;
//
//    NettyTcpCommonHandler(NetworkEndpoint<T> networkEndpoint) {
//        codec = networkEndpoint.getCodec();
//        dispatcher = networkEndpoint.getDispatcher();
//    }
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        boolean release = true;
//        try {
//            if (msg instanceof ByteBuf) {
//                ByteBuf byteBuf = (ByteBuf) msg;
//                byte[] bytes = new byte[byteBuf.readableBytes()];
//                byteBuf.readBytes(bytes);
//                T decode = codec.decode(bytes);
//                dispatcher.dispatch(new NettyRpcCallContext(decode, ctx));
//            } else {
//                release = false;
//                ctx.fireChannelRead(msg);
//            }
//        } finally {
//            if (release) {
//                ReferenceCountUtil.release(msg);
//            }
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//        ByteBuf buf = null;
//        try {
//            // TODO 缺少泛型类比对
//            if (msg instanceof NettyRpcCallContext) {
//                NettyRpcCallContext cast = (NettyRpcCallContext) msg;
//                buf = ctx.alloc().heapBuffer();
//                try {
//                    byte[] encode = codec.encode((T) cast.getRequestContent());
//                    buf.writeBytes(encode);
//                } finally {
//                    ReferenceCountUtil.release(cast);
//                }
//
//                if (buf.isReadable()) {
//                    ctx.write(buf, promise);
//                } else {
//                    buf.release();
//                    ctx.write(Unpooled.EMPTY_BUFFER, promise);
//                }
//                buf = null;
//            } else {
//                ctx.write(msg, promise);
//            }
//        } catch (Throwable e) {
//            throw new EncoderException(e);
//        } finally {
//            if (buf != null) {
//                buf.release();
//            }
//        }
//    }
//
//    @Override
//    public void flush(ChannelHandlerContext ctx) throws Exception {
//        super.flush(ctx);
//    }
//}
