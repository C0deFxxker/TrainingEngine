package com.lyl.study.training.resource.core;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * 应用上下文
 *
 * @author liyilin
 */
@Data
@ToString
@EqualsAndHashCode
public class ApplicationContext {
    /**
     * 应用信息
     */
    ApplicationInfo applicationInfo;
    /**
     * 应用实例信息
     */
    private List<InstanceInfo> instanceInfos;
    /**
     * 应用状态
     */
    private ApplicationStatus status;
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
