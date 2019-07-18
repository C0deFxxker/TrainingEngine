package com.lyl.study.trainning.engine.core.rpc.dispatch;

import com.lyl.study.trainning.engine.core.exception.RpcDispatchException;
import com.lyl.study.trainning.engine.core.exception.UnknownServiceException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认Rpc服务分发器
 *
 * @author liyilin
 */
@Slf4j
public class DefaultRpcDispatcher implements RpcDispatcher {
    Map<String, RpcDispatchTarget> dispatchTargetMap = new ConcurrentHashMap<>();

    @Override
    public RpcDispatchTarget getTargetByServiceName(String serivceName) {
        return dispatchTargetMap.get(serivceName);
    }

    @Override
    public void registerService(String serviceName, Object target, Method method) throws IllegalArgumentException {
        RpcDispatchTarget rpcDispatchTarget = new RpcDispatchTarget();
        rpcDispatchTarget.setSerivceName(serviceName);
        rpcDispatchTarget.setTarget(target);
        rpcDispatchTarget.setMethod(method);
        if (null != dispatchTargetMap.putIfAbsent(serviceName, rpcDispatchTarget)) {
            throw new IllegalArgumentException("服务名已被注册");
        }
    }

    @Override
    public boolean unregisterService(String serviceName) {
        return dispatchTargetMap.remove(serviceName) != null;
    }

    @Override
    public Object dispatch(String serviceName, Object[] args) throws RpcDispatchException {
        RpcDispatchTarget rpcDispatchTarget = getTargetByServiceName(serviceName);
        if (rpcDispatchTarget == null) {
            throw new UnknownServiceException("未知服务名[" + serviceName + "]");
        }

        Method method = rpcDispatchTarget.getMethod();
        Object target = rpcDispatchTarget.getTarget();
        try {
            if (log.isDebugEnabled()) {
                log.debug("调用Rpc服务: 方法={}, 参数列表={}", method.toString(), Arrays.asList(args));
            }
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RpcDispatchException("服务调用异常", e);
        }
    }
}
