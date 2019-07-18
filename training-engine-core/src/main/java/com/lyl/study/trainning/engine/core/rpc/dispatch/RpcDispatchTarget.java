package com.lyl.study.trainning.engine.core.rpc.dispatch;

import lombok.Data;
import lombok.ToString;

import java.lang.reflect.Method;

/**
 * 分发目标信息
 *
 * @author liyilin
 */
@Data
@ToString
public class RpcDispatchTarget {
    /**
     * 请求的服务名称
     */
    private String serivceName;
    /**
     * 服务目标对象
     * <p>
     * 不在网络过程中传输，只在服务端解析请求信息后得到
     */
    private transient Object target;
    /**
     * 服务目标方法
     * <p>
     * 不在网络过程中传输，只在服务端解析请求信息后得到
     */
    private transient Method method;
}
