package com.lyl.study.training.resource.core;

import java.util.Date;

/**
 * 应用实例信息
 *
 * @author liyilin
 */
public class InstanceInfo {
    /**
     * 实例ID
     */
    private String id;
    /**
     * 应用ID
     */
    private String applicationId;
    /**
     * 虚拟IP
     */
    private String virutalIp;
    /**
     * 物理机IP
     */
    private String machineIp;
    /**
     * 重启次数
     */
    private Integer restarts;
    /**
     * 实例状态
     */
    private InstanceStatus status;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 启动时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date endTime;
}
