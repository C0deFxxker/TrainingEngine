package com.lyl.study.trainning.engine.core.rpc.netty;

import com.lyl.study.trainning.engine.core.exception.RpcDispatchException;
import com.lyl.study.trainning.engine.core.rpc.RequestMessage;
import com.lyl.study.trainning.engine.core.rpc.RpcAddress;
import com.lyl.study.trainning.engine.core.rpc.RpcCallContext;
import com.lyl.study.trainning.engine.core.rpc.dispatch.RpcDispatchRequest;
import com.lyl.study.trainning.engine.core.rpc.dispatch.RpcDispatcher;
import com.lyl.study.trainning.engine.core.rpc.serialize.ObjectMapperCodec;
import io.netty.channel.ChannelPipeline;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 带有Rpc服务分发功能的Netty服务器
 *
 * @author liyilin
 */
@Slf4j
public class NettyRpcDispatcherEndpoint extends NettyRpcServerEndpoint {
    /**
     * Rpc服务分发器
     */
    private RpcDispatcher rpcDispatcher;

    public NettyRpcDispatcherEndpoint(String name, int port, RpcDispatcher rpcDispatcher) {
        super(name, port);
        this.rpcDispatcher = rpcDispatcher;
    }

    @Override
    protected void setupChannelHandler(ChannelPipeline channelPipeline) {
        channelPipeline.addLast(new NettyRequestCodecHandler(new ObjectMapperCodec<>(RequestMessage.class)));
    }

    @Override
    public void receive(RpcCallContext context) {
        Object requestContent = context.getRequestContent();
        assert requestContent instanceof RpcDispatchRequest : "请求参数类型必须是" + RpcDispatchRequest.class;
        RpcDispatchRequest request = (RpcDispatchRequest) requestContent;
        String serviceName = request.getServiceName();
        Object[] arguments = request.getArguments();
        if (arguments == null) {
            arguments = new Object[0];
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("开始调用Rpc服务, 服务名={}, 参数列表={}", serviceName, Arrays.asList(arguments).toString());
            }
            Object result = rpcDispatcher.dispatch(serviceName, arguments);
            if (log.isDebugEnabled()) {
                log.debug("Rpc服务调用完毕, 服务名={}, 返回值={}", serviceName, result);
            }
            context.reply(result);
        } catch (RpcDispatchException e) {
            log.error("Rpc服务调用异常: " + e.getMessage(), e);
            context.replyFailure(e);
        }
    }

    @Override
    public void onError(Throwable cause) {
        log.error("发生了异常: " + cause.getMessage(), cause);
    }

    @Override
    public void onConnected(RpcAddress remoteAddress) {

    }

    @Override
    public void onDisconnected(RpcAddress remoteAddress) {

    }

    @Override
    public void onStart() {
        log.info("Rpc服务初始化完毕，绑定{}端口", port);
    }

    @Override
    public void onStop() {
        log.info("Rpc服务销毁完毕");
    }
}
