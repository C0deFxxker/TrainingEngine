package com.lyl.study.training.resource.core;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 端口映射规则
 *
 * @author liyilin
 */
@Data
@ToString
@EqualsAndHashCode
public class PortMap {
    /**
     * 容器端口
     */
    private int containerPort;
    /**
     * 映射到的主机端口
     */
    private int hostPort;
    /**
     * TCP或UDP，默认为TCP
     */
    private String protocol;
}
