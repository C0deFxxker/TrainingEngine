package com.lyl.study.trainning.engine.core.net.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.net.ProtocolFamily;

/**
 * 服务端Socket的通用配置
 *
 * @author liyilin
 */
@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ServerSocketOptions extends SocketOptions {
    /**
     * 内核全连接队列的容量
     * <p>
     * 内核会根据传入的backlog参数与系统参数somaxconn对比，取最小值。
     * <p>
     * 经过TCP三次握手后的连接会放入全连接队列，若ServerSocket没有及时accept()这些连接，并且全连接队列已堆满，服务器将会拒绝新连接。
     */
    private int backlog = System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS") ? 200 : 128;
    /**
     * 是否开启{@code SO_REUSEADDR}配置
     * <p>
     * 开启后，会有以下功能：
     * <li>允许在同一端口上启动同一服务器的多个实例，只要每个实例捆绑一个不同的本地IP地址即可。</li>
     * <li>当一个IP地址和端口号已绑定到某个套接字上时，如果传输协议支持（一般指UDP协议），同样的IP地址和端口还可以捆绑到另一个套接字上。</li>
     * <li>每次绑定端口的时候，如果此端口正在使用且绑定IP相同的话，新进程就会把端口“抢”过来</li>
     */
    private boolean reuseAddr = true;
    /**
     * 指定Socket的协议族
     */
    private ProtocolFamily protocolFamily = null;
}
