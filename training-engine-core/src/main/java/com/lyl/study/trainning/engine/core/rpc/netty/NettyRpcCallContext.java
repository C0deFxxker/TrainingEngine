package com.lyl.study.trainning.engine.core.rpc.netty;

import com.lyl.study.trainning.engine.core.rpc.RequestMessage;
import com.lyl.study.trainning.engine.core.rpc.RpcAddress;
import com.lyl.study.trainning.engine.core.rpc.RpcCallContext;
import com.lyl.study.trainning.engine.core.rpc.RpcEndpointRef;

/**
 * Netty实现的Rpc调用上下文
 *
 * @author liyilin
 */
public class NettyRpcCallContext implements RpcCallContext {
    private RpcEndpointRef sender;
    private RequestMessage requestMessage;

    public NettyRpcCallContext(RpcEndpointRef sender, RequestMessage requestMessage) {
        this.sender = sender;
        this.requestMessage = requestMessage;
    }

    @Override
    public RpcAddress getSenderAddress() {
        return sender.getAddress();
    }

    public RequestMessage getRequestMessage() {
        return requestMessage;
    }

    @Override
    public Object getRequestContent() {
        return requestMessage.getContent();
    }

    @Override
    public void reply(Object response) {
        NettyResponse nettyResponse = NettyResponse.success(response);

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setRequestId(this.requestMessage.getRequestId());
        requestMessage.setContent(nettyResponse);
        requestMessage.setReceiver(sender);

        sender.send(requestMessage);
    }

    @Override
    public void replyFailure(Throwable e) {
        NettyResponse nettyResponse = NettyResponse.fail(e);

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setRequestId(this.requestMessage.getRequestId());
        requestMessage.setContent(nettyResponse);
        requestMessage.setReceiver(sender);

        sender.send(requestMessage);
    }
}
