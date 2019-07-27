package com.lyl.study.training.resource.core;

/**
 * 应用实例状态
 *
 * @author liyilin
 */
public enum InstanceStatus {
    /**
     * 资源申请中
     */
    PENDING,
    /**
     * 部署中
     */
    DEPLOYING,
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
