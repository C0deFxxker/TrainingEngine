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
     * @param applicationInfo 应用信息，若应用提交成功，参数中的id属性会被设置为新创建的应用ID
     * @return 是否成功
     */
    boolean submitApplication(ApplicationInfo applicationInfo);

    /**
     * 获取应用详情
     *
     * @param applicationId 应用ID
     * @return 对应ID的应用详情信息；若找不到对应ID的应用信息，则返回 {@code null}
     */
    ApplicationContext getApplicationDetail(String applicationId);

    /**
     * 停止应用
     *
     * @param applicationId 应用ID
     * @return 是否成功
     */
    boolean shutdownApplication(String applicationId);

    /**
     * 强制停止应用
     *
     * @param applicationId 应用ID
     * @return 是否成功
     */
    boolean forceShutdownApplication(String applicationId);
}
