package com.lyl.study.trainning.engine.core.rpc.netty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Netty响应对象
 *
 * @author liyilin
 */
@Data
@ToString
@NoArgsConstructor
public class NettyResponse {
    private String requestId;
    private Object content;
    private boolean success;

    public static NettyResponse success(Object responseObject) {
        NettyResponse nettyResponse = new NettyResponse();
        nettyResponse.content = responseObject;
        nettyResponse.success = true;
        return nettyResponse;
    }

    public static NettyResponse fail(Throwable cause) {
        NettyResponse nettyResponse = new NettyResponse();
        nettyResponse.content = cause.getMessage();
        nettyResponse.success = false;
        return nettyResponse;
    }
}