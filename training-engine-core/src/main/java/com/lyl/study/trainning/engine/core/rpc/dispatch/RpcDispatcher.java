package com.lyl.study.trainning.engine.core.rpc.dispatch;

import com.lyl.study.trainning.engine.core.exception.RpcDispatchException;

import java.lang.reflect.Method;

/**
 * Rpc调用分发器
 *
 * @author liyilin
 */
public interface RpcDispatcher {
    /**
     * 根据服务名寻找分发目标
     *
     * @param serivceName 服务名
     */
    RpcDispatchTarget getTargetByServiceName(String serivceName);

    /**
     * 注册服务
     *
     * @param serviceName 服务名称（不能重名）
     * @param target      服务对象
     * @param method      服务方法
     * @throws IllegalArgumentException 注册的服务重名会抛出此异常
     */
    void registerService(String serviceName, Object target, Method method) throws IllegalArgumentException;

    /**
     * 注销服务
     *
     * @param serviceName 服务名
     * @return 是否注销成功；一般只有在指定服务名没有注册到分发器上才会返回false
     */
    boolean unregisterService(String serviceName);

    /**
     * 分发并调用服务
     *
     * @param serviceName 服务名
     * @param args        调用参数
     * @return 调用结果
     * @throws RpcDispatchException 调用过程异常
     */
    Object dispatch(String serviceName, Object[] args) throws RpcDispatchException;
}
