package com.lyl.study.trainning.engine.core.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.SocketAddress;

/**
 * Rpc调用地址
 *
 * @author liyilin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcAddress {
    private String host;
    private Integer port;

    public RpcAddress(SocketAddress socketAddress) {
        String remoteAddress = socketAddress.toString();
        int idx = remoteAddress.lastIndexOf(":");
        port = 80;
        if (idx != -1) {
            host = remoteAddress.substring(0, idx);
            port = Integer.parseInt(remoteAddress.substring(idx + 1));
        } else {
            host = remoteAddress;
        }
    }

    public String toString() {
        return host + ":" + port;
    }
}
