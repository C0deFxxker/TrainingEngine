package com.lyl.study.training.resource.core;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

/**
 * 应用资源申请上下文
 *
 * @author liyilin
 */
@Data
@ToString
@EqualsAndHashCode
public class ApplicationInfo {
    /**
     * 应用ID
     */
    private String id;
    /**
     * 应用名称
     */
    private String name;
    /**
     * 需要的资源
     */
    private Resource resource;
    /**
     * 环境变量
     */
    private Map<String, String> environment;
    /**
     * 运行参数
     */
    private String[] command;
    /**
     * 资源队列
     */
    private String queue;
    /**
     * 优先级
     */
    private Long priority;
}