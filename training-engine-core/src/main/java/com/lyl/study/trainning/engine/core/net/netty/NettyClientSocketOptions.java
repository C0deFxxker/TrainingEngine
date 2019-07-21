package com.lyl.study.trainning.engine.core.net.netty;

import com.lyl.study.trainning.engine.core.net.config.ClientSocketOptions;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

/**
 * Netty实现的客户端配置
 *
 * @author liyilin
 */
@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class NettyClientSocketOptions extends ClientSocketOptions {
    private Consumer<ChannelPipeline> pipelineConfigurer;
    private EventLoopGroup eventLoopGroup;
    private long reconnectDelayMillis = 5000L;
    private int reconnectMaxAttempts = Integer.MAX_VALUE;
}
