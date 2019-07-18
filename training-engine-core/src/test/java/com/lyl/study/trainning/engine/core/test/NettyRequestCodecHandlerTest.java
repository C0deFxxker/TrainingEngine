package com.lyl.study.trainning.engine.core.test;

import com.lyl.study.trainning.engine.core.rpc.RequestMessage;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyRequestCodecHandler;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyResponse;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyRpcCallContext;
import com.lyl.study.trainning.engine.core.rpc.netty.NettyRpcEndpointRef;
import com.lyl.study.trainning.engine.core.rpc.serialize.ObjectMapperCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.local.LocalAddress;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * NettyRequestCodec单元测试类
 *
 * @author liyilin
 */
@Slf4j
public class NettyRequestCodecHandlerTest {
    @Test
    public void test1() {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                new NettyRequestCodecSpyOutboundHandler(),
                new NettyRequestCodecHandler(new ObjectMapperCodec<>(RequestMessage.class)),
                new NettyRequestCodecSpyInboundHandler()
        );
        embeddedChannel.connect(new LocalAddress("1"));

        final String content = "[\"com.lyl.study.trainning.engine.core.rpc.RequestMessage\",{\"requestId\":\"1\",\"senderAddress\":[\"com.lyl.study.trainning.engine.core.rpc.RpcAddress\",{\"host\":\"127.0.0.1\",\"port\":80}],\"content\":[\"com.lyl.study.trainning.engine.core.rpc.netty.NettyResponse\",{\"content\":[\"java.util.HashMap\",{\"msg\":\"success\",\"code\":0}],\"success\":true}]}]";
        embeddedChannel.writeInbound(Unpooled.copiedBuffer(content.getBytes()));
    }

    private static class NettyRequestCodecSpyInboundHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            assertTrue(msg instanceof RequestMessage);
            RequestMessage requestMessage = (RequestMessage) msg;
            log.info("接收到[" + requestMessage.getSenderAddress() + "]的信息内容: " + requestMessage);

            NettyRpcEndpointRef rpcEndpointRef = new NettyRpcEndpointRef(ctx);
            rpcEndpointRef.setName("<unknown>");
            NettyRpcCallContext rpcCallContext = new NettyRpcCallContext(rpcEndpointRef, requestMessage);
            Map<String, Object> map = new HashMap<>(2);
            map.put("code", 0);
            map.put("msg", "success");
            rpcCallContext.reply(map);
        }
    }

    private static class NettyRequestCodecSpyOutboundHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            assertTrue(msg instanceof ByteBuf);
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            String responseString = new String(bytes);
            log.info("准备向[" + ctx.channel().remoteAddress() + "]发送内容: \n" + responseString);

            String[] splits = responseString.split("\r\n");
            assertEquals(4, splits.length);
            assertEquals("1.0", splits[0]);
            assertEquals(NettyResponse.class.getName(), splits[2]);
        }
    }
}
