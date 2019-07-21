package com.lyl.study.trainning.engine.core.net.config;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Socket连接通用配置类
 *
 * @author liyilin
 */
@Data
@Accessors(chain = true)
public abstract class SocketOptions {
    /**
     * 连接超时(ms)
     */
    private long timeoutMillis = 30000;
    /**
     * 是否开启Tcp协议的心跳检测
     * <p>
     * 开启此配置后，在连接闲置期间发送心跳检测信息（默认心跳周期是2小时），确认双方是否还保持连接
     */
    private boolean keepAlive = true;
    /**
     * Tcp协议的延迟关闭时长(秒)
     * <p>
     * 在调用Socket的{@code close()}方法时，若发送缓冲区中仍有数据残留且该值不为0时，则进程会进入睡眠状态等待相应的毫秒数，
     * 继续把缓冲区的数据发送出去。在超时前，Socket发送缓冲区已清空并收到对方ACK信号确认后，
     * 内核会用正常的FIN|ACK|FIN|ACK四个分组来关闭该连接。若超过该时长仍未能清空掉发送缓冲区的数据，
     * 则会发送RST分组(而不是用正常的FIN|ACK|FIN|ACK四个分组)来强制关闭该连接。
     * <p>
     * 若此值设置为0，在调用Socket的{@code close()}方法时，不再理会Socket发送缓冲区是否还有数据残留，
     * 直接向对方通过发送RST分组来强制关闭该连接。
     */
    private int lingerSeconds = 5;
    /**
     * 是否开启Tcp协议的Nagle算法
     * <p>
     * 该算法要求一个TCP连接上最多只能有一个未被确认的小分组，在该小分组的确认到来之前，不能发送其他小分组。
     */
    private boolean tcpNoDeplay = true;
    /**
     * 发送缓冲区大小（默认16K）
     * <p>
     * 为了达到最大网络吞吐，{@code SO_SNDBUF} 不应该小于带宽和延迟的乘积。
     */
    private int sndbuf = 1024 * 16;
    /**
     * 接收缓冲区大小（默认16K）
     * <p>
     * 对于TCP，如果应用进程一直没有读取缓冲区的数据，buffer满了之后，内核会主动通知对端TCP协议中的窗口关闭。
     * <p>
     * 对于UDP，如果应用进程一直没有读取缓冲区的数据，buffer满了之后，会把新来的数据丢弃掉。
     */
    private int rcvbuf = 1024 * 16;
    /**
     * 消息处理最大并发量
     * <p>
     * 设置为Long.MAX_VALUE时，不对消息处理并发量做限制。
     * <p>
     * 当消息生产速度大于消息消费速度时，需要设置该值来限制发送方发送消息的频率，否则会使消费方资源耗尽。
     * 比如，该值设置为10时，当消费方正在并行消费10个消息时，不再继续读取Channel中的消息。
     */
    private long prefetch = Long.MAX_VALUE;
}
