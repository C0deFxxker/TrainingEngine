package com.lyl.study.training.resource.core;

/**
 * 应用状态
 *
 * @author liyilin
 */
public enum ApplicationStatus {
    /**
     * 资源申请中
     */
    PENDING,
    /**
     * 部署中
     */
    DEPLOYING,
    /**
     * 部分运行中
     */
    PARTITIAL_RUNNING,
    /**
     * 正在运行
     */
    RUNNING,
    /**
     * 失败
     */
    FAILED,
    /**
     * 正在中断
     */
    STOPPING,
    /**
     * 中断完毕
     */
    STOPPED
}
