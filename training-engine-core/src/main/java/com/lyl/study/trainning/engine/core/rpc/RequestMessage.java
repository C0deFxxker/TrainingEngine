package com.lyl.study.trainning.engine.core.rpc;

import lombok.Data;
import lombok.ToString;

/**
 * Rpc请求消息模型
 *
 * @author liyilin
 */
@Data
@ToString
public class RequestMessage {
    /**
     * 请求ID
     */
    private String requestId;
    /**
     * 发送者地址
     */
    private RpcAddress senderAddress;
    /**
     * 接受者引用
     */
    private transient RpcEndpointRef receiver;
    /**
     * 消息内容
     */
    private Object content;
}
