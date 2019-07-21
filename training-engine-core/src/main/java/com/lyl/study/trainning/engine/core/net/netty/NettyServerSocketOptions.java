package com.lyl.study.trainning.engine.core.net.netty;

import com.lyl.study.trainning.engine.core.net.config.ServerSocketOptions;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

/**
 * Netty服务器配置
 *
 * @author liyilin
 */
@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class NettyServerSocketOptions extends ServerSocketOptions {
    private Consumer<ChannelPipeline> pipelineConfigurer;
    private EventLoopGroup eventLoopGroup;
}
