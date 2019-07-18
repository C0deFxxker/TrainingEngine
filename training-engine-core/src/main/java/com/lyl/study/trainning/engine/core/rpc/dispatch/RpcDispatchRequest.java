package com.lyl.study.trainning.engine.core.rpc.dispatch;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * RPC调用过程中，客户端向服务端发送的服务请求内容
 *
 * @author liyilin
 */
@Data
@ToString
public class RpcDispatchRequest implements Serializable {
    private String serviceName;
    private Object[] arguments;
}
