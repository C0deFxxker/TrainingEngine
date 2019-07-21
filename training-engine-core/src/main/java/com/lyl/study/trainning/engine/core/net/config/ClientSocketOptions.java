package com.lyl.study.trainning.engine.core.net.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 客户端Socket配置
 *
 * @author liyilin
 */
@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClientSocketOptions extends SocketOptions {
}
