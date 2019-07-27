package com.lyl.study.training.resource.core.protocol;

import com.lyl.study.training.resource.core.ApplicationContext;
import com.lyl.study.training.resource.core.ApplicationInfo;

/**
 * 资源管理器协议
 *
 * @author liyilin
 */
public interface ResourceManagerProtocol {
    /**
     * 提交应用
     *
     * @param applicationInfo
     * @return
     */
    boolean submitApplication(ApplicationInfo applicationInfo);

    /**
     * 获取应用状态
     *
     * @param applicationId
     * @return
     */
    ApplicationContext getApplicationStatus(String applicationId);

    /**
     * 停止应用
     *
     * @param applicationId
     * @return
     */
    boolean shutdownApplication(String applicationId);

    /**
     * 强制停止应用
     *
     * @param applicationId
     * @return
     */
    boolean forceShutdownApplication(String applicationId);
}
